package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.ButtonShape
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalBlue

enum class ButtonVariant {
  Primary,    // Signal Blue action
  Growth,     // Growth Green (investments / positive round-ups)
  Secondary,  // Dark Navy
}

/**
 * Quantiml Primary Action Pill Button
 */
@Composable
fun QuantimlButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: ButtonVariant = ButtonVariant.Primary,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  fullWidth: Boolean = true,
  testTag: String = "quantiml_button"
) {
  val containerColor = when (variant) {
    ButtonVariant.Primary -> SignalBlue
    ButtonVariant.Growth -> GrowthGreen
    ButtonVariant.Secondary -> InkNavy
  }

  val contentColor = PureWhite

  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 52.dp)
      .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
      .testTag(testTag),
    enabled = enabled && !isLoading,
    shape = ButtonShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor,
      disabledContainerColor = containerColor.copy(alpha = 0.45f),
      disabledContentColor = contentColor.copy(alpha = 0.7f)
    ),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp)
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.5.dp,
        color = contentColor
      )
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (leadingIcon != null) {
          leadingIcon()
          Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
          )
        )

        if (trailingIcon != null) {
          Spacer(modifier = Modifier.width(8.dp))
          trailingIcon()
        }
      }
    }
  }
}

/**
 * Quantiml Outlined Pill Button
 */
@Composable
fun QuantimlOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  borderColor: Color = BorderGrey,
  textColor: Color = InkNavy,
  fullWidth: Boolean = true,
  leadingIcon: @Composable (() -> Unit)? = null,
  testTag: String = "quantiml_outlined_button"
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 52.dp)
      .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
      .testTag(testTag),
    enabled = enabled,
    shape = ButtonShape,
    border = BorderStroke(1.2.dp, borderColor),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = textColor,
      disabledContentColor = textColor.copy(alpha = 0.4f)
    ),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      )
    }
  }
}
