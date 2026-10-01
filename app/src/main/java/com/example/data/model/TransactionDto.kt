package com.example.data.model

/**
 * Data Transfer Object representing a transaction as received from/sent to the backend.
 * Category is determined by the backend NLP service (ESSENTIAL or DISCRETIONARY).
 */
enum class TransactionCategory {
  ESSENTIAL,
  DISCRETIONARY
}

enum class TransactionType {
  DEBIT,
  CREDIT
}

enum class TransactionStatus {
  PENDING,
  CLASSIFIED,
  ROUNDED_UP,
  INVESTED
}

data class TransactionDto(
  val id: String,
  val merchant: String,
  val amount: Double,
  val date: Long,
  val transactionType: TransactionType,
  val category: TransactionCategory? = null,
  val roundUpAmount: Double = 0.0,
  val status: TransactionStatus = TransactionStatus.PENDING
)
