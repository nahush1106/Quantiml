package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.SignalBlue

/**
 * Quantiml official logo:
 * A continuous ring representing the 'Q' and the round-up-return cycle,
 * opening into an upward checkmark/growth path and ending with a vibrant growth green dot.
 */
@Composable
fun QuantimlLogo(
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  ringColor: Color = SignalBlue,
  growthColor: Color = GrowthGreen,
  strokeWidthRatio: Float = 0.12f
) {
  Box(
    modifier = modifier
      .size(size)
      .testTag("quantiml_logo"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val strokeWidth = this.size.minDimension * strokeWidthRatio
      val padding = strokeWidth * 1.2f
      val w = this.size.width
      val h = this.size.height

      // Main circular Q ring (with opening in upper right for growth path)
      val ringRadius = (w - (padding * 2)) / 2f
      val center = Offset(w / 2f, h / 2f)

      drawArc(
        color = ringColor,
        startAngle = 55f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2, ringRadius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )

      // Upward checkmark / growth path bursting upward from the center through the Q
      val path = Path().apply {
        // Start from inner bottom-left of center
        moveTo(w * 0.38f, h * 0.52f)
        // Downward inflection of the checkmark
        lineTo(w * 0.50f, h * 0.65f)
        // Upward surge breaking through the open Q quadrant to the top right
        lineTo(w * 0.74f, h * 0.32f)
      }

      drawPath(
        path = path,
        color = ringColor,
        style = Stroke(width = strokeWidth * 1.05f, cap = StrokeCap.Round)
      )

      // Terminal growth dot at the tip of the upward trajectory
      drawCircle(
        color = growthColor,
        radius = strokeWidth * 1.15f,
        center = Offset(w * 0.76f, h * 0.28f)
      )
    }
  }
}
