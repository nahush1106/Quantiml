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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
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
 * Register Screen
 * Allows new users to create their account with Full Name, Email, Mobile (+91), and 4-digit Security PIN.
 * Integrates directly with the Node.js Express backend, acquiring a JWT token and syncing to local Room.
 */
@Composable
fun RegisterScreen(
  onRegisterSuccess: () -> Unit,
  onNavigateToLogin: () -> Unit,
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier
) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var mobileNumber by remember { mutableStateOf("") }
  var passCode by remember { mutableStateOf("") }
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
        onRegisterSuccess()
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
      .testTag("register_screen"),
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
            .testTag("backend_status_pill_register"),
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
            .size(60.dp)
            .clip(CircleShape)
            .background(PureWhite),
          contentAlignment = Alignment.Center
        ) {
          QuantimlLogo(size = 40.dp)
        }

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
          text = "Create an account",
          style = MaterialTheme.typography.headlineMedium.copy(
            color = InkNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
          )
        )

        Spacer(modifier = Modifier.height(spacing.xxs))

        Text(
          text = "Start auto-investing your spare change in minutes.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextSecondaryLight,
            fontSize = 13.sp
          )
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Registration Form Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PureWhite)
            .padding(20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
            // Quick Demo Credential Pill
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GrowthGreen.copy(alpha = 0.08f))
                .clickable {
                  fullName = "Priya Patel"
                  email = "priya.patel@example.com"
                  mobileNumber = "9876501234"
                  passCode = "1234"
                  errorMessage = null
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("register_demo_fill_button"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Rounded.FlashOn,
                  contentDescription = null,
                  tint = GrowthGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Fill Sample: Priya Patel (priya.patel@example.com)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GrowthGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                  )
                )
              }
            }

            // Full Name Input
            QuantimlTextField(
              value = fullName,
              onValueChange = {
                fullName = it
                errorMessage = null
              },
              label = "Full Legal Name",
              placeholder = "e.g. Priya Patel",
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Person,
                  contentDescription = null,
                  tint = TextSecondaryLight
                )
              },
              testTag = "register_name_input"
            )

            // Email Input
            QuantimlTextField(
              value = email,
              onValueChange = {
                email = it
                errorMessage = null
              },
              label = "Email Address",
              placeholder = "priya.patel@example.com",
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Email,
                  contentDescription = null,
                  tint = TextSecondaryLight
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              testTag = "register_email_input"
            )

            // Mobile Input
            QuantimlTextField(
              value = mobileNumber,
              onValueChange = {
                if (it.length <= 10) mobileNumber = it
                errorMessage = null
              },
              label = "Mobile Number (10 digits)",
              placeholder = "9876501234",
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Phone,
                  contentDescription = null,
                  tint = TextSecondaryLight
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              testTag = "register_mobile_input"
            )

            // 4-Digit PIN Input
            QuantimlTextField(
              value = passCode,
              onValueChange = {
                if (it.length <= 6) passCode = it
                errorMessage = null
              },
              label = "Set Security PIN (4-6 digits)",
              placeholder = "Create 4-digit PIN",
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
              testTag = "register_passcode_input"
            )

            AnimatedVisibility(
              visible = errorMessage != null,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Card(
                colors = CardDefaults.cardColors(containerColor = AlertCoral.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("register_error_message")
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
              text = if (isLoading) "Creating Account & Issuing JWT..." else "Create Account",
              onClick = {
                viewModel.register(fullName.trim(), email.trim(), mobileNumber.trim(), passCode.trim())
              },
              isLoading = isLoading,
              variant = ButtonVariant.Growth,
              testTag = "register_submit_button"
            )
          }
        }

        Spacer(modifier = Modifier.height(spacing.s))

        // Compliance Note
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Security,
            contentDescription = null,
            tint = GrowthGreen,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Compliant with SEBI & AMFI mutual fund guidelines",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondaryLight,
              fontSize = 11.sp
            )
          )
        }
      }

      // Bottom Login Link
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = spacing.m),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Already registered? ",
          style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
        )
        TextButton(
          onClick = onNavigateToLogin,
          modifier = Modifier.testTag("register_to_login_button")
        ) {
          Text(
            text = "Sign In",
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
