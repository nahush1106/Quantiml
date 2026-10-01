package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [TransactionEntity::class, UserEntity::class],
  version = 2,
  exportSchema = false
)
abstract class QuantimlDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun userDao(): UserDao

  companion object {
    @Volatile
    private var INSTANCE: QuantimlDatabase? = null

    fun getDatabase(context: Context): QuantimlDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          QuantimlDatabase::class.java,
          "quantiml_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
