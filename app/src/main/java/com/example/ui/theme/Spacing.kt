package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Quantiml official spacing system:
 * 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 dp
 */
@Immutable
data class QuantimlSpacing(
  val xxs: Dp = 4.dp,
  val xs: Dp = 8.dp,
  val s: Dp = 12.dp,
  val m: Dp = 16.dp,
  val l: Dp = 20.dp,
  val xl: Dp = 24.dp,
  val xxl: Dp = 32.dp,
  val xxxl: Dp = 40.dp,
  val huge: Dp = 48.dp,
  val giant: Dp = 64.dp,
)

val LocalQuantimlSpacing = staticCompositionLocalOf { QuantimlSpacing() }
