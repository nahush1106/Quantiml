package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

  // For Android Emulator to access host machine localhost: use http://10.0.2.2:5000/
  // For physical Android device: use your computer's local Wi-Fi IP (e.g., http://192.168.1.X:5000/)
  // Or your deployed Cloud server (e.g., https://your-quantiml-api.onrender.com/)
  @Volatile
  var baseUrl: String = "http://10.0.2.2:5000/"
    set(value) {
      field = if (value.endsWith("/")) value else "$value/"
      apiService = buildApiService(field)
    }

  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .addInterceptor(logging)
      .build()
  }

  private fun buildApiService(url: String): QuantimlApiService {
    return Retrofit.Builder()
      .baseUrl(url)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(QuantimlApiService::class.java)
  }

  @Volatile
  var apiService: QuantimlApiService = buildApiService(baseUrl)
    private set
}
