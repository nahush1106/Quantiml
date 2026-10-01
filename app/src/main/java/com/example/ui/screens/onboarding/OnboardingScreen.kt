package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ButtonVariant
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlButton
import com.example.ui.components.QuantimlCard
import com.example.ui.components.QuantimlHeroCard
import com.example.ui.components.QuantimlOutlinedButton
import com.example.ui.components.RoundUpBadge
import com.example.ui.theme.AmberInsight
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthGreenContainer
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlTheme
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.SignalBlueContainer
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch

data class OnboardingStep(
  val tag: String,
  val title: String,
  val description: String,
  val highlightBadge: String
)

val OnboardingSteps = listOf(
  OnboardingStep(
    tag = "STEP 1 OF 3",
    title = "Spend normally. Save automatically.",
    description = "Every UPI, debit card, or merchant purchase is rounded up to the nearest ₹10 or ₹50. Your spare change builds wealth in the background.",
    highlightBadge = "Smart Round-Up"
  ),
  OnboardingStep(
    tag = "STEP 2 OF 3",
    title = "Classified by QuantAI.",
    description = "Our backend model distinguishes essentials like groceries from discretionary treats, optimizing your round-ups so you never feel the pinch.",
    highlightBadge = "QuantAI NLP"
  ),
  OnboardingStep(
    tag = "STEP 3 OF 3",
    title = "Invested in SEBI-regulated funds.",
    description = "Once your wallet reaches ₹500, funds are automatically deployed into diversified mutual funds with zero lock-in and instant liquidity.",
    highlightBadge = "Auto-Deploy"
  )
)

/**
 * Phase 2: Onboarding Screen
 * 3-step carousel showcasing Quantiml's core value proposition with interactive visual graphic cards,
 * pagination indicators, Skip, and Next / Get Started buttons.
 */
@Composable
fun OnboardingScreen(
  onFinishOnboarding: () -> Unit,
  onLoginClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val pagerState = rememberPagerState(pageCount = { OnboardingSteps.size })
  val coroutineScope = rememberCoroutineScope()
  val spacing = QuantimlTheme.spacing

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("onboarding_screen"),
    containerColor = MistGrey,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = spacing.m, vertical = spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        QuantimlBadge(
          text = OnboardingSteps[pagerState.currentPage].tag,
          textColor = SignalBlue,
          backgroundColor = SignalBlueContainer
        )

        if (pagerState.currentPage < OnboardingSteps.size - 1) {
          TextButton(
            onClick = onFinishOnboarding,
            modifier = Modifier.testTag("onboarding_skip_button")
          ) {
            Text(
              text = "Skip",
              style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondaryLight,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        } else {
          Spacer(modifier = Modifier.width(48.dp))
        }
      }
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = spacing.m, vertical = spacing.m)
      ) {
        // Page Indicators
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.m),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(OnboardingSteps.size) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(8.dp)
                .width(if (isSelected) 24.dp else 8.dp)
                .clip(CircleShape)
                .background(if (isSelected) SignalBlue else BorderGrey)
            )
          }
        }

        // Navigation Action Buttons
        if (pagerState.currentPage < OnboardingSteps.size - 1) {
          QuantimlButton(
            text = "Continue",
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
              }
            },
            variant = ButtonVariant.Primary,
            testTag = "onboarding_next_button"
          )
        } else {
          QuantimlButton(
            text = "Get Started",
            onClick = onFinishOnboarding,
            variant = ButtonVariant.Growth,
            testTag = "onboarding_get_started_button"
          )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Already have an account? ",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
          )
          TextButton(
            onClick = onLoginClick,
            modifier = Modifier.testTag("onboarding_login_link")
          ) {
            Text(
              text = "Sign In",
              style = MaterialTheme.typography.labelSmall.copy(
                color = SignalBlue,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
      }
    }
  ) { innerPadding ->
    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) { page ->
      val step = OnboardingSteps[page]
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Spacer(modifier = Modifier.height(spacing.s))

        // Center Visual Graphic (Custom per step)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          when (page) {
            0 -> OnboardingStepOneVisual()
            1 -> OnboardingStepTwoVisual()
            2 -> OnboardingStepThreeVisual()
          }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Title and Description text
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = spacing.l)
        ) {
          Text(
            text = step.title,
            style = MaterialTheme.typography.headlineMedium.copy(
              color = InkNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              textAlign = TextAlign.Center
            )
          )

          Spacer(modifier = Modifier.height(spacing.s))

          Text(
            text = step.description,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = TextSecondaryLight,
              textAlign = TextAlign.Center,
              fontSize = 14.sp,
              lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = spacing.s)
          )
        }
      }
    }
  }
}

