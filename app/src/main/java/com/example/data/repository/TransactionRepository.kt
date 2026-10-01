package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.api.model.CreateTransactionRequest
import com.example.data.auth.AuthRepository
import com.example.data.database.TransactionDao
import com.example.data.database.TransactionEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionStatus
import com.example.data.nlp.QuantAiNlpClassifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepository(
  private val transactionDao: TransactionDao,
  private val authRepository: AuthRepository? = null
) {

  // Reactively query transactions belonging to the currently authenticated user
  val allTransactions: Flow<List<TransactionEntity>> = if (authRepository != null) {
    authRepository.userSessionFlow.flatMapLatest { session ->
      val uid = if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID
      transactionDao.getTransactionsForUser(uid)
    }
  } else {
    transactionDao.getAllTransactions()
  }

  val walletBalance: Flow<Double?> = if (authRepository != null) {
    authRepository.userSessionFlow.flatMapLatest { session ->
      val uid = if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID
      transactionDao.getWalletSpareChangeSum(uid)
    }
  } else {
    transactionDao.getWalletSpareChangeSum(AuthRepository.DEFAULT_DEMO_USER_ID)
  }

  val totalInvested: Flow<Double?> = if (authRepository != null) {
    authRepository.userSessionFlow.flatMapLatest { session ->
      val uid = if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID
      transactionDao.getTotalInvestedSum(uid)
    }
  } else {
    transactionDao.getTotalInvestedSum(AuthRepository.DEFAULT_DEMO_USER_ID)
  }

  suspend fun ensureInitialData(targetUserId: String = AuthRepository.DEFAULT_DEMO_USER_ID) {
    if (transactionDao.getTransactionCount(targetUserId) == 0) {
      val now = System.currentTimeMillis()
      val sampleData = listOf(
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Starbucks Reserve",
          amount = 294.0,
          date = now - 3600000L * 2, // 2 hrs ago
          category = TransactionCategory.DISCRETIONARY.name,
          roundUpAmount = 6.0,
          status = TransactionStatus.ROUNDED_UP.name,
          rawSmsBody = "Sent Rs. 294.00 from HDFC Bank to Starbucks via UPI on 24-Sep-26"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Blinkit Groceries",
          amount = 442.0,
          date = now - 3600000L * 6, // 6 hrs ago
          category = TransactionCategory.ESSENTIAL.name,
          roundUpAmount = 8.0,
          status = TransactionStatus.ROUNDED_UP.name,
          rawSmsBody = "INR 442.00 spent on ICICI Bank Card ending 4012 at Blinkit"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Zomato Dine-in",
          amount = 685.0,
          date = now - 86400000L, // yesterday
          category = TransactionCategory.DISCRETIONARY.name,
          roundUpAmount = 15.0,
          status = TransactionStatus.ROUNDED_UP.name,
          rawSmsBody = "Your A/C XX4921 is debited by Rs 685.00 at Zomato. UPI Ref 382910"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Apollo Pharmacy",
          amount = 182.0,
          date = now - 86400000L * 2,
          category = TransactionCategory.ESSENTIAL.name,
          roundUpAmount = 8.0,
          status = TransactionStatus.ROUNDED_UP.name,
          rawSmsBody = "Paid Rs. 182.00 to Apollo Pharmacy using Paytm UPI"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Cult.fit Gym",
          amount = 1450.0,
          date = now - 86400000L * 5,
          category = TransactionCategory.DISCRETIONARY.name,
          roundUpAmount = 50.0,
          status = TransactionStatus.INVESTED.name,
          rawSmsBody = "INR 1450.00 debited from A/C *5531 towards Cult.fit"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "Uber Premier",
          amount = 313.0,
          date = now - 86400000L * 6,
          category = TransactionCategory.DISCRETIONARY.name,
          roundUpAmount = 7.0,
          status = TransactionStatus.INVESTED.name,
          rawSmsBody = "Paid Rs. 313.00 to Uber India via UPI from HDFC Bank"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          userId = targetUserId,
          merchant = "DMart Supermarket",
          amount = 1890.0,
          date = now - 86400000L * 8,
          category = TransactionCategory.ESSENTIAL.name,
          roundUpAmount = 10.0,
          status = TransactionStatus.INVESTED.name,
          rawSmsBody = "INR 1890.00 spent at DMart Supermarket on 16-Sep-26"
        )
      )
      transactionDao.insertAll(sampleData)
    }
  }

  suspend fun getCurrentUserId(): String {
    return if (authRepository != null) {
      val session = authRepository.userSessionFlow.first()
      if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID
    } else {
      AuthRepository.DEFAULT_DEMO_USER_ID
    }
  }

  suspend fun recordTransaction(
    merchant: String,
    amount: Double,
    customMultiplier: Double? = null,
    roundToMultiple: Int? = null,
    targetUserId: String? = null,
    rawSms: String? = null
  ): TransactionEntity {
    val uid = targetUserId ?: getCurrentUserId()

    // Read user preferences if not explicitly overridden
    val (effectiveMultiplier, effectiveTarget) = if (authRepository != null) {
      val session = authRepository.userSessionFlow.first()
      Pair(
        customMultiplier ?: session.roundUpMultiplier,
        roundToMultiple ?: session.roundUpTarget
      )
    } else {
      Pair(customMultiplier ?: 1.0, roundToMultiple ?: 10)
    }

    val category = QuantAiNlpClassifier.classify(merchant)
    val roundUp = QuantAiNlpClassifier.calculateRoundUp(amount, effectiveTarget, effectiveMultiplier)

    val entity = TransactionEntity(
      id = UUID.randomUUID().toString(),
      userId = uid,
      merchant = merchant,
      amount = amount,
      date = System.currentTimeMillis(),
      category = category.name,
      roundUpAmount = roundUp,
      status = TransactionStatus.ROUNDED_UP.name,
      rawSmsBody = rawSms
    )

    transactionDao.insertTransaction(entity)

    // Sync to Node.js backend using JWT Bearer token if session is authenticated
    if (authRepository != null) {
      try {
        val session = authRepository.userSessionFlow.first()
        val token = session.authToken
        if (session.isLoggedIn && !token.isNullOrBlank()) {
          NetworkClient.apiService.createTransaction(
            bearerToken = "Bearer $token",
            request = CreateTransactionRequest(
              merchant = merchant,
              amount = amount,
              roundUpAmount = roundUp,
              totalSpent = amount + roundUp,
              category = category.name,
              status = TransactionStatus.ROUNDED_UP.name,
              rawSmsBody = rawSms
            )
          )
        }
      } catch (_: Exception) {
        // Safe offline operation
      }
    }

    return entity
  }

  suspend fun syncTransactionsFromBackend(): Result<Int> {
    if (authRepository == null) return Result.success(0)
    try {
      val session = authRepository.userSessionFlow.first()
      val token = session.authToken
      if (!session.isLoggedIn || token.isNullOrBlank()) {
        return Result.success(0)
      }

      val response = NetworkClient.apiService.getTransactions("Bearer $token")
      if (response.isSuccessful) {
        val dtoList = response.body() ?: emptyList()
        if (dtoList.isNotEmpty()) {
          val currentUid = if (session.userId.isNotBlank()) session.userId else AuthRepository.DEFAULT_DEMO_USER_ID
          val entities = dtoList.map { dto ->
            val timestamp = try {
              if (!dto.timestamp.isNullOrBlank()) {
                Instant.parse(dto.timestamp).toEpochMilli()
              } else {
                System.currentTimeMillis()
              }
            } catch (_: Exception) {
              System.currentTimeMillis()
            }

            TransactionEntity(
              id = dto.id.ifBlank { UUID.randomUUID().toString() },
              userId = if (dto.userId.isNotBlank()) dto.userId else currentUid,
              merchant = dto.merchant,
              amount = dto.amount,
              date = timestamp,
              category = dto.category,
              roundUpAmount = dto.roundUpAmount,
              status = dto.status,
              rawSmsBody = dto.rawSmsBody
            )
          }
          transactionDao.insertAll(entities)
          return Result.success(entities.size)
        }
      }
      return Result.success(0)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  suspend fun syncFromSmsInbox(context: android.content.Context): Int {
    try {
      val parsedList = com.example.data.sms.BankSmsParser.readRecentBankSmsFromInbox(context)
      if (parsedList.isEmpty()) return 0

      var importedCount = 0
      val currentUid = getCurrentUserId()

      for (parsed in parsedList) {
        val exists = parsed.rawSms.isNotBlank() && transactionDao.getTransactionCountBySmsBody(parsed.rawSms) > 0
        if (!exists) {
          recordTransaction(
            merchant = parsed.merchant,
            amount = parsed.amount,
            targetUserId = currentUid,
            rawSms = parsed.rawSms
          )
          importedCount++
        }
      }
      return importedCount
    } catch (_: Exception) {
      return 0
    }
  }

  suspend fun deployWalletToInvestments(): Int {
    val uid = getCurrentUserId()
    if (authRepository != null) {
      try {
        val session = authRepository.userSessionFlow.first()
        val token = session.authToken
        if (session.isLoggedIn && !token.isNullOrBlank()) {
          NetworkClient.apiService.deployTransactions(bearerToken = "Bearer $token")
        }
      } catch (_: Exception) {
        // Safe offline operation
      }
    }
    return transactionDao.markAllWalletAsInvested(uid)
  }
}
