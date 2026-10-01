package com.example.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TransactionEntity
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlCard
import com.example.ui.components.QuantimlTextField
import com.example.ui.components.SleekBottomNav
import com.example.ui.components.SleekTab
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlTheme
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 5: Transactions History Screen
 * - Search bar with instant filter
 * - Category filter chips (All, Discretionary, Essential, In Wallet, Invested)
 * - Financial summary header (Total Spent, Total Saved via Round-Ups, Discretionary Ratio)
 * - Clean transaction card list with detailed bottom sheet drill-down
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  viewModel: HistoryViewModel,
  onNavigateBack: () -> Unit = {},
  onNavigateToHome: () -> Unit = {},
  onNavigateToGrowth: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  onFabClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }
  val spacing = QuantimlTheme.spacing

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("history_screen"),
    containerColor = MistGrey,
    bottomBar = {
      SleekBottomNav(
        selectedTab = SleekTab.History,
        onTabSelected = { tab ->
          when (tab) {
            SleekTab.Home -> onNavigateToHome()
            SleekTab.History -> { /* Already here */ }
            SleekTab.Growth -> onNavigateToGrowth()
            SleekTab.Profile -> onNavigateToProfile()
          }
        },
        onFabClick = onFabClick
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = spacing.m),
      verticalArrangement = Arrangement.spacedBy(spacing.s)
    ) {
      item { Spacer(modifier = Modifier.height(spacing.xxs)) }

      // Top Bar Title with Back Navigation and Export Action
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs)
            .testTag("history_header_row"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag("history_back_btn")
            ) {
              Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = InkNavy)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = InkNavy,
                  fontSize = 20.sp
                )
              )
              Text(
                text = "${uiState.filteredTransactions.size} transactions tracked",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontSize = 11.sp
                )
              )
            }
          }

          IconButton(
            onClick = { viewModel.refreshFromBackend() },
            modifier = Modifier.testTag("history_refresh_btn")
          ) {
            Icon(
              Icons.Rounded.Refresh,
              contentDescription = "Sync from Node.js Backend",
              tint = if (uiState.isRefreshing) SignalBlue else InkNavy
            )
          }
        }
      }

      // Search Bar
      item {
        QuantimlTextField(
          value = uiState.searchQuery,
          onValueChange = { viewModel.updateSearchQuery(it) },
          placeholder = "Search merchants, cafes, groceries...",
          leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondaryLight)
          },
          trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
            {
              IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextSecondaryLight)
              }
            }
          } else null,
          testTag = "history_search_input"
        )
      }

      // Filter Chips Scrollable Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TransactionFilter.values().forEach { filter ->
            val isSelected = uiState.selectedFilter == filter
            Surface(
              onClick = { viewModel.setFilter(filter) },
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) InkNavy else PureWhite,
              border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
              modifier = Modifier.testTag("filter_chip_${filter.name}")
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = filter.displayName,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) PureWhite else InkNavy,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                  )
                )
              }
            }
          }
        }
      }

      // Metric Strip: Total Spent vs Total Saved Round-Ups
      item {
        QuantimlCard(
          shape = RoundedCornerShape(16.dp),
          testTag = "history_summary_strip"
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL SPENT",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              FinancialText(
                amount = uiState.totalSpent,
                size = FinancialSize.Headline,
                showDecimals = false,
                customColor = InkNavy
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(BorderGrey)
            )

            Column {
              Text(
                text = "ROUND-UPS GENERATED",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              FinancialText(
                amount = uiState.totalSaved,
                size = FinancialSize.Headline,
                isPositiveDelta = true,
                customColor = GrowthGreen
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(BorderGrey)
            )

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "DISC. RATIO",
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${uiState.discretionaryPercentage}%",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = SignalBlue,
                  fontSize = 18.sp
                )
              )
            }
          }
        }
      }

      // Transactions List
      if (uiState.filteredTransactions.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No matching transactions found",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontWeight = FontWeight.Medium
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try clearing the search or switching filter chips",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = InkNavy.copy(alpha = 0.35f),
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      } else {
        items(uiState.filteredTransactions, key = { it.id }) { txn ->
          HistoryTransactionRow(
            transaction = txn,
            onClick = { selectedTransactionForDetails = txn }
          )
        }
      }

      item { Spacer(modifier = Modifier.height(spacing.l)) }
    }
  }

  // Drill-down Transaction Detail Sheet
  selectedTransactionForDetails?.let { txn ->
    TransactionDetailBottomSheet(
      transaction = txn,
      onDismiss = { selectedTransactionForDetails = null }
    )
  }
}

