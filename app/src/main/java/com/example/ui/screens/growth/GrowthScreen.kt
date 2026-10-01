package com.example.ui.screens.growth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlCard
import com.example.ui.components.QuantimlHeroCard
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
import java.util.Locale

/**
 * Phase 6: Growth & Investment Portfolio Screen
 * - High-end Sleek dark/light cards adhering to FinTech guidelines
 * - Portfolio Valuation & Lifetime Gain
 * - Interactive Custom Canvas Multi-Year Compound Growth Chart (1Y, 3Y, 5Y, 10Y)
 * - Micro-SIP Compound Calculator with real-time sliders (Years & Monthly spare change rate)
 * - SEBI / AMFI compliant underlying Mutual Fund Allocations (Large Cap Index, Flexi Cap, Liquid Fund)
 */
@Composable
fun GrowthScreen(
  viewModel: GrowthViewModel,
  onNavigateToHome: () -> Unit = {},
  onNavigateToHistory: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  onFabClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val spacing = QuantimlTheme.spacing

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("growth_screen"),
    containerColor = MistGrey,
    bottomBar = {
      SleekBottomNav(
        selectedTab = SleekTab.Growth,
        onTabSelected = { tab ->
          when (tab) {
            SleekTab.Home -> onNavigateToHome()
            SleekTab.History -> onNavigateToHistory()
            SleekTab.Growth -> { /* Already on Growth */ }
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
      verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
      item { Spacer(modifier = Modifier.height(spacing.xxs)) }

      // 1. Title Header
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs)
            .testTag("growth_header_section")
        ) {
          Text(
            text = "INVESTMENT PORTFOLIO",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = InkNavy.copy(alpha = 0.5f),
              letterSpacing = 1.2.sp
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Portfolio Growth",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = InkNavy,
                fontSize = 24.sp
              )
            )

            QuantimlBadge(
              text = "AMFI Registered",
              textColor = GrowthGreen,
              backgroundColor = GrowthGreen.copy(alpha = 0.1f)
            )
          }
        }
      }

      // 2. Portfolio Hero Valuation Card
      item {
        GrowthHeroCard(
          portfolioValue = uiState.currentPortfolioValue,
          totalInvested = uiState.totalInvested,
          totalGain = uiState.totalGain,
          percentageGain = uiState.percentageGain
        )
      }

      // 3. Interactive Compound Growth Chart & Projection Simulator
      item {
        CompoundProjectionSection(
          projectionYears = uiState.projectionYears,
          monthlySavings = uiState.monthlyRoundUpProjection,
          projectedTotalWealth = uiState.futureProjectedWealth,
          totalInvested = uiState.futureTotalInvested,
          gainAmount = uiState.futureWealthGain,
          onYearsChanged = { viewModel.setProjectionYears(it) },
          onMonthlySavingsChanged = { viewModel.updateMonthlySavings(it) }
        )
      }

      // 4. Asset Allocation Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Fund Allocation Breakdown",
            style = MaterialTheme.typography.titleMedium.copy(
              color = InkNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          )
          Text(
            text = "3 Curated Funds",
            style = MaterialTheme.typography.labelSmall.copy(
              color = InkNavy.copy(alpha = 0.5f),
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          )
        }
      }

      // 5. Fund Allocation Items
      items(uiState.allocations) { fund ->
        FundAllocationCard(fund = fund)
      }

      // 6. Regulatory & Security Trust Footer
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              Icons.Rounded.Security,
              contentDescription = null,
              tint = SignalBlue,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Investments are routed strictly through SEBI regulated mutual fund houses (BSE Star MF / NSE NMF II). Your money is held in custody with top-tier Indian AMCs.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.7f),
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(spacing.l)) }
    }
  }
}

@Composable
private fun GrowthHeroCard(
  portfolioValue: Double,
  totalInvested: Double,
  totalGain: Double,
  percentageGain: Double
) {
  val spacing = QuantimlTheme.spacing
  QuantimlHeroCard(testTag = "growth_hero_portfolio_card") {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "CURRENT PORTFOLIO VALUE",
        style = MaterialTheme.typography.labelSmall.copy(
          color = PureWhite.copy(alpha = 0.75f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.1.sp
        )
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          Icons.Rounded.TrendingUp,
          contentDescription = null,
          tint = GrowthGreen,
          modifier = Modifier.size(14.dp)
        )
        Text(
          text = "+${String.format(Locale.US, "%.1f", percentageGain)}% Overall",
          style = MaterialTheme.typography.labelSmall.copy(
            color = GrowthGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(spacing.xs))

    FinancialText(
      amount = portfolioValue,
      size = FinancialSize.Display,
      customColor = PureWhite
    )

    Spacer(modifier = Modifier.height(spacing.m))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "INVESTED CAPITAL",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PureWhite.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        FinancialText(
          amount = totalInvested,
          size = FinancialSize.Title,
          showDecimals = false,
          customColor = PureWhite
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "TOTAL RETURNS",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PureWhite.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        FinancialText(
          amount = totalGain,
          size = FinancialSize.Title,
          isPositiveDelta = true,
          showDecimals = false,
          customColor = GrowthGreen
        )
      }
    }
  }
}

