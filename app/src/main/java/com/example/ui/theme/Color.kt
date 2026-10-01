package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Quantiml Core Brand Colors (per design system specifications)
val InkNavy = Color(0xFF0B1F3A)
val SignalBlue = Color(0xFF2554FF)
val GrowthGreen = Color(0xFF00C48C)
val AmberInsight = Color(0xFFFFB020)
val AlertCoral = Color(0xFFFF5A5F)
val MistGrey = Color(0xFFF4F6FA)
val BorderGrey = Color(0xFFE4E8F0)

// Supporting & Functional Colors
val DeepNavySurface = Color(0xFF102747)
val CardNavy = Color(0xFF132D52)
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)
val OffWhite = Color(0xFFFAFBFC)

// Text Colors
val TextPrimaryLight = Color(0xFF0B1F3A)
val TextSecondaryLight = Color(0xFF627289)
val TextMutedLight = Color(0xFF98A6B8)

val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF9EAFD0)
val TextMutedDark = Color(0xFF677E9E)

// Soft Badge / Pill Backgrounds
val GrowthGreenContainer = Color(0xFFE6F9F3)
val SignalBlueContainer = Color(0xFFEBF0FF)
val AmberInsightContainer = Color(0xFFFFF7E6)
val AlertCoralContainer = Color(0xFFFFEEEE)

// Quantiml Hero & Subtle Brand Gradients
object QuantimlGradients {
  val Hero = Brush.linearGradient(
    colors = listOf(
      Color(0xFF0B1F3A),
      Color(0xFF163761),
      Color(0xFF0D2545)
    )
  )

  // Sleek Interface Theme: Vibrant Signal Blue to Ink Navy
  val SleekHero = Brush.linearGradient(
    colors = listOf(
      Color(0xFF2554FF),
      Color(0xFF1533A1),
      Color(0xFF0B1F3A)
    )
  )

  val BlueGlow = Brush.radialGradient(
    colors = listOf(
      Color(0xFF2554FF).copy(alpha = 0.35f),
      Color.Transparent
    )
  )

  val CardDark = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF16325C),
      Color(0xFF0F2442)
    )
  )

  val GreenGrowth = Brush.horizontalGradient(
    colors = listOf(
      Color(0xFF00C48C),
      Color(0xFF00E5A3)
    )
  )
}

