package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TransactionEntity
import com.example.ui.components.ButtonVariant
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlButton
import com.example.ui.components.QuantimlCard
import com.example.ui.components.QuantimlHeroCard
import com.example.ui.components.QuantimlInsightCard
import com.example.ui.components.QuantimlTextField
import com.example.ui.components.SleekBottomNav
import com.example.ui.components.SleekTab
import com.example.ui.theme.AmberInsight
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
 * Phase 4: Core Micro-Investment Engine & Home Dashboard Screen
 * High-fidelity Sleek Interface theme matching design mandates:
 * - Header with User Greeting and Initial Avatar
 * - Spare Change Wallet Hero Card with live progress bar toward ₹500 threshold and "Auto-Deploy" trigger
 * - 2-Column Metrics: Total Invested & Portfolio Value with dynamic percentage gain
 * - QuantAI Dynamic Insight Card
 * - Recent Transactions list with NLP Discretionary / Essential categorization badges and live round-ups
 * - Interactive Modal Sheet to simulate real-time UPI purchases (e.g. Starbucks ₹97 -> ₹3 saved)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  onNavigateToHistory: () -> Unit = {},
  onNavigateToGrowth: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  onViewThresholdDetails: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current
  var showAddSpendSheet by remember { mutableStateOf(false) }
  var showBankSmsSheet by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableStateOf(SleekTab.Home) }
  val spacing = QuantimlTheme.spacing

  androidx.compose.runtime.LaunchedEffect(Unit) {
    viewModel.refreshDashboard(context)
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("home_screen"),
    containerColor = MistGrey,
    bottomBar = {
      SleekBottomNav(
        selectedTab = selectedTab,
        onTabSelected = { tab ->
          selectedTab = tab
          when (tab) {
            SleekTab.Home -> { /* Stay on Home */ }
            SleekTab.History -> onNavigateToHistory()
            SleekTab.Growth -> onNavigateToGrowth()
            SleekTab.Profile -> onNavigateToProfile()
          }
        },
        onFabClick = { showAddSpendSheet = true }
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = spacing.m),
      verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
      item { Spacer(modifier = Modifier.height(spacing.xxs)) }

      // 1. Sleek Top Header
      item {
        HomeHeaderSection(
          userName = uiState.userName,
          onProfileClick = onNavigateToProfile
        )
      }

      // 2. Hero Spare Change Wallet Card
      item {
        HomeWalletHeroCard(
          walletBalance = uiState.walletBalance,
          investThreshold = uiState.investThreshold,
          isThresholdReached = uiState.isThresholdReached,
          onDeployClick = { viewModel.deployWalletNow() },
          onViewDetailsClick = onViewThresholdDetails
        )
      }

      // 3. 2-Column Side-by-Side Financial Metrics
      item {
        HomeMetricsSection(
          totalInvested = uiState.totalInvested,
          portfolioValue = uiState.portfolioValue,
          percentageGain = uiState.percentageGain
        )
      }

      // 4. QuantAI Smart Insight
      item {
        QuantimlInsightCard(
          title = uiState.aiInsightTitle,
          insightText = uiState.aiInsightBody,
          testTag = "home_ai_insight_card"
        )
      }

      // 5. Bank SMS Auto-Tracking & Spare Change Ingestion
      item {
        HomeBankSmsAutoTrackCard(
          onOpenSmsScanner = { showBankSmsSheet = true }
        )
      }

      // 5. Recent Activity Section Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium.copy(
              color = InkNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          )

          Text(
            text = "View All",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SignalBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            ),
            modifier = Modifier
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNavigateToHistory
              )
              .testTag("home_view_all_transactions_btn")
          )
        }
      }

      // 6. Recent Transaction Items
      items(uiState.recentTransactions, key = { it.id }) { txn ->
        HomeTransactionItem(transaction = txn)
      }

      item { Spacer(modifier = Modifier.height(spacing.l)) }
    }
  }

  // Quick Add / Simulate Spend Sheet
  if (showAddSpendSheet) {
    SimulateSpendBottomSheet(
      onDismiss = { showAddSpendSheet = false },
      onSimulateSpend = { merchant, amount, roundTo, multiplier ->
        viewModel.simulateNewSpend(merchant, amount, roundTo, multiplier)
        showAddSpendSheet = false
      }
    )
  }

  // Bank SMS Scanner & Auto-Detection Simulator Sheet
  if (showBankSmsSheet) {
    BankSmsBottomSheet(
      onDismiss = { showBankSmsSheet = false },
      activeRoundUpTarget = uiState.roundUpTarget,
      activeMultiplier = uiState.roundUpMultiplier,
      onProcessSmsTransaction = { merchant, amount, rawSms ->
        viewModel.processBankSmsTransaction(merchant, amount, rawSms)
      }
    )
  }
}