@Composable
private fun CompoundProjectionSection(
  projectionYears: Int,
  monthlySavings: Double,
  projectedTotalWealth: Double,
  totalInvested: Double,
  gainAmount: Double,
  onYearsChanged: (Int) -> Unit,
  onMonthlySavingsChanged: (Double) -> Unit
) {
  val yearsOptions = listOf(1, 3, 5, 10)

  QuantimlCard(
    shape = RoundedCornerShape(20.dp),
    testTag = "compound_projection_card"
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Micro-SIP Projection",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy,
            fontSize = 16.sp
          )
        )
        Text(
          text = "At ~14% historical equity index returns",
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy.copy(alpha = 0.5f),
            fontSize = 11.sp
          )
        )
      }

      // Year selector tabs
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        yearsOptions.forEach { year ->
          val isSelected = projectionYears == year
          Surface(
            onClick = { onYearsChanged(year) },
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) InkNavy else MistGrey,
            modifier = Modifier.testTag("year_chip_${year}y")
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${year}Y",
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

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Projection Chart Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .testTag("growth_projection_canvas_box")
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background subtle grid lines
        val lineCount = 3
        for (i in 1..lineCount) {
          val y = height * (i / (lineCount + 1).toFloat())
          drawLine(
            color = Color(0xFFE5E9F0),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
          )
        }

        // Draw compound growth curve path
        val path = Path()
        val fillPath = Path()

        val startX = 0f
        val startY = height * 0.85f
        val midX = width * 0.5f
        val midY = height * 0.55f
        val endX = width
        val endY = height * 0.15f

        path.moveTo(startX, startY)
        path.cubicTo(
          startX + width * 0.25f, startY,
          midX - width * 0.1f, midY,
          midX, midY
        )
        path.cubicTo(
          midX + width * 0.2f, midY,
          endX - width * 0.2f, endY,
          endX, endY
        )

        fillPath.addPath(path)
        fillPath.lineTo(endX, height)
        fillPath.lineTo(startX, height)
        fillPath.close()

        // Gradient fill under the curve
        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              SignalBlue.copy(alpha = 0.20f),
              SignalBlue.copy(alpha = 0.01f)
            ),
            startY = 0f,
            endY = height
          )
        )

        // Line curve stroke
        drawPath(
          path = path,
          color = SignalBlue,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Highlight end point
        drawCircle(
          color = PureWhite,
          radius = 6.dp.toPx(),
          center = Offset(endX - 2.dp.toPx(), endY)
        )
        drawCircle(
          color = SignalBlue,
          radius = 4.dp.toPx(),
          center = Offset(endX - 2.dp.toPx(), endY)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Projected Result Highlight
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MistGrey)
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "ESTIMATED CORPUS",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        FinancialText(
          amount = projectedTotalWealth,
          size = FinancialSize.Headline,
          showDecimals = false,
          customColor = InkNavy
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "ESTIMATED GAINS",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        FinancialText(
          amount = gainAmount,
          size = FinancialSize.Headline,
          isPositiveDelta = true,
          showDecimals = false,
          customColor = GrowthGreen
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Monthly Spare Change Slider
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Monthly Round-Up Savings",
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
          )
        )
        Text(
          text = "₹${monthlySavings.toInt()} / month",
          style = MaterialTheme.typography.labelMedium.copy(
            color = SignalBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        )
      }

      Slider(
        value = monthlySavings.toFloat(),
        onValueChange = { onMonthlySavingsChanged(it.toDouble()) },
        valueRange = 200f..3000f,
        steps = 27,
        colors = SliderDefaults.colors(
          thumbColor = SignalBlue,
          activeTrackColor = SignalBlue,
          inactiveTrackColor = BorderGrey
        ),
        modifier = Modifier.testTag("monthly_savings_slider")
      )
    }
  }
}

@Composable
private fun FundAllocationCard(fund: FundAllocation) {
  QuantimlCard(
    shape = RoundedCornerShape(16.dp),
    testTag = "fund_card_${fund.name.replace(" ", "_")}"
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Percentage badge circle
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(SignalBlue.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "${fund.allocationPercent}%",
          style = MaterialTheme.typography.labelMedium.copy(
            color = SignalBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = fund.name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy,
            fontSize = 15.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${fund.category} • 3Y Return: ${fund.annualizedReturn}%",
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy.copy(alpha = 0.5f),
            fontSize = 11.sp
          )
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        FinancialText(
          amount = fund.currentValuation,
          size = FinancialSize.Body,
          showDecimals = false,
          customColor = InkNavy
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          repeat(fund.rating) {
            Icon(
              Icons.Rounded.Star,
              contentDescription = null,
              tint = Color(0xFFFFB800),
              modifier = Modifier.size(11.dp)
            )
          }
        }
      }
    }
  }
}
