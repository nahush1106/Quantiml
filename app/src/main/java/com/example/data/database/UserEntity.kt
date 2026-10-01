package com.example.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "users",
  indices = [
    Index(value = ["email"], unique = true),
    Index(value = ["mobileNumber"], unique = true)
  ]
)
data class UserEntity(
  @PrimaryKey val id: String,
  val email: String,
  val mobileNumber: String,
  val fullName: String,
  val passCodeHash: String,
  val createdAt: Long = System.currentTimeMillis(),
  val roundUpTarget: Int = 10,
  val multiplier: Double = 1.0,
  val autoInvestThreshold: Double = 500.0,
  val isBiometricEnabled: Boolean = true
)
