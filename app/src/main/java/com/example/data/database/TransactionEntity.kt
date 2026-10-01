package com.example.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType

@Entity(
  tableName = "transactions",
  indices = [
    Index(value = ["userId"]),
    Index(value = ["date"])
  ]
)
data class TransactionEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_arjun_001",
  val merchant: String,
  val amount: Double,
  val date: Long,
  val transactionType: String = TransactionType.DEBIT.name,
  val category: String = TransactionCategory.DISCRETIONARY.name,
  val roundUpAmount: Double = 0.0,
  val status: String = TransactionStatus.ROUNDED_UP.name,
  val rawSmsBody: String? = null
)
