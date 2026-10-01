package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Quantiml official corner radius system:
 * 8 dp, 12 dp, 16 dp, 24 dp, 999 dp for pills
 */
val QuantimlRadiusSmall = 8.dp
val QuantimlRadiusMedium = 12.dp
val QuantimlRadiusLarge = 16.dp
val QuantimlRadiusExtraLarge = 24.dp
val QuantimlRadiusPill = 999.dp

val QuantimlShapes = Shapes(
  extraSmall = RoundedCornerShape(QuantimlRadiusSmall),
  small = RoundedCornerShape(QuantimlRadiusSmall),
  medium = RoundedCornerShape(QuantimlRadiusMedium),
  large = RoundedCornerShape(QuantimlRadiusLarge),
  extraLarge = RoundedCornerShape(QuantimlRadiusExtraLarge)
)

val PillShape = RoundedCornerShape(QuantimlRadiusPill)
val CardShape = RoundedCornerShape(QuantimlRadiusLarge)
val ButtonShape = RoundedCornerShape(QuantimlRadiusPill)
val InputShape = RoundedCornerShape(QuantimlRadiusMedium)
val BadgeShape = RoundedCornerShape(QuantimlRadiusPill)
