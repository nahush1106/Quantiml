package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
  data object Idle : AuthUiState
  data object Loading : AuthUiState
  data class Success(val session: UserSession, val message: String = "Authenticated successfully") : AuthUiState
  data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

  private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  private val _backendUrl = MutableStateFlow(authRepository.getBaseUrl())
  val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

  val userSession: StateFlow<UserSession> = authRepository.userSessionFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserSession()
    )

  fun login(identifier: String, passCode: String) {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val result = authRepository.login(identifier, passCode)
      result.fold(
        onSuccess = { session ->
          val sourceMsg = if (session.isBackendConnected) "Authenticated with Node.js JWT" else "Authenticated offline"
          _uiState.value = AuthUiState.Success(session, sourceMsg)
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed")
        }
      )
    }
  }

  fun register(fullName: String, email: String, mobileNumber: String, passCode: String) {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val result = authRepository.register(fullName, email, mobileNumber, passCode)
      result.fold(
        onSuccess = { session ->
          val sourceMsg = if (session.isBackendConnected) "Account created on Node.js (JWT issued)" else "Account created offline"
          _uiState.value = AuthUiState.Success(session, sourceMsg)
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
        }
      )
    }
  }

  fun updateBackendUrl(newUrl: String) {
    val sanitized = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
    authRepository.updateBaseUrl(sanitized)
    _backendUrl.value = sanitized
  }

  fun resetState() {
    _uiState.value = AuthUiState.Idle
  }
}
