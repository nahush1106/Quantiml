package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.UserSession
import com.example.data.database.TransactionEntity
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeDashboardUiState(
  val userName: String = "Arjun Sharma",
  val walletBalance: Double = 350.0,
  val totalInvested: Double = 2000.0,
  val portfolioValue: Double = 2350.0,
  val percentageGain: Double = 6.2,
  val investThreshold: Double = 500.0,
  val roundUpTarget: Int = 10,
  val roundUpMultiplier: Double = 1.0,
  val recentTransactions: List<TransactionEntity> = emptyList(),
  val isThresholdReached: Boolean = false,
  val aiInsightTitle: String = "Smart AI Insight",
  val aiInsightBody: String = "Reducing food delivery by 20% could increase your monthly investment by ₹400.",
  val isSyncing: Boolean = false
)

private data class FinancialOverview(
  val transactions: List<TransactionEntity>,
  val wallet: Double,
  val invested: Double
)

class HomeViewModel(
  private val repository: TransactionRepository,
  private val authRepository: AuthRepository? = null
) : ViewModel() {

  private val _simulatedGainPercent = MutableStateFlow(6.2)
  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val userSessionFlow = authRepository?.userSessionFlow ?: flowOf(UserSession())

  private val financialDataFlow = combine(
    repository.allTransactions,
    repository.walletBalance,
    repository.totalInvested
  ) { transactions, wallet, invested ->
    FinancialOverview(
      transactions = transactions,
      wallet = wallet ?: 0.0,
      invested = invested ?: 0.0
    )
  }

  val uiState: StateFlow<HomeDashboardUiState> = combine(
    userSessionFlow,
    financialDataFlow,
    _simulatedGainPercent,
    _isSyncing
  ) { session, fin, gainPercent, syncing ->
    val walletVal = fin.wallet
    val investedVal = fin.invested
    val portfolioVal = if (investedVal > 0) investedVal * (1.0 + (gainPercent / 100.0)) else 0.0
    val threshold = if (session.autoInvestThreshold > 0) session.autoInvestThreshold else 500.0

    val discretionaryCount = fin.transactions.take(10).count { it.category == "DISCRETIONARY" }
    val insight = if (discretionaryCount >= 3) {
      Pair(
        "Discretionary Spend Surge",
        "Discretionary dining & shopping detected. Applying micro-savings boosted your wallet by ₹${walletVal.toInt()}."
      )
    } else {
      Pair(
        "Smart AI Insight",
        "Reducing food delivery by 20% could increase your monthly investment by ₹400."
      )
    }

    HomeDashboardUiState(
      userName = session.fullName.ifBlank { "Investor" },
      walletBalance = walletVal,
      totalInvested = investedVal,
      portfolioValue = portfolioVal,
      percentageGain = gainPercent,
      investThreshold = threshold,
      roundUpTarget = session.roundUpTarget,
      roundUpMultiplier = session.roundUpMultiplier,
      recentTransactions = fin.transactions.take(6),
      isThresholdReached = walletVal >= threshold,
      aiInsightTitle = insight.first,
      aiInsightBody = insight.second,
      isSyncing = syncing
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = HomeDashboardUiState()
  )

  init {
    viewModelScope.launch {
      repository.ensureInitialData()
      refreshDashboard()
    }
  }

  fun refreshDashboard(context: android.content.Context? = null) {
    viewModelScope.launch {
      _isSyncing.value = true
      try {
        context?.let { repository.syncFromSmsInbox(it) }
        repository.syncTransactionsFromBackend()
        authRepository?.syncProfileWithBackend()
      } catch (_: Exception) {
      } finally {
        _isSyncing.value = false
      }
    }
  }

  fun simulateNewSpend(
    merchant: String,
    amount: Double,
    roundToMultiple: Int = 10,
    multiplier: Double = 1.0
  ) {
    viewModelScope.launch {
      repository.recordTransaction(
        merchant = merchant,
        amount = amount,
        customMultiplier = multiplier,
        roundToMultiple = roundToMultiple
      )
    }
  }

  fun processBankSmsTransaction(
    merchant: String,
    amount: Double,
    rawSms: String
  ) {
    viewModelScope.launch {
      repository.recordTransaction(
        merchant = merchant,
        amount = amount,
        rawSms = rawSms
      )
    }
  }

  fun deployWalletNow() {
    viewModelScope.launch {
      repository.deployWalletToInvestments()
    }
  }
}