@Composable
private fun HistoryTransactionRow(
  transaction: TransactionEntity,
  onClick: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
  val formattedDateTime = remember(transaction.date) { dateFormat.format(Date(transaction.date)) }
  val isDiscretionary = transaction.category == "DISCRETIONARY"

  val iconEmoji = when {
    transaction.merchant.contains("Coffee", ignoreCase = true) || transaction.merchant.contains("Starbucks", ignoreCase = true) -> "☕"
    transaction.merchant.contains("Zomato", ignoreCase = true) || transaction.merchant.contains("Swiggy", ignoreCase = true) -> "🍕"
    transaction.merchant.contains("Blinkit", ignoreCase = true) || transaction.merchant.contains("Grocery", ignoreCase = true) -> "🥦"
    transaction.merchant.contains("Pharmacy", ignoreCase = true) || transaction.merchant.contains("Apollo", ignoreCase = true) -> "💊"
    transaction.merchant.contains("Movie", ignoreCase = true) || transaction.merchant.contains("BookMyShow", ignoreCase = true) -> "🎬"
    transaction.merchant.contains("Uber", ignoreCase = true) -> "🚗"
    transaction.merchant.contains("Gym", ignoreCase = true) || transaction.merchant.contains("Cult", ignoreCase = true) -> "💪"
    else -> "🛍️"
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = PureWhite,
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .testTag("history_txn_item_${transaction.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MistGrey),
        contentAlignment = Alignment.Center
      ) {
        Text(text = iconEmoji, fontSize = 18.sp)
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = transaction.merchant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = InkNavy,
              fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f, fill = false)
          )
          com.example.ui.components.CategoryBadge(isDiscretionary = isDiscretionary)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Spent ₹${String.format(Locale.US, "%.0f", transaction.amount)} • $formattedDateTime",
          maxLines = 1,
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy.copy(alpha = 0.45f),
            fontSize = 11.sp
          )
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        FinancialText(
          amount = transaction.roundUpAmount,
          size = FinancialSize.Body,
          isPositiveDelta = true,
          customColor = GrowthGreen
        )
        Text(
          text = if (transaction.status == "INVESTED") "Invested" else "In Wallet",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (transaction.status == "INVESTED") GrowthGreen else InkNavy.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailBottomSheet(
  transaction: TransactionEntity,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy 'at' hh:mm a", Locale.getDefault()) }
  val formattedFullDate = remember(transaction.date) { dateFormat.format(Date(transaction.date)) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transaction Breakdown",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy
          )
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Rounded.Close, contentDescription = "Close")
        }
      }

      // Breakdown Card
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MistGrey,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          DetailRow(label = "Merchant", value = transaction.merchant)
          DetailRow(label = "Transaction Amount", value = "₹${String.format(Locale.US, "%.2f", transaction.amount)}")
          DetailRow(label = "Classification", value = transaction.category)
          DetailRow(
            label = "Round-Up Spare Change",
            value = "+₹${String.format(Locale.US, "%.2f", transaction.roundUpAmount)}",
            valueColor = GrowthGreen
          )
          DetailRow(
            label = "Deployment Status",
            value = if (transaction.status == "INVESTED") "Deployed into Mutual Funds" else "Accumulating in Wallet"
          )
          DetailRow(label = "Timestamp", value = formattedFullDate)
          DetailRow(label = "Transaction ID", value = transaction.id.take(16))
        }
      }

      // Micro-explanation pill
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SignalBlue.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Rounded.Info, contentDescription = null, tint = SignalBlue, modifier = Modifier.size(20.dp))
          Text(
            text = "QuantAI analyzed this transaction and rounded it to the next multiple of ₹10, depositing spare change into your mutual fund threshold wallet.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = InkNavy,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String,
  valueColor: Color = InkNavy
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(
        color = InkNavy.copy(alpha = 0.55f),
        fontSize = 12.sp
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        color = valueColor,
        fontSize = 13.sp
      )
    )
  }
}
