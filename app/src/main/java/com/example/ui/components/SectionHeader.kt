package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkNavy
import com.example.ui.theme.SignalBlue

@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  titleColor: Color = InkNavy,
  testTag: String = "section_header"
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = titleColor,
        fontSize = 17.sp
      )
    )

    if (actionText != null && onActionClick != null) {
      Text(
        text = actionText,
        modifier = Modifier
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onActionClick
          )
          .padding(4.dp),
        style = MaterialTheme.typography.labelMedium.copy(
          color = SignalBlue,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
      )
    }
  }
}
