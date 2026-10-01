package com.example.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.api.NetworkClient
import com.example.data.api.model.LoginRequest
import com.example.data.api.model.RegisterRequest
import com.example.data.api.model.UpdateSettingsRequest
import com.example.data.database.UserDao
import com.example.data.database.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quantiml_auth_prefs")

data class UserSession(
  val isLoggedIn: Boolean = false,
  val userId: String = "user_arjun_001",
  val fullName: String = "Arjun Sharma",
  val email: String = "arjun.sharma@example.com",
  val mobileNumber: String = "+91 98765 43210",
  val authToken: String? = null,
  val isBackendConnected: Boolean = false,
  val backendSource: String = "Local",
  val roundUpTarget: Int = 10,
  val roundUpMultiplier: Double = 1.0,
  val autoInvestThreshold: Double = 500.0,
  val selectedAssetDestination: String = "NIFTY_50_INDEX"
)

class AuthRepository(
  private val context: Context,
  private val userDao: UserDao? = null
) {

  companion object {
    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_MOBILE = stringPreferencesKey("mobile")
    private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_IS_BACKEND_CONNECTED = booleanPreferencesKey("is_backend_connected")
    private val KEY_BACKEND_SOURCE = stringPreferencesKey("backend_source")
    private val KEY_ROUND_UP_TARGET = intPreferencesKey("round_up_target")
    private val KEY_ROUND_UP_MULTIPLIER = doublePreferencesKey("round_up_multiplier")
    private val KEY_AUTO_INVEST_THRESHOLD = doublePreferencesKey("auto_invest_threshold")
    private val KEY_SELECTED_ASSET_DESTINATION = stringPreferencesKey("selected_asset_destination")
    private val KEY_BASE_URL = stringPreferencesKey("base_url")

    const val DEFAULT_DEMO_USER_ID = "user_arjun_001"
  }

  val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
    // Synchronize saved baseUrl if configured
    val savedUrl = prefs[KEY_BASE_URL]
    if (!savedUrl.isNullOrBlank() && savedUrl != NetworkClient.baseUrl) {
      NetworkClient.baseUrl = savedUrl
    }

    UserSession(
      isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false,
      userId = prefs[KEY_USER_ID] ?: DEFAULT_DEMO_USER_ID,
      fullName = prefs[KEY_FULL_NAME] ?: "Arjun Sharma",
      email = prefs[KEY_EMAIL] ?: "arjun.sharma@example.com",
      mobileNumber = prefs[KEY_MOBILE] ?: "+91 98765 43210",
      authToken = prefs[KEY_AUTH_TOKEN],
      isBackendConnected = prefs[KEY_IS_BACKEND_CONNECTED] ?: false,
      backendSource = prefs[KEY_BACKEND_SOURCE] ?: "Local",
      roundUpTarget = prefs[KEY_ROUND_UP_TARGET] ?: 10,
      roundUpMultiplier = prefs[KEY_ROUND_UP_MULTIPLIER] ?: 1.0,
      autoInvestThreshold = prefs[KEY_AUTO_INVEST_THRESHOLD] ?: 500.0,
      selectedAssetDestination = prefs[KEY_SELECTED_ASSET_DESTINATION] ?: "NIFTY_50_INDEX"
    )
  }

  suspend fun ensureDefaultUser() {
    if (userDao == null) return
    try {
      if (userDao.getUserCount() == 0) {
        val demoUser = UserEntity(
          id = DEFAULT_DEMO_USER_ID,
          email = "arjun.sharma@example.com",
          mobileNumber = "+91 98765 43210",
          fullName = "Arjun Sharma",
          passCodeHash = "1234",
          createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(demoUser)
      }
    } catch (_: Exception) {}
  }

  suspend fun login(
    identifier: String,
    passCode: String
  ): Result<UserSession> {
    val cleanId = identifier.trim().lowercase()
    val cleanPin = passCode.trim()

    if (cleanId.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your email or 10-digit mobile number"))
    }
    if (cleanPin.length < 4) {
      return Result.failure(IllegalArgumentException("Passcode must be at least 4 digits"))
    }

    ensureDefaultUser()

    // 1. Attempt Node.js Express Backend Login with JWT
    try {
      val response = NetworkClient.apiService.login(
        LoginRequest(identifier = cleanId, password = cleanPin)
      )

      if (response.isSuccessful) {
        val authBody = response.body()
        if (authBody != null && authBody.success && !authBody.token.isNullOrBlank()) {
          val userDto = authBody.user
          val userId = userDto?.id ?: DEFAULT_DEMO_USER_ID
          val fullName = userDto?.fullName ?: "Investor"
          val email = userDto?.email ?: cleanId
          val phone = userDto?.phone ?: ""
          val jwtToken = authBody.token

          val target = userDto?.roundUpTarget ?: 10
          val multiplier = userDto?.roundUpMultiplier ?: 1.0
          val threshold = userDto?.autoInvestThreshold ?: 500.0

          // Sync to local Room database for offline resilience
          userDao?.insertUser(
            UserEntity(
              id = userId,
              email = email,
              mobileNumber = phone,
              fullName = fullName,
              passCodeHash = cleanPin,
              createdAt = System.currentTimeMillis()
            )
          )

          val session = UserSession(
            isLoggedIn = true,
            userId = userId,
            fullName = fullName,
            email = email,
            mobileNumber = phone,
            authToken = jwtToken,
            isBackendConnected = true,
            backendSource = "Node.js (JWT)",
            roundUpTarget = target,
            roundUpMultiplier = multiplier,
            autoInvestThreshold = threshold
          )
          saveSessionToDataStore(session)
          return Result.success(session)
        } else {
          val msg = authBody?.message ?: "Invalid credentials"
          return Result.failure(IllegalArgumentException(msg))
        }
      } else {
        val rawError = response.errorBody()?.string()
        val errorMsg = parseErrorMessage(rawError) ?: "Authentication failed: Invalid credentials (HTTP ${response.code()})"
        return Result.failure(IllegalArgumentException(errorMsg))
      }
    } catch (networkEx: Exception) {
      // 2. Offline fallback: Verify against local Room database
      if (userDao != null) {
        val localUser = userDao.getUserByIdentifier(cleanId)
        if (localUser != null) {
          if (localUser.passCodeHash != cleanPin) {
            return Result.failure(IllegalArgumentException("Incorrect 4-digit security PIN. Please try again."))
          }

          val session = UserSession(
            isLoggedIn = true,
            userId = localUser.id,
            fullName = localUser.fullName,
            email = localUser.email,
            mobileNumber = localUser.mobileNumber,
            authToken = "quantiml_local_token_${System.currentTimeMillis()}",
            isBackendConnected = false,
            backendSource = "Local Cache (Offline)"
          )
          saveSessionToDataStore(session)
          return Result.success(session)
        }
      }

      return Result.failure(
        IllegalArgumentException(
          "Backend connection error (${NetworkClient.baseUrl}): ${networkEx.localizedMessage ?: "Server unreachable"}. Check if Node.js server is running on port 5000."
        )
      )
    }
  }

  suspend fun register(
    fullName: String,
    email: String,
    mobileNumber: String,
    passCode: String
  ): Result<UserSession> {
    val cleanName = fullName.trim()
    val cleanEmail = email.trim().lowercase()
    val cleanMobile = mobileNumber.trim()
    val cleanPin = passCode.trim()

    if (cleanName.length < 2) {
      return Result.failure(IllegalArgumentException("Please enter your valid full legal name"))
    }
    if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
      return Result.failure(IllegalArgumentException("Please enter a valid email address (e.g. name@domain.com)"))
    }
    if (cleanMobile.length < 10) {
      return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number"))
    }
    if (cleanPin.length < 4) {
      return Result.failure(IllegalArgumentException("Passcode must be at least 4 digits"))
    }

    ensureDefaultUser()

    try {
      val response = NetworkClient.apiService.register(
        RegisterRequest(
          fullName = cleanName,
          email = cleanEmail,
          phone = cleanMobile,
          password = cleanPin
        )
      )

      if (response.isSuccessful) {
        val authBody = response.body()
        if (authBody != null && authBody.success && !authBody.token.isNullOrBlank()) {
          val userDto = authBody.user
          val userId = userDto?.id ?: "user_${System.currentTimeMillis()}"
          val jwtToken = authBody.token
          val target = userDto?.roundUpTarget ?: 10
          val multiplier = userDto?.roundUpMultiplier ?: 1.0
          val threshold = userDto?.autoInvestThreshold ?: 500.0

          // Cache in local Room DB
          userDao?.insertUser(
            UserEntity(
              id = userId,
              email = cleanEmail,
              mobileNumber = cleanMobile,
              fullName = cleanName,
              passCodeHash = cleanPin,
              createdAt = System.currentTimeMillis()
            )
          )

          val session = UserSession(
            isLoggedIn = true,
            userId = userId,
            fullName = cleanName,
            email = cleanEmail,
            mobileNumber = cleanMobile,
            authToken = jwtToken,
            isBackendConnected = true,
            backendSource = "Node.js (JWT)",
            roundUpTarget = target,
            roundUpMultiplier = multiplier,
            autoInvestThreshold = threshold
          )
          saveSessionToDataStore(session)
          return Result.success(session)
        } else {
          val msg = authBody?.message ?: "Registration failed on server"
          return Result.failure(IllegalArgumentException(msg))
        }
      } else {
        val rawError = response.errorBody()?.string()
        val errorMsg = parseErrorMessage(rawError) ?: "Registration failed (HTTP ${response.code()})"
        return Result.failure(IllegalArgumentException(errorMsg))
      }
    } catch (networkEx: Exception) {
      // Offline fallback to Room database
      if (userDao != null) {
        val existingByEmail = userDao.getUserByEmail(cleanEmail)
        if (existingByEmail != null) {
          return Result.failure(IllegalArgumentException("An account with this email already exists. Please log in."))
        }
        val existingByMobile = userDao.getUserByMobile(cleanMobile)
        if (existingByMobile != null) {
          return Result.failure(IllegalArgumentException("An account with this mobile number already exists. Please log in."))
        }

        val newUserId = "user_${UUID.randomUUID().toString().take(8)}"
        userDao.insertUser(
          UserEntity(
            id = newUserId,
            email = cleanEmail,
            mobileNumber = cleanMobile,
            fullName = cleanName,
            passCodeHash = cleanPin,
            createdAt = System.currentTimeMillis()
          )
        )

        val session = UserSession(
          isLoggedIn = true,
          userId = newUserId,
          fullName = cleanName,
          email = cleanEmail,
          mobileNumber = cleanMobile,
          authToken = "quantiml_local_token_${System.currentTimeMillis()}",
          isBackendConnected = false,
          backendSource = "Local Cache (Offline)"
        )
        saveSessionToDataStore(session)
        return Result.success(session)
      }

      return Result.failure(
        IllegalArgumentException(
          "Backend connection error (${NetworkClient.baseUrl}): ${networkEx.localizedMessage ?: "Server unreachable"}"
        )
      )
    }
  }

  suspend fun updateSettings(
    target: Int,
    multiplier: Double,
    threshold: Double,
    destination: String? = null
  ): Result<UserSession> {
    context.dataStore.edit { prefs ->
      prefs[KEY_ROUND_UP_TARGET] = target
      prefs[KEY_ROUND_UP_MULTIPLIER] = multiplier
      prefs[KEY_AUTO_INVEST_THRESHOLD] = threshold
      destination?.let { prefs[KEY_SELECTED_ASSET_DESTINATION] = it }
    }

    try {
      val session = userSessionFlow.first()
      val token = session.authToken
      if (session.isLoggedIn && !token.isNullOrBlank()) {
        NetworkClient.apiService.updateSettings(
          bearerToken = "Bearer $token",
          request = UpdateSettingsRequest(
            roundUpTarget = target,
            roundUpMultiplier = multiplier,
            autoInvestThreshold = threshold,
            selectedAssetDestination = destination ?: session.selectedAssetDestination
          )
        )
      }
    } catch (_: Exception) {}

    return Result.success(userSessionFlow.first())
  }

  suspend fun syncProfileWithBackend(): Result<UserSession> {
    try {
      val session = userSessionFlow.first()
      val token = session.authToken
      if (!session.isLoggedIn || token.isNullOrBlank()) return Result.success(session)

      val response = NetworkClient.apiService.getMe("Bearer $token")
      if (response.isSuccessful) {
        val userDto = response.body()
        if (userDto != null) {
          val updatedSession = session.copy(
            fullName = userDto.fullName.ifBlank { session.fullName },
            email = userDto.email.ifBlank { session.email },
            mobileNumber = userDto.phone.ifBlank { session.mobileNumber },
            roundUpTarget = userDto.roundUpTarget,
            roundUpMultiplier = userDto.roundUpMultiplier,
            autoInvestThreshold = userDto.autoInvestThreshold,
            selectedAssetDestination = userDto.selectedAssetDestination.ifBlank { session.selectedAssetDestination },
            isBackendConnected = true
          )
          saveSessionToDataStore(updatedSession)
          return Result.success(updatedSession)
        }
      }
      return Result.success(session)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  private fun parseErrorMessage(json: String?): String? {
    if (json.isNullOrBlank()) return null
    return try {
      val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
      regex.find(json)?.groupValues?.getOrNull(1)
    } catch (_: Exception) {
      null
    }
  }

  suspend fun saveSessionToDataStore(session: UserSession) {
    context.dataStore.edit { prefs ->
      prefs[KEY_IS_LOGGED_IN] = session.isLoggedIn
      prefs[KEY_USER_ID] = session.userId
      prefs[KEY_FULL_NAME] = session.fullName
      prefs[KEY_EMAIL] = session.email
      prefs[KEY_MOBILE] = session.mobileNumber
      prefs[KEY_AUTH_TOKEN] = session.authToken ?: ""
      prefs[KEY_IS_BACKEND_CONNECTED] = session.isBackendConnected
      prefs[KEY_BACKEND_SOURCE] = session.backendSource
      prefs[KEY_ROUND_UP_TARGET] = session.roundUpTarget
      prefs[KEY_ROUND_UP_MULTIPLIER] = session.roundUpMultiplier
      prefs[KEY_AUTO_INVEST_THRESHOLD] = session.autoInvestThreshold
      prefs[KEY_SELECTED_ASSET_DESTINATION] = session.selectedAssetDestination
    }
  }

  suspend fun logout() {
    context.dataStore.edit { prefs ->
      prefs[KEY_IS_LOGGED_IN] = false
      prefs.remove(KEY_AUTH_TOKEN)
      prefs[KEY_IS_BACKEND_CONNECTED] = false
    }
  }

  fun updateBaseUrl(newUrl: String) {
    NetworkClient.baseUrl = newUrl
    CoroutineScope(Dispatchers.IO).launch {
      context.dataStore.edit { prefs ->
        prefs[KEY_BASE_URL] = NetworkClient.baseUrl
      }
    }
  }

  fun getBaseUrl(): String = NetworkClient.baseUrl
}
