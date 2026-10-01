package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = SignalBlue,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF1A388A),
    onPrimaryContainer = Color(0xFFD6E2FF),

    secondary = GrowthGreen,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF004D36),
    onSecondaryContainer = Color(0xFF75F8CD),

    tertiary = AmberInsight,
    onTertiary = PureBlack,
    tertiaryContainer = Color(0xFF5C3E00),
    onTertiaryContainer = Color(0xFFFFDFA6),

    background = InkNavy,
    onBackground = PureWhite,

    surface = DeepNavySurface,
    onSurface = PureWhite,
    surfaceVariant = CardNavy,
    onSurfaceVariant = TextSecondaryDark,

    outline = Color(0xFF1E3D6B),
    outlineVariant = Color(0xFF132B4F),

    error = AlertCoral,
    onError = PureWhite,
    errorContainer = Color(0xFF5E1316),
    onErrorContainer = Color(0xFFFFD4D6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SignalBlue,
    onPrimary = PureWhite,
    primaryContainer = SignalBlueContainer,
    onPrimaryContainer = SignalBlue,

    secondary = GrowthGreen,
    onSecondary = PureWhite,
    secondaryContainer = GrowthGreenContainer,
    onSecondaryContainer = Color(0xFF007A57),

    tertiary = AmberInsight,
    onTertiary = PureWhite,
    tertiaryContainer = AmberInsightContainer,
    onTertiaryContainer = Color(0xFF8C5800),

    background = MistGrey,
    onBackground = InkNavy,

    surface = PureWhite,
    onSurface = InkNavy,
    surfaceVariant = MistGrey,
    onSurfaceVariant = TextSecondaryLight,

    outline = BorderGrey,
    outlineVariant = Color(0xFFEDEFEF),

    error = AlertCoral,
    onError = PureWhite,
    errorContainer = AlertCoralContainer,
    onErrorContainer = Color(0xFF9E1F24)
  )

/**
 * Quantiml Design System Theme Wrapper
 */
@Composable
fun QuantimlTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val spacing = QuantimlSpacing()
  val financialTypography = FinancialTypography()

  CompositionLocalProvider(
    LocalQuantimlSpacing provides spacing,
    LocalFinancialTypography provides financialTypography
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      shapes = QuantimlShapes,
      typography = QuantimlTypography,
      content = content
    )
  }
}

/**
 * Accessor object for easy access to custom Quantiml tokens in Composables:
 * e.g., QuantimlTheme.spacing.m, QuantimlTheme.financialTypography.display
 */
object QuantimlTheme {
  val spacing: QuantimlSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalQuantimlSpacing.current

  val financialTypography: FinancialTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalFinancialTypography.current
}

// Backwards compatibility for template files
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve Quantiml brand identity over dynamic device wallpaper
  content: @Composable () -> Unit,
) {
  QuantimlTheme(darkTheme = darkTheme, content = content)
}

