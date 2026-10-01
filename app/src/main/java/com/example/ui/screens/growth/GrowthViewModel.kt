package com.example.ui.screens.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow

data class FundAllocation(
  val name: String,
  val category: String,
  val allocationPercent: Int,
  val annualizedReturn: Double,
  val rating: Int = 5,
  val currentValuation: Double = 0.0
)

data class GrowthUiState(
  val currentPortfolioValue: Double = 2350.0,
  val totalInvested: Double = 2000.0,
  val totalGain: Double = 350.0,
  val percentageGain: Double = 17.5,
  val monthlyRoundUpProjection: Double = 850.0,
  val projectionYears: Int = 5,
  val expectedAnnualReturn: Double = 14.0, // Indian Nifty 50 / Large Cap average
  val futureProjectedWealth: Double = 0.0,
  val futureTotalInvested: Double = 0.0,
  val futureWealthGain: Double = 0.0,
  val allocations: List<FundAllocation> = emptyList(),
  val riskProfile: String = "Balanced Wealth Builder"
)

class GrowthViewModel(private val repository: TransactionRepository) : ViewModel() {

  private val _projectionYears = MutableStateFlow(5)
  val projectionYears: StateFlow<Int> = _projectionYears.asStateFlow()

  private val _monthlySavingsInput = MutableStateFlow(850.0)
  val monthlySavingsInput: StateFlow<Double> = _monthlySavingsInput.asStateFlow()

  private val _expectedAnnualReturn = MutableStateFlow(14.0)
  val expectedAnnualReturn: StateFlow<Double> = _expectedAnnualReturn.asStateFlow()

  val uiState: StateFlow<GrowthUiState> = combine(
    repository.totalInvested,
    _projectionYears,
    _monthlySavingsInput,
    _expectedAnnualReturn
  ) { investedFromDb, years, monthlySavings, annualRate ->
    val invested = (investedFromDb ?: 2000.0).coerceAtLeast(500.0)
    val gainPct = 17.5
    val currentPortfolio = invested * (1.0 + (gainPct / 100.0))
    val totalGain = currentPortfolio - invested

    // Future SIP Projection calculation: FV = P * [((1 + r)^n - 1) / r] * (1 + r)
    // where r = monthly rate, n = total months
    val monthlyRate = (annualRate / 100.0) / 12.0
    val months = years * 12
    val futureSipValue = if (monthlyRate > 0) {
      monthlySavings * (((1.0 + monthlyRate).pow(months) - 1.0) / monthlyRate) * (1.0 + monthlyRate)
    } else {
      monthlySavings * months
    }

    // Include existing invested compounding
    val existingCompounded = currentPortfolio * (1.0 + (annualRate / 100.0)).pow(years)
    val totalFutureProjected = existingCompounded + futureSipValue
    val totalFutureInvested = invested + (monthlySavings * months)
    val totalFutureGain = totalFutureProjected - totalFutureInvested

    val allocations = listOf(
      FundAllocation(
        name = "Nifty 50 Index Fund",
        category = "Large Cap Index",
        allocationPercent = 50,
        annualizedReturn = 14.8,
        rating = 5,
        currentValuation = currentPortfolio * 0.50
      ),
      FundAllocation(
        name = "Parag Parikh Flexi Cap",
        category = "Flexi Cap Equity",
        allocationPercent = 30,
        annualizedReturn = 18.2,
        rating = 5,
        currentValuation = currentPortfolio * 0.30
      ),
      FundAllocation(
        name = "ICICI Prudential Liquid Fund",
        category = "Low Volatility Debt",
        allocationPercent = 20,
        annualizedReturn = 7.1,
        rating = 4,
        currentValuation = currentPortfolio * 0.20
      )
    )

    GrowthUiState(
      currentPortfolioValue = currentPortfolio,
      totalInvested = invested,
      totalGain = totalGain,
      percentageGain = gainPct,
      monthlyRoundUpProjection = monthlySavings,
      projectionYears = years,
      expectedAnnualReturn = annualRate,
      futureProjectedWealth = totalFutureProjected,
      futureTotalInvested = totalFutureInvested,
      futureWealthGain = totalFutureGain,
      allocations = allocations,
      riskProfile = "Balanced Wealth Builder"
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = GrowthUiState()
  )

  fun setProjectionYears(years: Int) {
    _projectionYears.value = years
  }

  fun updateMonthlySavings(amount: Double) {
    _monthlySavingsInput.value = amount
  }

  fun updateExpectedReturn(rate: Double) {
    _expectedAnnualReturn.value = rate
  }
}
