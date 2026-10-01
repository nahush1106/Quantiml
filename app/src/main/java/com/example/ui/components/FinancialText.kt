package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.QuantimlTheme

enum class FinancialSize {
  Display,  // 32sp large balance
  Headline, // 24sp card balance
  Title,    // 18sp section balance
  Body,     // 15sp transaction amount
  Small     // 12sp small tag
}

/**
 * Quantiml Financial Number Display
 * Uses IBM Plex Mono styling for tabular readability and precision.
 */
@Composable
fun FinancialText(
  amount: Double,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹",
  size: FinancialSize = FinancialSize.Headline,
  isPositiveDelta: Boolean = false,
  customColor: Color? = null,
  showDecimals: Boolean = true,
  testTag: String = "financial_text"
) {
  val baseStyle: TextStyle = when (size) {
    FinancialSize.Display -> QuantimlTheme.financialTypography.display
    FinancialSize.Headline -> QuantimlTheme.financialTypography.headline
    FinancialSize.Title -> QuantimlTheme.financialTypography.title
    FinancialSize.Body -> QuantimlTheme.financialTypography.body
    FinancialSize.Small -> QuantimlTheme.financialTypography.small
  }

  val textColor = customColor ?: when {
    isPositiveDelta -> GrowthGreen
    else -> InkNavy
  }

  val formattedNumber = if (showDecimals) {
    String.format("%,.2f", amount)
  } else {
    String.format("%,.0f", amount)
  }

  val displayText = buildString {
    if (isPositiveDelta && amount > 0) append("+")
    append(currencySymbol)
    append(formattedNumber)
  }

  Text(
    text = displayText,
    modifier = modifier.testTag(testTag),
    style = baseStyle.copy(color = textColor)
  )
}
