package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
  @Json(name = "fullName") val fullName: String,
  @Json(name = "email") val email: String,
  @Json(name = "phone") val phone: String,
  @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
  @Json(name = "identifier") val identifier: String, // email or phone
  @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserDto(
  @Json(name = "id") val id: String,
  @Json(name = "fullName") val fullName: String,
  @Json(name = "email") val email: String,
  @Json(name = "phone") val phone: String,
  @Json(name = "roundUpTarget") val roundUpTarget: Int = 10,
  @Json(name = "roundUpMultiplier") val roundUpMultiplier: Double = 1.0,
  @Json(name = "autoInvestThreshold") val autoInvestThreshold: Double = 500.0,
  @Json(name = "selectedAssetDestination") val selectedAssetDestination: String = "NIFTY_50_INDEX"
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
  @Json(name = "success") val success: Boolean,
  @Json(name = "token") val token: String?,
  @Json(name = "user") val user: UserDto?,
  @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class CreateTransactionRequest(
  @Json(name = "merchant") val merchant: String,
  @Json(name = "amount") val amount: Double,
  @Json(name = "roundUpAmount") val roundUpAmount: Double,
  @Json(name = "totalSpent") val totalSpent: Double,
  @Json(name = "category") val category: String,
  @Json(name = "status") val status: String = "ROUNDED_UP",
  @Json(name = "rawSmsBody") val rawSmsBody: String? = null
)

@JsonClass(generateAdapter = true)
data class TransactionResponseDto(
  @Json(name = "_id") val id: String = "",
  @Json(name = "userId") val userId: String = "",
  @Json(name = "merchant") val merchant: String = "",
  @Json(name = "amount") val amount: Double = 0.0,
  @Json(name = "roundUpAmount") val roundUpAmount: Double = 0.0,
  @Json(name = "totalSpent") val totalSpent: Double = 0.0,
  @Json(name = "category") val category: String = "DISCRETIONARY",
  @Json(name = "status") val status: String = "ROUNDED_UP",
  @Json(name = "rawSmsBody") val rawSmsBody: String? = null,
  @Json(name = "timestamp") val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class DeployResponse(
  @Json(name = "success") val success: Boolean,
  @Json(name = "deployedCount") val deployedCount: Int,
  @Json(name = "totalDeployedAmount") val totalDeployedAmount: Double,
  @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class UpdateSettingsRequest(
  @Json(name = "roundUpTarget") val roundUpTarget: Int? = null,
  @Json(name = "roundUpMultiplier") val roundUpMultiplier: Double? = null,
  @Json(name = "autoInvestThreshold") val autoInvestThreshold: Double? = null,
  @Json(name = "selectedAssetDestination") val selectedAssetDestination: String? = null
)
