package com.example.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.nlp.QuantAiNlpClassifier
import java.util.Locale
import java.util.regex.Pattern

data class ParsedBankTransaction(
  val amount: Double,
  val merchant: String,
  val bankName: String,
  val rawSms: String,
  val category: String,
  val roundUpAmount: Double,
  val isDebit: Boolean = true,
  val timestamp: Long = System.currentTimeMillis()
)

data class SampleBankSms(
  val bank: String,
  val sender: String,
  val sampleText: String,
  val expectedMerchant: String,
  val expectedAmount: Double
)

object BankSmsParser {

  val SAMPLE_BANK_MESSAGES = listOf(
    SampleBankSms(
      bank = "Bank of India (BOI)",
      sender = "AD-BOIBK",
      sampleText = "Rs.1.00 debited A/cXX0497 and credited to SAHIL SANTOSH KUMBHAR via UPI Ref No 627223305646 on 29Sep26. Call 18001031906, if not done by you. -BOI",
      expectedMerchant = "SAHIL SANTOSH KUMBHAR",
      expectedAmount = 1.0
    ),
    SampleBankSms(
      bank = "HDFC Bank (UPI)",
      sender = "VM-HDFCBK",
      sampleText = "Sent Rs. 340.00 from HDFC Bank A/C **1234 to Starbucks via UPI on 24-Sep-26. Ref: 382910. Avail Bal: INR 12,430.00",
      expectedMerchant = "Starbucks",
      expectedAmount = 340.0
    ),
    SampleBankSms(
      bank = "ICICI Bank (Card)",
      sender = "AD-ICICIB",
      sampleText = "Dear Customer, INR 248.00 spent on ICICI Bank Card ending 4012 at Blinkit on 24-Sep-26. Avail Limit: INR 85,000.00",
      expectedMerchant = "Blinkit",
      expectedAmount = 248.0
    ),
    SampleBankSms(
      bank = "State Bank of India (A/C)",
      sender = "BZ-SBIINB",
      sampleText = "Your A/C XX4921 is debited by Rs 185.00 on 24-09-2026 at Zomato. UPI Ref 382910 - SBI",
      expectedMerchant = "Zomato",
      expectedAmount = 185.0
    ),
    SampleBankSms(
      bank = "Axis Bank (Travel)",
      sender = "AX-AXISBK",
      sampleText = "INR 415.00 debited from A/C *5531 on 24-Sep-26 towards Uber India via UPI. Ref 992831",
      expectedMerchant = "Uber India",
      expectedAmount = 415.0
    ),
    SampleBankSms(
      bank = "Paytm / UPI (Pharmacy)",
      sender = "PAYTM",
      sampleText = "Paid Rs. 92.00 to Apollo Pharmacy using Paytm UPI on 24 Sep. UPI Ref: 202609241234",
      expectedMerchant = "Apollo Pharmacy",
      expectedAmount = 92.0
    )
  )

  private val DEBIT_KEYWORDS = listOf(
    "debited", "debit", "spent", "sent", "paid", "withdrawn",
    "transferred", "transfer", "txn", "purchase", "purchased", "payment",
    "deducted", "completed", "successful", "vpa", "upi"
  )

  fun isBankDebitSms(sender: String, body: String): Boolean {
    val lower = body.lowercase(Locale.ROOT)

    val hasDebitKeyword = DEBIT_KEYWORDS.any { lower.contains(it) }

    // Differentiate user account receiving money from "credited to beneficiary"
    val isUserAccountCredited = (lower.contains("credited to a/c") ||
        lower.contains("credited to your") ||
        lower.contains("is credited by") ||
        lower.contains("has been credited") ||
        lower.contains("credited with")) &&
        !lower.contains("debited")

    val isOtpOrUtility = lower.contains("otp") || lower.contains("verification") ||
        lower.contains("code is") || lower.contains("do not share") || lower.contains("bill due")

    val hasCurrencyOrAccount = lower.contains("rs") || lower.contains("re") || lower.contains("inr") ||
        lower.contains("₹") || lower.contains("rupee") || lower.contains("a/c") || lower.contains("ac") ||
        lower.contains("upi") || lower.contains("vpa") || lower.contains("bal")

    return hasDebitKeyword && !isUserAccountCredited && !isOtpOrUtility && hasCurrencyOrAccount
  }

  fun parse(
    body: String,
    sender: String = "BANK",
    roundToMultiple: Int = 10,
    multiplier: Double = 1.0
  ): ParsedBankTransaction? {
    if (!isBankDebitSms(sender, body)) {
      return null
    }

    val amount = extractAmount(body) ?: return null
    val merchant = extractMerchant(body)
    val bank = detectBank(sender, body)
    val category = QuantAiNlpClassifier.classify(merchant)
    val roundUp = QuantAiNlpClassifier.calculateRoundUp(amount, roundToMultiple, multiplier)

    return ParsedBankTransaction(
      amount = amount,
      merchant = merchant,
      bankName = bank,
      rawSms = body,
      category = category.name,
      roundUpAmount = roundUp,
      isDebit = true,
      timestamp = System.currentTimeMillis()
    )
  }

