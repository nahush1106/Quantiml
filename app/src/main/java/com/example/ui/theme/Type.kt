package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Font Families (using system fallbacks that align with Manrope, Inter, and IBM Plex Mono)
val HeadingFontFamily = FontFamily.SansSerif      // Represents Manrope for structured modern headings
val BodyFontFamily = FontFamily.SansSerif         // Represents Inter for highly readable body copy
val FinancialFontFamily = FontFamily.Monospace    // Represents IBM Plex Mono for clean tabular figures

/**
 * Quantiml Material 3 Typography
 */
val QuantimlTypography = Typography(
  // Display & Headings (Manrope aesthetic)
  displayLarge = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = (-0.5).sp
  ),
  displayMedium = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.25).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.2).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.1).sp
  ),
  headlineSmall = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),

  // Titles
  titleLarge = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
  ),
  titleMedium = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.15.sp
  ),
  titleSmall = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),

  // Body (Inter aesthetic)
  bodyLarge = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.25.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp
  ),

  // Labels & Buttons
  labelLarge = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
  ),
  labelSmall = TextStyle(
    fontFamily = HeadingFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.4.sp
  )
)

/**
 * Dedicated typography for Financial Figures (IBM Plex Mono aesthetic).
 * Provides proportional tabular numbers for wallets, round-ups, and portfolio values.
 */
@Immutable
data class FinancialTypography(
  val display: TextStyle = TextStyle(
    fontFamily = FinancialFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
  ),
  val headline: TextStyle = TextStyle(
    fontFamily = FinancialFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.2).sp
  ),
  val title: TextStyle = TextStyle(
    fontFamily = FinancialFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),
  val body: TextStyle = TextStyle(
    fontFamily = FinancialFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 22.sp
  ),
  val small: TextStyle = TextStyle(
    fontFamily = FinancialFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
  )
)

val LocalFinancialTypography = staticCompositionLocalOf { FinancialTypography() }

// Backwards compatibility
val Typography = QuantimlTypography

