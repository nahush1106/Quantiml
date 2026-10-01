package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.InkNavy
import com.example.ui.theme.InputShape
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight

/**
 * Quantiml Outlined / Filled Input Field
 */
@Composable
fun QuantimlTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  placeholder: String? = null,
  isError: Boolean = false,
  errorMessage: String? = null,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  testTag: String = "quantiml_text_field"
) {
  Column(modifier = modifier.fillMaxWidth()) {
    if (label != null) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
          color = TextSecondaryLight,
          fontSize = 13.sp
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .clip(InputShape)
        .testTag(testTag),
      enabled = enabled,
      singleLine = singleLine,
      shape = InputShape,
      isError = isError,
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      placeholder = if (placeholder != null) {
        {
          Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedLight)
          )
        }
      } else null,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      colors = TextFieldDefaults.colors(
        focusedContainerColor = PureWhite,
        unfocusedContainerColor = MistGrey,
        disabledContainerColor = MistGrey.copy(alpha = 0.5f),
        errorContainerColor = PureWhite,
        focusedIndicatorColor = SignalBlue,
        unfocusedIndicatorColor = BorderGrey,
        errorIndicatorColor = AlertCoral,
        focusedTextColor = InkNavy,
        unfocusedTextColor = InkNavy
      ),
      textStyle = MaterialTheme.typography.bodyMedium.copy(color = InkNavy)
    )

    if (isError && !errorMessage.isNullOrEmpty()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodySmall.copy(
          color = AlertCoral,
          fontSize = 11.sp
        )
      )
    }
  }
}
