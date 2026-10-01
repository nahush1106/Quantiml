package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.theme.AmberInsight
import com.example.ui.theme.AmberInsightContainer
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardShape
import com.example.ui.theme.InkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlGradients
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight

val CardSleekShape = RoundedCornerShape(20.dp)
val HeroSleekShape = RoundedCornerShape(24.dp)

/**
 * Standard Light Quantiml Card (for transactions, settings, quick actions)
 * Styled with Sleek Interface aesthetic: border 1dp #E4E8F0, rounded-[20px], white surface
 */
@Composable
fun QuantimlCard(
  modifier: Modifier = Modifier,
  borderColor: Color = BorderGrey,
  backgroundColor: Color = PureWhite,
  shape: androidx.compose.ui.graphics.Shape = CardSleekShape,
  testTag: String = "quantiml_card",
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(BorderStroke(1.dp, borderColor), shape)
      .testTag(testTag),
    color = backgroundColor,
    shape = shape,
    tonalElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      content = content
    )
  }
}

/**
 * Hero Dark Card (for high-impact financial totals like Portfolio Value & Wallet Balance)
 * Styled with Sleek Interface aesthetic: rounded-[24px], gradient from #2554FF to #0B1F3A,
 * subtle radial blur accent, high-contrast typography
 */
@Composable
fun QuantimlHeroCard(
  modifier: Modifier = Modifier,
  testTag: String = "quantiml_hero_card",
  content: @Composable ColumnScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(HeroSleekShape)
      .background(QuantimlGradients.SleekHero)
      .border(BorderStroke(1.dp, Color(0xFF2554FF).copy(alpha = 0.25f)), HeroSleekShape)
      .testTag(testTag)
  ) {
    // Subtle top-right ambient blur orb
    Box(
      modifier = Modifier
        .matchParentSize()
        .background(QuantimlGradients.BlueGlow)
    )

    Column(
      modifier = Modifier.padding(24.dp),
      content = content
    )
  }
}

/**
 * AI Insight Card (for QuantAI discretionary spending insights)
 * Uses the Sleek Interface aesthetic: Amber Insight Container with 20% border, rounded-[16px]
 */
@Composable
fun QuantimlInsightCard(
  title: String,
  insightText: String,
  modifier: Modifier = Modifier,
  testTag: String = "quantiml_insight_card"
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(AmberInsight.copy(alpha = 0.10f))
      .border(BorderStroke(1.dp, AmberInsight.copy(alpha = 0.25f)), RoundedCornerShape(16.dp))
      .padding(16.dp)
      .testTag(testTag)
  ) {
    Row(verticalAlignment = Alignment.Top) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(AmberInsight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = "AI Insight",
          tint = PureWhite,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
              color = InkNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          QuantimlBadge(
            text = "QuantAI",
            textColor = Color(0xFF8C5800),
            backgroundColor = AmberInsight.copy(alpha = 0.20f)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = insightText,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = InkNavy.copy(alpha = 0.75f),
            fontSize = 13.sp,
            lineHeight = 18.sp
          )
        )
      }
    }
  }
}
