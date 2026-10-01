package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
  val userSession: UserSession = UserSession(),
  val roundUpTarget: Int = 10, // Round to ₹10 or ₹50
  val multiplier: Double = 1.0, // 1.0x, 1.5x, 2.0x
  val autoInvestThreshold: Double = 500.0, // ₹200, ₹500, ₹1000, ₹2000
  val selectedAssetDestination: String = "NIFTY_50_INDEX", // NIFTY_50_INDEX, DIGITAL_GOLD, FLEXI_CAP
  val isSmsPermissionGranted: Boolean = true,
  val isBiometricEnabled: Boolean = true,
  val linkedUpiId: String = "arjun.sharma@okhdfcbank",
  val panNumberMasked: String = "ABCDE****F",
  val kycStatus: String = "VERIFIED (SEBI Compliant)",
  val currentBaseUrl: String = "http://10.0.2.2:5000/",
  val isSettingsSynced: Boolean = true
)

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

  private val _isBiometricEnabled = MutableStateFlow(true)
  val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

  private val _currentBaseUrl = MutableStateFlow(authRepository.getBaseUrl())
  val currentBaseUrl: StateFlow<String> = _currentBaseUrl.asStateFlow()

  private val _isSettingsSynced = MutableStateFlow(true)
  val isSettingsSynced: StateFlow<Boolean> = _isSettingsSynced.asStateFlow()

  val uiState: StateFlow<ProfileUiState> = combine(
    authRepository.userSessionFlow,
    _isBiometricEnabled,
    _currentBaseUrl
  ) { session: UserSession, biometric: Boolean, baseUrl: String ->
    ProfileUiState(
      userSession = session,
      roundUpTarget = session.roundUpTarget,
      multiplier = session.roundUpMultiplier,
      autoInvestThreshold = session.autoInvestThreshold,
      selectedAssetDestination = session.selectedAssetDestination,
      isBiometricEnabled = biometric,
      linkedUpiId = if (session.email.isNotBlank()) "${session.email.substringBefore("@")}@okhdfcbank" else "arjun.sharma@okhdfcbank",
      currentBaseUrl = baseUrl,
      isSettingsSynced = _isSettingsSynced.value
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProfileUiState()
  )

  init {
    viewModelScope.launch {
      _currentBaseUrl.value = authRepository.getBaseUrl()
      authRepository.syncProfileWithBackend()
    }
  }

  fun setRoundUpTarget(target: Int) {
    viewModelScope.launch {
      _isSettingsSynced.value = false
      val current = uiState.value
      authRepository.updateSettings(
        target = target,
        multiplier = current.multiplier,
        threshold = current.autoInvestThreshold,
        destination = current.selectedAssetDestination
      )
      _isSettingsSynced.value = true
    }
  }

  fun setMultiplier(mult: Double) {
    viewModelScope.launch {
      _isSettingsSynced.value = false
      val current = uiState.value
      authRepository.updateSettings(
        target = current.roundUpTarget,
        multiplier = mult,
        threshold = current.autoInvestThreshold,
        destination = current.selectedAssetDestination
      )
      _isSettingsSynced.value = true
    }
  }

  fun setAutoInvestThreshold(threshold: Double) {
    viewModelScope.launch {
      _isSettingsSynced.value = false
      val current = uiState.value
      authRepository.updateSettings(
        target = current.roundUpTarget,
        multiplier = current.multiplier,
        threshold = threshold,
        destination = current.selectedAssetDestination
      )
      _isSettingsSynced.value = true
    }
  }

  fun setAssetDestination(destination: String) {
    viewModelScope.launch {
      _isSettingsSynced.value = false
      val current = uiState.value
      authRepository.updateSettings(
        target = current.roundUpTarget,
        multiplier = current.multiplier,
        threshold = current.autoInvestThreshold,
        destination = destination
      )
      _isSettingsSynced.value = true
    }
  }

  fun updateBaseUrl(newUrl: String) {
    authRepository.updateBaseUrl(newUrl)
    _currentBaseUrl.value = authRepository.getBaseUrl()
  }

  fun toggleBiometric(enabled: Boolean) {
    _isBiometricEnabled.value = enabled
  }

  fun logout(onLogoutSuccess: () -> Unit) {
    viewModelScope.launch {
      authRepository.logout()
      onLogoutSuccess()
    }
  }
}
