package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.ui.navigation.QuantimlNavHost
import com.example.ui.screens.design.DesignSystemPreviewScreen
import com.example.ui.theme.QuantimlTheme

class MainActivity : ComponentActivity() {

  private val requestPermissionsLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    // Permissions requested for SMS auto-detection
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    requestSmsAndNotificationPermissions()

    setContent {
      QuantimlTheme {
        QuantimlNavHost()
      }
    }
  }

  private fun requestSmsAndNotificationPermissions() {
    val permissionsToRequest = mutableListOf(
      Manifest.permission.RECEIVE_SMS,
      Manifest.permission.READ_SMS
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val missingPermissions = permissionsToRequest.filter { permission ->
      ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }

    if (missingPermissions.isNotEmpty()) {
      requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
    }
  }
}

/**
 * Retained for greeting screenshot test verification
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  QuantimlTheme {
    DesignSystemPreviewScreen()
  }
}

