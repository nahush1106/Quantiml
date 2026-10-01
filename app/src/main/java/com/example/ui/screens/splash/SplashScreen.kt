package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuantimlLogo
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlGradients
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay

/**
 * Phase 2: Quantiml Splash Screen
 * Features the brand Q logo with entering scale/alpha animations,
 * high-contrast tagline "Grow, without trying.", and seamless navigation to Onboarding.
 */
@Composable
fun SplashScreen(
  onAnimationFinished: () -> Unit,
  modifier: Modifier = Modifier,
  autoNavigateDelayMillis: Long = 1800L
) {
  val logoScale = remember { Animatable(0.7f) }
  val contentAlpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    // Animate logo spring/ease in
    logoScale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    contentAlpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
    delay(autoNavigateDelayMillis)
    onAnimationFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(QuantimlGradients.SleekHero)
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Top-right subtle glowing orb accent
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(QuantimlGradients.BlueGlow)
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(logoScale.value)
        .alpha(contentAlpha.value)
    ) {
      // White container pill/card backing for the logo
      Box(
        modifier = Modifier
          .size(92.dp)
          .clip(CircleShape)
          .background(PureWhite),
        contentAlignment = Alignment.Center
      ) {
        QuantimlLogo(
          size = 64.dp,
          ringColor = SignalBlue,
          growthColor = GrowthGreen
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Quantiml",
        style = MaterialTheme.typography.displayMedium.copy(
          color = PureWhite,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp,
          fontSize = 32.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Grow, without trying",
          style = MaterialTheme.typography.bodyLarge.copy(
            color = TextSecondaryDark,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
          )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
          modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(GrowthGreen)
        )
      }
    }

    // Bottom brand tag
    Text(
      text = "Smart Micro-Investing for India",
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp)
        .alpha(contentAlpha.value * 0.7f),
      style = MaterialTheme.typography.labelSmall.copy(
        color = PureWhite.copy(alpha = 0.5f),
        letterSpacing = 0.5.sp
      )
    )
  }
}
