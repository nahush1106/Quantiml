package com.example.ui.screens.design

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.ButtonVariant
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlButton
import com.example.ui.components.QuantimlCard
import com.example.ui.components.QuantimlHeroCard
import com.example.ui.components.QuantimlInsightCard
import com.example.ui.components.QuantimlLogo
import com.example.ui.components.QuantimlOutlinedButton
import com.example.ui.components.QuantimlTextField
import com.example.ui.components.RoundUpBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.AmberInsight
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardNavy
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthGreenContainer
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlRadiusMedium
import com.example.ui.theme.QuantimlTheme
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight

import com.example.ui.components.SleekBottomNav
import com.example.ui.components.SleekTab

/**
 * Phase 1 Design System Preview Screen:
 * Showcases all colors, typography scales, corner radiuses, spacing,
 * and reusable components adhering to the Quantiml Figma specifications.
 */
@Composable
fun DesignSystemPreviewScreen(
  modifier: Modifier = Modifier
) {
  var sampleInput by remember { mutableStateOf("") }
  var selectedTab by remember { mutableStateOf(SleekTab.Home) }
  val spacing = QuantimlTheme.spacing

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("design_system_preview_screen"),
    containerColor = MistGrey,
    bottomBar = {
      SleekBottomNav(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onFabClick = { /* Quick Add */ }
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
      // 1. Header & Brand Identity Banner
      item {
        Spacer(modifier = Modifier.height(spacing.s))
        HeaderSection()
      }

      // 2. Color System Swatches
      item {
        SectionHeader(title = "Brand Color System")
        ColorPaletteSection()
      }

      // 3. Hero Financial Card (Sleek Interface reference)
      item {
        SectionHeader(title = "Spare Change Wallet")
        HeroCardDemo()
      }

      // 3b. Sleek 2-Column Metrics (Total Invested & Portfolio Value)
      item {
        SleekMetricsSection()
      }

      // 4. AI Insight Component (Amber Insight)
      item {
        SectionHeader(title = "Smart AI Insight")
        QuantimlInsightCard(
          title = "Smart AI Insight",
          insightText = "Reducing food delivery by 20% could increase your monthly investment by ₹400."
        )
      }

      // 5. Smart Round-Up & Transaction Card
      item {
        SectionHeader(title = "Smart Round-Up Transaction Card")
        TransactionCardDemo()
      }

      // 6. Typography & Financial Tabular Numbers
      item {
        SectionHeader(title = "Typography & Tabular Numbers")
        TypographySection()
      }

      // 7. Interactive Form Field
      item {
        SectionHeader(title = "Input Component")
        QuantimlTextField(
          value = sampleInput,
          onValueChange = { sampleInput = it },
          label = "Registered Mobile / Email",
          placeholder = "e.g. aryan.sharma@example.com",
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Email,
              contentDescription = null,
              tint = TextSecondaryLight
            )
          }
        )
      }

      // 8. Buttons & Actions
      item {
        SectionHeader(title = "Action Components")
        Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
          QuantimlButton(
            text = "Continue to Onboarding",
            onClick = { /* Demo */ },
            variant = ButtonVariant.Primary
          )

          QuantimlButton(
            text = "Invest Now (Simulated ₹500)",
            onClick = { /* Demo */ },
            variant = ButtonVariant.Growth,
            leadingIcon = {
              Icon(
                imageVector = Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = PureWhite
              )
            }
          )

          QuantimlOutlinedButton(
            text = "Secondary Action / Skip",
            onClick = { /* Demo */ }
          )
        }
      }

      // Bottom Spacer for clean edge-to-edge layout
      item {
        Spacer(modifier = Modifier.height(spacing.xxl))
      }
    }
  }
}

@Composable
private fun HeaderSection() {
  val spacing = QuantimlTheme.spacing
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = spacing.xs),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "Good morning,",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = InkNavy.copy(alpha = 0.6f),
          letterSpacing = 1.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Arjun Sharma",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = InkNavy,
          fontSize = 24.sp
        )
      )
    }

    // Sleek Interface Avatar: 48dp, rounded-2xl, InkNavy with white border and initial AS
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(InkNavy)
        .border(2.dp, PureWhite, RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "AS",
        style = MaterialTheme.typography.titleMedium.copy(
          color = PureWhite,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      )
    }
  }
}

