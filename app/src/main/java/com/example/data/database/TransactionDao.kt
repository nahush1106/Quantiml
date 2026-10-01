package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
  fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions ORDER BY date DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category ORDER BY date DESC")
  fun getTransactionsByCategory(userId: String, category: String): Flow<List<TransactionEntity>>

  @Query("SELECT SUM(roundUpAmount) FROM transactions WHERE userId = :userId AND status = 'ROUNDED_UP'")
  fun getWalletSpareChangeSum(userId: String): Flow<Double?>

  @Query("SELECT SUM(roundUpAmount) FROM transactions WHERE userId = :userId AND status = 'INVESTED'")
  fun getTotalInvestedSum(userId: String): Flow<Double?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Query("UPDATE transactions SET status = 'INVESTED' WHERE userId = :userId AND status = 'ROUNDED_UP'")
  suspend fun markAllWalletAsInvested(userId: String): Int

  @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
  suspend fun getTransactionCount(userId: String): Int

  @Query("SELECT COUNT(*) FROM transactions WHERE rawSmsBody = :rawSmsBody")
  suspend fun getTransactionCountBySmsBody(rawSmsBody: String): Int

  @Query("SELECT COUNT(*) FROM transactions")
  suspend fun getTotalTransactionCount(): Int
}