/**
 * Step 1 Visual: Coffee purchase with interactive round-up calculation
 */
@Composable
private fun OnboardingStepOneVisual() {
  val spacing = QuantimlTheme.spacing
  QuantimlCard(
    modifier = Modifier.fillMaxWidth(0.92f),
    shape = RoundedCornerShape(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MistGrey),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "☕", fontSize = 22.sp)
      }

      Spacer(modifier = Modifier.width(spacing.s))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Morning Espresso",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy
          )
        )
        Text(
          text = "Actual spend: ₹93.00",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
        )
      }

      RoundUpBadge(amount = "₹7.00")
    }

    Spacer(modifier = Modifier.height(spacing.m))

    // Round-up mechanics flow breakdown
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MistGrey)
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Charged",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
          )
          Text(
            text = "₹100.00",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = InkNavy
            )
          )
        }
        Text(text = "➔", color = SignalBlue, fontWeight = FontWeight.Bold)
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Auto-saved to Wallet",
            style = MaterialTheme.typography.labelSmall.copy(color = GrowthGreen)
          )
          Text(
            text = "+₹7.00",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = GrowthGreen
            )
          )
        }
      }
    }
  }
}

/**
 * Step 2 Visual: QuantAI NLP Categorization distinction
 */
@Composable
private fun OnboardingStepTwoVisual() {
  val spacing = QuantimlTheme.spacing
  Column(
    modifier = Modifier.fillMaxWidth(0.92f),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Card A: Discretionary Spend (Boosted)
    QuantimlCard(
      shape = RoundedCornerShape(16.dp),
      borderColor = BorderGrey
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AmberInsight.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🍕", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(spacing.s))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Zomato Dine-in",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = InkNavy
            )
          )
          Text(
            text = "Classified: Discretionary",
            style = MaterialTheme.typography.bodySmall.copy(
              color = AmberInsight,
              fontSize = 11.sp
            )
          )
        }
        QuantimlBadge(
          text = "2x Round-Up",
          textColor = PureWhite,
          backgroundColor = SignalBlue
        )
      }
    }

    // Card B: Essential Spend (Protected)
    QuantimlCard(
      shape = RoundedCornerShape(16.dp),
      borderColor = BorderGrey
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MistGrey),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🥦", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(spacing.s))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Blinkit Groceries",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = InkNavy
            )
          )
          Text(
            text = "Classified: Essential",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondaryLight,
              fontSize = 11.sp
            )
          )
        }
        QuantimlBadge(
          text = "Standard",
          textColor = TextSecondaryLight,
          backgroundColor = MistGrey
        )
      }
    }
  }
}

/**
 * Step 3 Visual: Portfolio auto-deployment & SEBI regulation trust badge
 */
@Composable
private fun OnboardingStepThreeVisual() {
  val spacing = QuantimlTheme.spacing
  QuantimlHeroCard(
    modifier = Modifier.fillMaxWidth(0.92f)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "TARGET FUND DEPLOYMENT",
        style = MaterialTheme.typography.labelSmall.copy(
          color = PureWhite.copy(alpha = 0.7f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      )
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(GrowthGreen.copy(alpha = 0.2f))
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = "SEBI Regulated",
          style = MaterialTheme.typography.labelSmall.copy(
            color = GrowthGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(spacing.s))

    FinancialText(
      amount = 500.00,
      size = FinancialSize.Headline,
      customColor = PureWhite
    )

    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Auto-invested in Nifty 50 Index Fund",
      style = MaterialTheme.typography.bodySmall.copy(
        color = PureWhite.copy(alpha = 0.8f),
        fontSize = 12.sp
      )
    )

    Spacer(modifier = Modifier.height(spacing.m))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(PureWhite.copy(alpha = 0.1f))
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Rounded.Security,
        contentDescription = "Bank-grade Security",
        tint = GrowthGreen,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "256-bit encryption • Direct AMC Folio",
        style = MaterialTheme.typography.labelSmall.copy(
          color = PureWhite.copy(alpha = 0.9f),
          fontSize = 11.sp
        )
      )
    }
  }
}
