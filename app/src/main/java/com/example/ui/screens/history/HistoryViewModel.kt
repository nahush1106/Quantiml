package com.example.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.TransactionEntity
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TransactionFilter(val displayName: String) {
  ALL("All"),
  DISCRETIONARY("Discretionary"),
  ESSENTIAL("Essential"),
  ROUNDED_UP("In Wallet"),
  INVESTED("Invested")
}

data class HistoryUiState(
  val transactions: List<TransactionEntity> = emptyList(),
  val filteredTransactions: List<TransactionEntity> = emptyList(),
  val selectedFilter: TransactionFilter = TransactionFilter.ALL,
  val searchQuery: String = "",
  val totalSaved: Double = 0.0,
  val totalSpent: Double = 0.0,
  val discretionaryPercentage: Int = 0,
  val isRefreshing: Boolean = false
)

class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {

  private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
  val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  val uiState: StateFlow<HistoryUiState> = combine(
    repository.allTransactions,
    _selectedFilter,
    _searchQuery,
    _isRefreshing
  ) { allTxns, filter, query, refreshing ->
    val filtered = allTxns.filter { txn ->
      val matchesQuery = query.isBlank() ||
          txn.merchant.contains(query, ignoreCase = true) ||
          txn.category.contains(query, ignoreCase = true)

      val matchesFilter = when (filter) {
        TransactionFilter.ALL -> true
        TransactionFilter.DISCRETIONARY -> txn.category == "DISCRETIONARY"
        TransactionFilter.ESSENTIAL -> txn.category == "ESSENTIAL"
        TransactionFilter.ROUNDED_UP -> txn.status == "ROUNDED_UP"
        TransactionFilter.INVESTED -> txn.status == "INVESTED"
      }

      matchesQuery && matchesFilter
    }

    val totalSaved = allTxns.sumOf { it.roundUpAmount }
    val totalSpent = allTxns.sumOf { it.amount }
    val discSpend = allTxns.filter { it.category == "DISCRETIONARY" }.sumOf { it.amount }
    val discPct = if (totalSpent > 0) ((discSpend / totalSpent) * 100).toInt() else 0

    HistoryUiState(
      transactions = allTxns,
      filteredTransactions = filtered,
      selectedFilter = filter,
      searchQuery = query,
      totalSaved = totalSaved,
      totalSpent = totalSpent,
      discretionaryPercentage = discPct,
      isRefreshing = refreshing
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = HistoryUiState()
  )

  fun setFilter(filter: TransactionFilter) {
    _selectedFilter.value = filter
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun refreshFromBackend() {
    viewModelScope.launch {
      _isRefreshing.value = true
      try {
        repository.syncTransactionsFromBackend()
      } catch (_: Exception) {
      } finally {
        _isRefreshing.value = false
      }
    }
  }
}
