package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeShape
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthGreenContainer
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.SignalBlueContainer

/**
 * Quantiml Semantic Badge / Pill Tag
 */
@Composable
fun QuantimlBadge(
  text: String,
  modifier: Modifier = Modifier,
  textColor: Color = SignalBlue,
  backgroundColor: Color = SignalBlueContainer,
  showDot: Boolean = false,
  dotColor: Color = textColor,
  testTag: String = "quantiml_badge"
) {
  Box(
    modifier = modifier
      .clip(BadgeShape)
      .background(backgroundColor)
      .padding(horizontal = 8.dp, vertical = 3.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (showDot) {
        Box(
          modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(dotColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.labelSmall.copy(
          color = textColor,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp
        )
      )
    }
  }
}

/**
 * Compact Category Circle Pill Badge ('D' for Discretionary, 'E' for Essential)
 */
@Composable
fun CategoryBadge(
  isDiscretionary: Boolean,
  modifier: Modifier = Modifier
) {
  val label = if (isDiscretionary) "D" else "E"
  val textColor = if (isDiscretionary) SignalBlue else GrowthGreen
  val bgColor = (if (isDiscretionary) SignalBlue else GrowthGreen).copy(alpha = 0.12f)

  Box(
    modifier = modifier
      .size(20.dp)
      .clip(CircleShape)
      .background(bgColor)
      .testTag("category_badge_${if (isDiscretionary) "discretionary" else "essential"}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = textColor,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp
      )
    )
  }
}

/**
 * Specialized Growth Green Pill for Round-ups (e.g. +₹30)
 */
@Composable
fun RoundUpBadge(
  amount: String,
  modifier: Modifier = Modifier
) {
  QuantimlBadge(
    text = "+$amount",
    textColor = GrowthGreen,
    backgroundColor = GrowthGreenContainer,
    modifier = modifier,
    testTag = "round_up_badge"
  )
}