  private fun extractAmount(text: String): Double? {
    val patterns = listOf(
      Pattern.compile("""(?:Rs\.?|Re\.?|INR|₹|Rupees?)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
      Pattern.compile("""(?:debited|spent|paid|sent|transferred)\s*(?:for|by|with|of)?\s*(?:Rs\.?|Re\.?|INR|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
      Pattern.compile("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:spent|debited|sent|paid|transferred)""", Pattern.CASE_INSENSITIVE),
      Pattern.compile("""\b([0-9]+(?:\.[0-9]{1,2})?)\b""")
    )

    for (p in patterns) {
      val matcher = p.matcher(text)
      while (matcher.find()) {
        val rawNum = matcher.group(1)?.replace(",", "")
        val parsed = rawNum?.toDoubleOrNull()
        if (parsed != null && parsed > 0 && parsed < 1000000) {
          return parsed
        }
      }
    }
    return null
  }

  private fun extractMerchant(text: String): String {
    val knownMerchants = listOf(
      "Starbucks", "Blinkit", "Zomato", "Swiggy", "Uber India", "Uber",
      "Ola", "Apollo Pharmacy", "DMart", "Amazon", "Flipkart", "Zepto",
      "Cult.fit", "Decathlon", "Reliance Fresh", "BigBasket", "BookMyShow",
      "Netflix", "Spotify", "McDonald's", "KFC", "Dominos", "Dunzo",
      "Tata 1mg", "Myntra", "Nykaa"
    )

    for (km in knownMerchants) {
      if (text.contains(km, ignoreCase = true)) {
        return km
      }
    }

    val prepositionPatterns = listOf(
      Pattern.compile("""(?:credited\s+to|transfer\s+to|paid\s+to|sent\s+to|towards|at|to|vpa|info:)\s+([A-Za-z0-9\s\.\&\-]{2,30}?)(?:\s+(?:on|via|Ref|UPI|using|Avail|for|\.)|$)""", Pattern.CASE_INSENSITIVE),
      Pattern.compile("""(?:spent\s+at)\s+([A-Za-z0-9\s\.\&\-]{2,25}?)(?:\s+(?:on|via|\.)|$)""", Pattern.CASE_INSENSITIVE)
    )

    for (p in prepositionPatterns) {
      val m = p.matcher(text)
      if (m.find()) {
        val candidate = m.group(1)?.trim()
        if (!candidate.isNullOrBlank() && candidate.length in 2..30 && !candidate.equals("your", ignoreCase = true) && !candidate.equals("a/c", ignoreCase = true)) {
          return cleanMerchant(candidate)
        }
      }
    }

    return "UPI Transfer"
  }

  private fun cleanMerchant(raw: String): String {
    return raw.replace(Regex("""(?i)\b(via|using|card|ref|ac|a/c)\b.*"""), "")
      .trim()
      .ifBlank { "Merchant" }
  }

  private fun detectBank(sender: String, body: String): String {
    val upper = (sender + " " + body).uppercase(Locale.ROOT)
    return when {
      upper.contains("BOI") || upper.contains("BANK OF INDIA") -> "Bank of India"
      upper.contains("HDFC") -> "HDFC Bank"
      upper.contains("ICICI") -> "ICICI Bank"
      upper.contains("SBI") || upper.contains("STATE BANK") -> "State Bank of India"
      upper.contains("AXIS") -> "Axis Bank"
      upper.contains("KOTAK") -> "Kotak Mahindra"
      upper.contains("PAYTM") -> "Paytm UPI"
      upper.contains("PHONEPE") -> "PhonePe"
      upper.contains("GPAY") || upper.contains("GOOGLE PAY") -> "Google Pay"
      upper.contains("BARODA") || upper.contains("BOB") -> "Bank of Baroda"
      upper.contains("PNB") || upper.contains("PUNJAB") -> "Punjab National Bank"
      else -> "Bank UPI"
    }
  }

  fun readRecentBankSmsFromInbox(context: Context): List<ParsedBankTransaction> {
    val results = mutableListOf<ParsedBankTransaction>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
      return results
    }

    try {
      val uri = Uri.parse("content://sms/inbox")
      val projection = arrayOf("_id", "address", "body", "date")
      val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
      val selection = "date >= ?"
      val selectionArgs = arrayOf(oneDayAgo.toString())

      val cursor = context.contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        "date DESC"
      )

      cursor?.use { c ->
        val addressIdx = c.getColumnIndex("address")
        val bodyIdx = c.getColumnIndex("body")
        val dateIdx = c.getColumnIndex("date")

        while (c.moveToNext()) {
          val address = if (addressIdx != -1) c.getString(addressIdx) ?: "" else ""
          val body = if (bodyIdx != -1) c.getString(bodyIdx) ?: "" else ""
          val smsDate = if (dateIdx != -1) c.getLong(dateIdx) else System.currentTimeMillis()

          if (isBankDebitSms(address, body)) {
            val parsed = parse(body, address)
            if (parsed != null) {
              results.add(parsed.copy(timestamp = smsDate))
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e("BankSmsParser", "Error reading SMS inbox", e)
    }
    return results
  }
}
