package com.example.data.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.auth.AuthRepository
import com.example.data.database.QuantimlDatabase
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsTransactionReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "SmsTransactionReceiver"
    private const val CHANNEL_ID = "quantiml_sms_alerts"
    private const val CHANNEL_NAME = "Bank SMS Auto-Detection Alerts"
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
    if (messages.isNullOrEmpty()) return

    val fullBody = StringBuilder()
    var sender = "BANK"
    for (sms in messages) {
      fullBody.append(sms.messageBody)
      sms.originatingAddress?.let { sender = it }
    }

    val bodyText = fullBody.toString()
    Log.d(TAG, "Incoming SMS from: $sender: $bodyText")

    if (!BankSmsParser.isBankDebitSms(sender, bodyText)) {
      Log.d(TAG, "SMS ignored (not a bank debit alert)")
      return
    }

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val appContext = context.applicationContext
        val db = QuantimlDatabase.getDatabase(appContext)
        val authRepo = AuthRepository(appContext, db.userDao())
        val session = authRepo.userSessionFlow.first()
        val currentUserId = if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID

        val transactionRepo = TransactionRepository(db.transactionDao(), authRepo)

        val parsed = BankSmsParser.parse(
          body = bodyText,
          sender = sender,
          roundToMultiple = session.roundUpTarget,
          multiplier = session.roundUpMultiplier
        )
        if (parsed != null) {
          Log.i(TAG, "Parsed bank SMS: Rs ${parsed.amount} at ${parsed.merchant} (Target: ₹${session.roundUpTarget}, Mult: ${session.roundUpMultiplier}x)")
          transactionRepo.recordTransaction(
            merchant = parsed.merchant,
            amount = parsed.amount,
            customMultiplier = session.roundUpMultiplier,
            roundToMultiple = session.roundUpTarget,
            targetUserId = currentUserId,
            rawSms = bodyText
          )

          showNotification(appContext, parsed)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error handling incoming bank SMS", e)
      } finally {
        pendingResult.finish()
      }
    }
  }

  private fun showNotification(context: Context, parsed: ParsedBankTransaction) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          CHANNEL_ID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH
        ).apply {
          description = "Notifications when bank transaction debit SMS is auto-detected"
        }
        notificationManager.createNotificationChannel(channel)
      }

      val roundUpInt = parsed.roundUpAmount.toInt()
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Quantiml: Bank SMS Detected 💳")
        .setContentText("Spent ₹${parsed.amount.toInt()} at ${parsed.merchant}. +₹$roundUpInt added to wallet!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

      notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to show notification", e)
    }
  }
}