@Composable
private fun HomeBankSmsAutoTrackCard(
  onOpenSmsScanner: () -> Unit
) {
  QuantimlCard(
    shape = RoundedCornerShape(16.dp),
    testTag = "home_bank_sms_autotrack_card"
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SignalBlue.copy(alpha = 0.1f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Rounded.Sms,
            contentDescription = null,
            tint = SignalBlue,
            modifier = Modifier.size(22.dp)
          )
        }

        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Bank SMS Auto-Track",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = InkNavy,
                fontSize = 15.sp
              )
            )
            QuantimlBadge(
              text = "ACTIVE",
              textColor = GrowthGreen,
              backgroundColor = GrowthGreen.copy(alpha = 0.12f)
            )
          }

          Text(
            text = "Auto-detect debit SMS from HDFC, SBI, ICICI & UPI",
            style = MaterialTheme.typography.bodySmall.copy(
              color = InkNavy.copy(alpha = 0.5f),
              fontSize = 11.sp
            )
          )
        }
      }

      Surface(
        onClick = onOpenSmsScanner,
        shape = RoundedCornerShape(10.dp),
        color = InkNavy,
        modifier = Modifier.testTag("open_sms_scanner_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Test / Sync",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun HomeHeaderSection(
  userName: String,
  onProfileClick: () -> Unit
) {
  val spacing = QuantimlTheme.spacing
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = spacing.xs)
      .testTag("home_header_section"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "GOOD MORNING,",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = InkNavy.copy(alpha = 0.5f),
          letterSpacing = 1.2.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = userName,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = InkNavy,
          fontSize = 24.sp
        )
      )
    }

    // Avatar Box
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(InkNavy)
        .border(2.dp, PureWhite, RoundedCornerShape(16.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onProfileClick
        )
        .testTag("home_avatar_box"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "AS",
        style = MaterialTheme.typography.titleMedium.copy(
          color = PureWhite,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      )
    }
  }
}

