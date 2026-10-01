package com.example.data.api

import com.example.data.api.model.AuthResponse
import com.example.data.api.model.CreateTransactionRequest
import com.example.data.api.model.DeployResponse
import com.example.data.api.model.LoginRequest
import com.example.data.api.model.RegisterRequest
import com.example.data.api.model.TransactionResponseDto
import com.example.data.api.model.UpdateSettingsRequest
import com.example.data.api.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface QuantimlApiService {

  @POST("api/auth/register")
  suspend fun register(
    @Body request: RegisterRequest
  ): Response<AuthResponse>

  @POST("api/auth/login")
  suspend fun login(
    @Body request: LoginRequest
  ): Response<AuthResponse>

  @GET("api/auth/me")
  suspend fun getMe(
    @Header("Authorization") bearerToken: String
  ): Response<UserDto>

  @GET("api/transactions?format=array")
  suspend fun getTransactions(
    @Header("Authorization") bearerToken: String
  ): Response<List<TransactionResponseDto>>

  @POST("api/transactions")
  suspend fun createTransaction(
    @Header("Authorization") bearerToken: String,
    @Body request: CreateTransactionRequest
  ): Response<TransactionResponseDto>

  @POST("api/transactions/deploy")
  suspend fun deployTransactions(
    @Header("Authorization") bearerToken: String
  ): Response<DeployResponse>

  @PUT("api/user/settings")
  suspend fun updateSettings(
    @Header("Authorization") bearerToken: String,
    @Body request: UpdateSettingsRequest
  ): Response<UserDto>
}
