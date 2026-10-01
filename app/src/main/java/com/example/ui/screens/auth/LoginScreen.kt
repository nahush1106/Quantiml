package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ButtonVariant
import com.example.ui.components.QuantimlButton
import com.example.ui.components.QuantimlLogo
import com.example.ui.components.QuantimlTextField
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlTheme
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryLight

/**
 * Login Screen
 * Styled in the Sleek Interface theme with email/mobile input, 4-6 digit PIN entry,
 * integration with Node.js Express backend using JWT tokens, demo credentials helper,
 * and links to registration.
 */
@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit,
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier
) {
  var identifier by remember { mutableStateOf("arjun.sharma@example.com") }
  var passCode by remember { mutableStateOf("1234") }
  var isPassCodeVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successFeedback by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var showConfigDialog by remember { mutableStateOf(false) }

  val spacing = QuantimlTheme.spacing
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  val uiState by viewModel.uiState.collectAsState()
  val backendUrl by viewModel.backendUrl.collectAsState()

  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is AuthUiState.Loading -> {
        isLoading = true
        errorMessage = null
        successFeedback = null
      }
      is AuthUiState.Success -> {
        isLoading = false
        successFeedback = state.message
        snackbarHostState.showSnackbar(state.message)
        onLoginSuccess()
      }
      is AuthUiState.Error -> {
        isLoading = false
        errorMessage = state.message
        snackbarHostState.showSnackbar(state.message)
      }
      is AuthUiState.Idle -> {
        isLoading = false
      }
    }
  }

  if (showConfigDialog) {
    BackendConfigDialog(
      currentUrl = backendUrl,
      onDismiss = { showConfigDialog = false },
      onSaveUrl = { newUrl ->
        viewModel.updateBackendUrl(newUrl)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Backend set to $newUrl")
        }
      }
    )
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("login_screen"),
    containerColor = MistGrey,
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = spacing.m)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(spacing.m))

        // Backend Status Banner
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { showConfigDialog = true }
            .testTag("backend_status_pill"),
          colors = CardDefaults.cardColors(containerColor = PureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(GrowthGreen)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Node.js REST API • JWT Protected",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 11.sp
                  )
                )
                Text(
                  text = backendUrl,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryLight,
                    fontSize = 10.sp
                  )
                )
              }
            }
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = "Configure Backend URL",
              tint = SignalBlue,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Brand Logo container
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          QuantimlLogo(size = 46.dp)
        }

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
          text = "Welcome back",
          style = MaterialTheme.typography.headlineLarge.copy(
            color = InkNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
          )
        )

        Spacer(modifier = Modifier.height(spacing.xxs))

        Text(
          text = "Log in to track your round-ups and growth portfolio.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextSecondaryLight,
            fontSize = 14.sp
          )
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Credentials Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PureWhite)
            .padding(20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            // Quick Demo Credential Pill
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SignalBlue.copy(alpha = 0.08f))
                .clickable {
                  identifier = "arjun.sharma@example.com"
                  passCode = "1234"
                  errorMessage = null
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("login_demo_fill_button"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Rounded.FlashOn,
                  contentDescription = null,
                  tint = SignalBlue,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Fill Demo: arjun.sharma@example.com (PIN: 1234)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SignalBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                  )
                )
              }
            }

            // Identifier Input
            QuantimlTextField(
              value = identifier,
              onValueChange = {
                identifier = it
                errorMessage = null
              },
              label = "Email or Mobile Number",
              placeholder = "e.g. arjun.sharma@example.com or 9876543210",
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Email,
                  contentDescription = null,
                  tint = TextSecondaryLight
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              testTag = "login_identifier_input"
            )

            // Passcode Input
            QuantimlTextField(
              value = passCode,
              onValueChange = {
                if (it.length <= 6) passCode = it
                errorMessage = null
              },
              label = "Security PIN / Passcode",
              placeholder = "Enter 4-digit PIN",
              visualTransformation = if (isPassCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Lock,
                  contentDescription = null,
                  tint = TextSecondaryLight
                )
              },
              trailingIcon = {
                IconButton(onClick = { isPassCodeVisible = !isPassCodeVisible }) {
                  Icon(
                    imageVector = if (isPassCodeVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = "Toggle PIN visibility",
                    tint = TextSecondaryLight
                  )
                }
              },
              testTag = "login_passcode_input"
            )

            AnimatedVisibility(
              visible = errorMessage != null,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Card(
                colors = CardDefaults.cardColors(containerColor = AlertCoral.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_error_message")
              ) {
                Text(
                  text = errorMessage ?: "",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = AlertCoral,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  modifier = Modifier.padding(10.dp)
                )
              }
            }

            AnimatedVisibility(
              visible = successFeedback != null,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Card(
                colors = CardDefaults.cardColors(containerColor = GrowthGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = GrowthGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = successFeedback ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = GrowthGreen,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            QuantimlButton(
              text = if (isLoading) "Authenticating with Node.js..." else "Sign In",
              onClick = {
                viewModel.login(identifier.trim(), passCode.trim())
              },
              isLoading = isLoading,
              variant = ButtonVariant.Primary,
              testTag = "login_submit_button"
            )
          }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Security Assurance Pill
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PureWhite.copy(alpha = 0.6f))
            .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Security,
            contentDescription = null,
            tint = GrowthGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Secured with JWT tokens & 256-bit encryption",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondaryLight,
              fontSize = 11.sp
            )
          )
        }
      }

      // Bottom Registration Link
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = spacing.m),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "New to Quantiml? ",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
        )
        TextButton(
          onClick = onNavigateToRegister,
          modifier = Modifier.testTag("login_to_register_button")
        ) {
          Text(
            text = "Create an account",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SignalBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          )
        }
      }
    }
  }
}