@Composable
private fun HomeWalletHeroCard(
  walletBalance: Double,
  investThreshold: Double,
  isThresholdReached: Boolean,
  onDeployClick: () -> Unit,
  onViewDetailsClick: () -> Unit
) {
  val spacing = QuantimlTheme.spacing
  val progressPercent = ((walletBalance / investThreshold) * 100).coerceIn(0.0, 100.0).toInt()

  QuantimlHeroCard(
    testTag = "home_hero_wallet_card"
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "SPARE CHANGE WALLET",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PureWhite.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
          )
        )
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(GrowthGreen)
        )
      }

      if (isThresholdReached) {
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(GrowthGreen.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "READY TO INVEST",
            style = MaterialTheme.typography.labelSmall.copy(
              color = GrowthGreen,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(spacing.xs))

    FinancialText(
      amount = walletBalance,
      size = FinancialSize.Display,
      customColor = PureWhite
    )

    Spacer(modifier = Modifier.height(spacing.m))

    // Threshold Progress and Action Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "INVEST THRESHOLD",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PureWhite.copy(alpha = 0.6f),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          )
          Text(
            text = "₹${walletBalance.toInt()} / ₹${investThreshold.toInt()} ($progressPercent%)",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PureWhite,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(end = 16.dp)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress track bar
        Box(
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(6.dp)
            .clip(CircleShape)
            .background(PureWhite.copy(alpha = 0.2f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(progressPercent / 100f)
              .height(6.dp)
              .clip(CircleShape)
              .background(if (isThresholdReached) GrowthGreen else PureWhite)
          )
        }
      }

      Surface(
        onClick = if (isThresholdReached) onDeployClick else onViewDetailsClick,
        shape = RoundedCornerShape(12.dp),
        color = PureWhite,
        modifier = Modifier.height(34.dp)
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isThresholdReached) "Deploy ₹${walletBalance.toInt()}" else "View Details",
            style = MaterialTheme.typography.labelMedium.copy(
              color = SignalBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun HomeMetricsSection(
  totalInvested: Double,
  portfolioValue: Double,
  percentageGain: Double
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("home_metrics_section"),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Total Invested Card
    QuantimlCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(20.dp),
      testTag = "home_total_invested_card"
    ) {
      Text(
        text = "TOTAL INVESTED",
        style = MaterialTheme.typography.labelSmall.copy(
          color = InkNavy.copy(alpha = 0.5f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      FinancialText(
        amount = totalInvested,
        size = FinancialSize.Title,
        showDecimals = false,
        customColor = InkNavy
      )
    }

    // Portfolio Value Card
    QuantimlCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(20.dp),
      testTag = "home_portfolio_value_card"
    ) {
      Text(
        text = "PORTFOLIO VALUE",
        style = MaterialTheme.typography.labelSmall.copy(
          color = InkNavy.copy(alpha = 0.5f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FinancialText(
          amount = portfolioValue,
          size = FinancialSize.Title,
          showDecimals = false,
          customColor = InkNavy
        )
        Text(
          text = "+${String.format(Locale.US, "%.1f", percentageGain)}%",
          style = MaterialTheme.typography.labelSmall.copy(
            color = GrowthGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
      }
    }
  }
}

@Composable
private fun HomeTransactionItem(
  transaction: TransactionEntity
) {
  val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val formattedTime = remember(transaction.date) { dateFormat.format(Date(transaction.date)) }

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
      .testTag("home_transaction_item_${transaction.id}")
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
          if (!transaction.rawSmsBody.isNullOrBlank()) {
            QuantimlBadge(
              text = "SMS",
              textColor = SignalBlue,
              backgroundColor = SignalBlue.copy(alpha = 0.12f)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Spent ₹${String.format(Locale.US, "%.0f", transaction.amount)} • $formattedTime",
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

/**
 * Bottom Sheet allowing users to simulate UPI debit payments
 * and witness the real-time QuantAI classification & round-up calculation in action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimulateSpendBottomSheet(
  onDismiss: () -> Unit,
  onSimulateSpend: (merchant: String, amount: Double, roundTo: Int, multiplier: Double) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var merchantName by remember { mutableStateOf("Third Wave Coffee") }
  var spendAmount by remember { mutableStateOf("193") }
  var roundToMultiple by remember { mutableStateOf(10) }
  var multiplier by remember { mutableStateOf(1.0) }

  val spacing = QuantimlTheme.spacing

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
          text = "Simulate New Purchase",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy
          )
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Rounded.Close, contentDescription = "Close")
        }
      }

      Text(
        text = "Enter a merchant and amount to see the QuantAI NLP classification and round-up mechanics calculate live.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
      )

      // Merchant Input
      QuantimlTextField(
        value = merchantName,
        onValueChange = { merchantName = it },
        label = "Merchant Name",
        placeholder = "e.g. Starbucks, Blinkit, Apollo Pharmacy",
        leadingIcon = {
          Icon(Icons.Rounded.Store, contentDescription = null, tint = TextSecondaryLight)
        },
        testTag = "simulate_merchant_input"
      )

      // Amount Input
      QuantimlTextField(
        value = spendAmount,
        onValueChange = { spendAmount = it },
        label = "Transaction Amount (₹)",
        placeholder = "e.g. 193",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = {
          Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = TextSecondaryLight)
        },
        testTag = "simulate_amount_input"
      )

      // Multiplier Options
      Column {
        Text(
          text = "Round-Up Multiplier",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
          )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(1.0 to "1x Standard", 1.5 to "1.5x Boost", 2.0 to "2x Double").forEach { (valMult, label) ->
            val isSelected = multiplier == valMult
            Surface(
              onClick = { multiplier = valMult },
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) SignalBlue else MistGrey,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) PureWhite else InkNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      QuantimlButton(
        text = "Execute Purchase & Round-Up",
        onClick = {
          val parsedAmount = spendAmount.toDoubleOrNull() ?: 95.0
          onSimulateSpend(merchantName.trim(), parsedAmount, roundToMultiple, multiplier)
        },
        variant = ButtonVariant.Growth,
        testTag = "simulate_execute_btn"
      )
    }
  }
}