@Composable
private fun ColorPaletteSection() {
  val spacing = QuantimlTheme.spacing
  QuantimlCard {
    Text(
      text = "Primary & Functional Palette",
      style = MaterialTheme.typography.labelMedium.copy(
        color = TextSecondaryLight,
        fontWeight = FontWeight.SemiBold
      )
    )
    Spacer(modifier = Modifier.height(spacing.s))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      ColorChip(color = InkNavy, name = "Ink Navy", hex = "#0B1F3A")
      ColorChip(color = SignalBlue, name = "Signal Blue", hex = "#2554FF")
      ColorChip(color = GrowthGreen, name = "Growth Green", hex = "#00C48C")
      ColorChip(color = AmberInsight, name = "Amber Insight", hex = "#FFB020")
      ColorChip(color = AlertCoral, name = "Alert Coral", hex = "#FF5A5F")
    }
  }
}

@Composable
private fun ColorChip(
  color: Color,
  name: String,
  hex: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(60.dp)
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(QuantimlRadiusMedium))
        .background(color)
        .border(1.dp, BorderGrey, RoundedCornerShape(QuantimlRadiusMedium))
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = name,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
      maxLines = 1,
      color = InkNavy
    )
    Text(
      text = hex,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        color = TextSecondaryLight
      )
    )
  }
}

@Composable
private fun HeroCardDemo() {
  val spacing = QuantimlTheme.spacing
  QuantimlHeroCard {
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
      // Pulse indicator
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(GrowthGreen)
      )
    }

    Spacer(modifier = Modifier.height(spacing.xs))

    FinancialText(
      amount = 350.00,
      size = FinancialSize.Display,
      customColor = PureWhite
    )

    Spacer(modifier = Modifier.height(spacing.m))

    // Invest Threshold Bar and Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column {
        Text(
          text = "INVEST THRESHOLD",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PureWhite.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Progress bar 70%
          Box(
            modifier = Modifier
              .width(100.dp)
              .height(6.dp)
              .clip(CircleShape)
              .background(PureWhite.copy(alpha = 0.2f))
          ) {
            Box(
              modifier = Modifier
                .width(70.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(GrowthGreen)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "70%",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PureWhite,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          )
        }
      }

      Surface(
        onClick = { /* Demo */ },
        shape = RoundedCornerShape(12.dp),
        color = PureWhite,
        modifier = Modifier.height(34.dp)
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "View Details",
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
private fun SleekMetricsSection() {
  val spacing = QuantimlTheme.spacing
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Total Invested Card
    QuantimlCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(20.dp)
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
        amount = 2000.00,
        size = FinancialSize.Title,
        showDecimals = false,
        customColor = InkNavy
      )
    }

    // Portfolio Value Card
    QuantimlCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(20.dp)
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
          amount = 2350.00,
          size = FinancialSize.Title,
          showDecimals = false,
          customColor = InkNavy
        )
        Text(
          text = "+6.2%",
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
private fun TransactionCardDemo() {
  val spacing = QuantimlTheme.spacing
  QuantimlCard(
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MistGrey),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "☕",
          fontSize = 20.sp
        )
      }

      Spacer(modifier = Modifier.width(spacing.s))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Starbucks",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy,
            fontSize = 15.sp
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Today, 08:45 AM",
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy.copy(alpha = 0.4f),
            fontSize = 11.sp
          )
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        FinancialText(
          amount = 3.00,
          size = FinancialSize.Body,
          isPositiveDelta = true,
          customColor = GrowthGreen
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Rounded from ₹97",
          style = MaterialTheme.typography.bodySmall.copy(
            color = InkNavy.copy(alpha = 0.4f),
            fontSize = 10.sp
          )
        )
      }
    }
  }
}

@Composable
private fun TypographySection() {
  val spacing = QuantimlTheme.spacing
  QuantimlCard {
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
      Text(
        text = "Heading Large (Manrope)",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = InkNavy
        )
      )
      Text(
        text = "Body Medium (Inter) – Quantiml turns your daily spare change into meaningful long-term investments automatically.",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "IBM Plex Mono (Tabular Numbers):",
          style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
        )
        FinancialText(
          amount = 14250.75,
          size = FinancialSize.Title,
          isPositiveDelta = true
        )
      }
    }
  }
}
