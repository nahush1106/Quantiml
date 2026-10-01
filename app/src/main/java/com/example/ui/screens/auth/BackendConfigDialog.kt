package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryLight

/**
 * Dialog allowing developers and testers to inspect and switch the Node.js Express backend URL
 * between Android Emulator (10.0.2.2:5000), Localhost (localhost:5000), or a custom LAN/Cloud server.
 */
@Composable
fun BackendConfigDialog(
  currentUrl: String,
  onDismiss: () -> Unit,
  onSaveUrl: (String) -> Unit
) {
  var urlInput by remember { mutableStateOf(currentUrl) }

  val presets = listOf(
    "http://10.0.2.2:5000/" to "Android Emulator (Default)",
    "http://localhost:5000/" to "Localhost / ChromeOS",
    "http://192.168.1.100:5000/" to "Physical Device (LAN IP)"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("backend_config_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SignalBlue.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Rounded.Dns,
            contentDescription = null,
            tint = SignalBlue,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Node.js Backend Server",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = InkNavy
          )
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Configure the Express REST API endpoint with JWT authentication and MongoDB persistence.",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
        )

        OutlinedTextField(
          value = urlInput,
          onValueChange = { urlInput = it },
          label = { Text("Base URL") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backend_url_input")
        )

        Text(
          text = "Quick Presets:",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy,
            fontWeight = FontWeight.Bold
          )
        )

        presets.forEach { (presetUrl, label) ->
          val isSelected = urlInput.trim().trimEnd('/') == presetUrl.trim().trimEnd('/')
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { urlInput = presetUrl },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) SignalBlue.copy(alpha = 0.1f) else MistGrey
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = label,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = InkNavy
                  )
                )
                Text(
                  text = presetUrl,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryLight,
                    fontSize = 11.sp
                  )
                )
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = "Selected",
                  tint = GrowthGreen,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onSaveUrl(urlInput.trim())
          onDismiss()
        },
        modifier = Modifier.testTag("backend_url_save_button")
      ) {
        Text("Save & Connect", fontWeight = FontWeight.Bold, color = SignalBlue)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondaryLight)
      }
    }
  )
}
