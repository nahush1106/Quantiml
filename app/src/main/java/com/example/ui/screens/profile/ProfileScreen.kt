package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ButtonVariant
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlButton
import com.example.ui.components.QuantimlCard
import com.example.ui.components.SleekBottomNav
import com.example.ui.components.SleekTab
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.QuantimlTheme
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryLight

/**
 * Phase 7: Profile, Savings Multipliers & Settings Screen
 * Features:
 * - KYC & User Identity Card (Full Name, Masked PAN, SEBI Compliant Badge)
 * - Micro-Savings Round-Up Rule Configurator (Round to nearest ₹10 vs ₹50)
 * - Multiplier Boost Selector (1.0x Standard, 1.5x Boost, 2.0x Double)
 * - Auto-Invest Threshold Selector (₹200, ₹500, ₹1000, ₹2000)
 * - Security & Verification Section (Biometrics, 256-bit encryption, PIN change)
 * - Bank Account & UPI Autopay Management
 * - Logout confirmation dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: ProfileViewModel,
  onNavigateToHome: () -> Unit = {},
  onNavigateToHistory: () -> Unit = {},
  onNavigateToGrowth: () -> Unit = {},
  onLogoutSuccess: () -> Unit = {},
  onFabClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showLogoutDialog by remember { mutableStateOf(false) }
  var showServerConfigDialog by remember { mutableStateOf(false) }
  var customUrlInput by remember { mutableStateOf(uiState.currentBaseUrl) }
  val spacing = QuantimlTheme.spacing

  val displayName = if (uiState.userSession.fullName.isNotBlank()) {
    uiState.userSession.fullName
  } else {
    "Arjun Sharma"
  }

  val displayEmail = if (uiState.userSession.email.isNotBlank()) {
    uiState.userSession.email
  } else {
    "arjun.sharma@example.com"
  }

  val displayInitials = remember(displayName) {
    val parts = displayName.trim().split(" ")
    if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
    else displayName.take(2).uppercase()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MistGrey)
      .testTag("profile_screen"),
    containerColor = MistGrey,
    bottomBar = {
      SleekBottomNav(
        selectedTab = SleekTab.Profile,
        onTabSelected = { tab ->
          when (tab) {
            SleekTab.Home -> onNavigateToHome()
            SleekTab.History -> onNavigateToHistory()
            SleekTab.Growth -> onNavigateToGrowth()
            SleekTab.Profile -> { /* Already on Profile */ }
          }
        },
        onFabClick = onFabClick
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = spacing.m),
      verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
      item { Spacer(modifier = Modifier.height(spacing.xxs)) }

      // 1. Screen Title
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs)
            .testTag("profile_header_section")
        ) {
          Text(
            text = "ACCOUNT & SETTINGS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = InkNavy.copy(alpha = 0.5f),
              letterSpacing = 1.2.sp
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Settings & Controls",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = InkNavy,
              fontSize = 24.sp
            )
          )
        }
      }

      // 2. User Identity & KYC Card
      item {
        QuantimlCard(
          shape = RoundedCornerShape(20.dp),
          testTag = "profile_user_card"
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(InkNavy),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = displayInitials,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = PureWhite,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = displayName,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 16.sp
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  Icons.Rounded.CheckCircle,
                  contentDescription = "Verified",
                  tint = GrowthGreen,
                  modifier = Modifier.size(16.dp)
                )
              }

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = displayEmail,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = InkNavy.copy(alpha = 0.5f),
                  fontSize = 12.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // KYC Status Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MistGrey)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "KYC STATUS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = InkNavy.copy(alpha = 0.5f),
                letterSpacing = 0.8.sp
              )
            )
            QuantimlBadge(
              text = uiState.kycStatus,
              textColor = GrowthGreen,
              backgroundColor = GrowthGreen.copy(alpha = 0.12f)
            )
          }
        }
      }

      // 3. Section Title: Savings Engine Controls
      item {
        Text(
          text = "Micro-Investment Engine Rules",
          style = MaterialTheme.typography.titleMedium.copy(
            color = InkNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        )
      }

      // 4. Savings Engine Card (Target, Multiplier, Threshold)
      item {
        QuantimlCard(
          shape = RoundedCornerShape(20.dp),
          testTag = "savings_controls_card"
        ) {
          // Rule A: Round-Up Multiple Target (₹10 vs ₹50)
          Column {
            Text(
              text = "Round-Up Multiple",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = InkNavy,
                fontSize = 14.sp
              )
            )
            Text(
              text = "Rounds spends to the nearest multiple (e.g. ₹93 -> ₹100 or ₹150)",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.5f),
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              listOf(10 to "Round to nearest ₹10", 50 to "Round to nearest ₹50").forEach { (target, label) ->
                val isSelected = uiState.roundUpTarget == target
                Surface(
                  onClick = { viewModel.setRoundUpTarget(target) },
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) InkNavy else MistGrey,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("round_up_target_${target}")
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) PureWhite else InkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Rule B: Multiplier Boost (1.0x, 1.5x, 2.0x)
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Savings Multiplier",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = InkNavy,
                  fontSize = 14.sp
                )
              )
              QuantimlBadge(
                text = "${uiState.multiplier}x Active",
                textColor = SignalBlue,
                backgroundColor = SignalBlue.copy(alpha = 0.1f)
              )
            }
            Text(
              text = "Multiplies every spare change round-up for accelerated investing",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.5f),
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(1.0 to "1.0x Standard", 1.5 to "1.5x Boost", 2.0 to "2.0x Double").forEach { (mult, label) ->
                val isSelected = uiState.multiplier == mult
                Surface(
                  onClick = { viewModel.setMultiplier(mult) },
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) SignalBlue else MistGrey,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("multiplier_chip_${mult}x")
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) PureWhite else InkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Rule C: Auto-Invest Deployment Threshold
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Auto-Deploy Threshold",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = InkNavy,
                  fontSize = 14.sp
                )
              )
              Text(
                text = "₹${uiState.autoInvestThreshold.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = GrowthGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              )
            }
            Text(
              text = "When wallet hits this amount, spare change auto-deploys into funds",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.5f),
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(200.0 to "₹200", 500.0 to "₹500", 1000.0 to "₹1,000", 2000.0 to "₹2,000").forEach { (th, label) ->
                val isSelected = uiState.autoInvestThreshold == th
                Surface(
                  onClick = { viewModel.setAutoInvestThreshold(th) },
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) GrowthGreen else MistGrey,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("threshold_chip_${th.toInt()}")
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) PureWhite else InkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Rule D: Auto-Deploy Investment Destination
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Auto-Deploy Asset Strategy",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = InkNavy,
                  fontSize = 14.sp
                )
              )
              QuantimlBadge(
                text = when (uiState.selectedAssetDestination) {
                  "DIGITAL_GOLD" -> "24K Gold"
                  "FLEXI_CAP" -> "Flexi-Cap"
                  else -> "Nifty 50"
                },
                textColor = SignalBlue,
                backgroundColor = SignalBlue.copy(alpha = 0.1f)
              )
            }
            Text(
              text = "Target mutual fund or digital gold asset for auto-deployed funds",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.5f),
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(
                "NIFTY_50_INDEX" to "Nifty 50",
                "DIGITAL_GOLD" to "24K Gold",
                "FLEXI_CAP" to "Flexi-Cap"
              ).forEach { (destKey, label) ->
                val isSelected = uiState.selectedAssetDestination == destKey
                Surface(
                  onClick = { viewModel.setAssetDestination(destKey) },
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) InkNavy else MistGrey,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("asset_dest_chip_$destKey")
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) PureWhite else InkNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 5. Section: Security & Payments
      item {
        Text(
          text = "Security & Linked Accounts",
          style = MaterialTheme.typography.titleMedium.copy(
            color = InkNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        )
      }

      // 6. Security Card
      item {
        QuantimlCard(
          shape = RoundedCornerShape(20.dp),
          testTag = "security_settings_card"
        ) {
          // Biometric Lock Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                .background(SignalBlue.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = SignalBlue, modifier = Modifier.size(20.dp))
              }
              Column {
                Text(
                  text = "Biometric / Fingerprint Unlock",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 14.sp
                  )
                )
                Text(
                  text = "Quick biometric authentication",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = InkNavy.copy(alpha = 0.5f),
                    fontSize = 11.sp
                  )
                )
              }
            }

            Switch(
              checked = uiState.isBiometricEnabled,
              onCheckedChange = { viewModel.toggleBiometric(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = SignalBlue,
                uncheckedThumbColor = PureWhite,
                uncheckedTrackColor = BorderGrey
              ),
              modifier = Modifier.testTag("biometric_switch")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Linked UPI Autopay Mandate
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(GrowthGreen.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(20.dp))
              }
              Column {
                Text(
                  text = "UPI Autopay Mandate",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 14.sp
                  )
                )
                Text(
                  text = uiState.linkedUpiId,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = InkNavy.copy(alpha = 0.5f),
                    fontSize = 11.sp
                  )
                )
              }
            }

            QuantimlBadge(
              text = "Active",
              textColor = GrowthGreen,
              backgroundColor = GrowthGreen.copy(alpha = 0.1f)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Bank-Grade Encryption Notice
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MistGrey)
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = InkNavy.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            Text(
              text = "256-bit AES banking security & SEBI compliant data encryption",
              style = MaterialTheme.typography.labelSmall.copy(
                color = InkNavy.copy(alpha = 0.6f),
                fontSize = 10.sp
              )
            )
          }
        }
      }

      // 7. Backend Server API & Live Sync Card
      item {
        QuantimlCard(
          shape = RoundedCornerShape(20.dp),
          testTag = "backend_server_settings_card"
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(SignalBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Rounded.Dns,
                  contentDescription = null,
                  tint = SignalBlue,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Text(
                  text = "Node.js REST API Server",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 14.sp
                  )
                )
                Text(
                  text = uiState.currentBaseUrl,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = InkNavy.copy(alpha = 0.5f),
                    fontSize = 11.sp
                  )
                )
              }
            }

            QuantimlBadge(
              text = if (uiState.userSession.isBackendConnected) "ONLINE" else "LOCAL",
              textColor = if (uiState.userSession.isBackendConnected) GrowthGreen else InkNavy.copy(alpha = 0.6f),
              backgroundColor = if (uiState.userSession.isBackendConnected) GrowthGreen.copy(alpha = 0.12f) else MistGrey
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              onClick = {
                customUrlInput = uiState.currentBaseUrl
                showServerConfigDialog = true
              },
              shape = RoundedCornerShape(10.dp),
              color = MistGrey,
              modifier = Modifier
                .weight(1f)
                .testTag("configure_backend_url_btn")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Rounded.Settings,
                  contentDescription = null,
                  tint = InkNavy,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Configure URL",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy
                  )
                )
              }
            }
          }
        }
      }

      // 8. Logout Button
      item {
        QuantimlButton(
          text = "Sign Out",
          onClick = { showLogoutDialog = true },
          variant = ButtonVariant.Secondary,
          leadingIcon = {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AlertCoral)
          },
          testTag = "profile_logout_btn"
        )
      }

      item { Spacer(modifier = Modifier.height(spacing.l)) }
    }
  }

  // Backend Server Configuration Dialog
  if (showServerConfigDialog) {
    AlertDialog(
      onDismissRequest = { showServerConfigDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Rounded.Dns, contentDescription = null, tint = SignalBlue, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Node.js Server URL",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = InkNavy)
          )
        }
      },
      text = {
        Column {
          Text(
            text = "Configure the base URL for the Quantiml Express backend API server (runs on port 5000):",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = customUrlInput,
            onValueChange = { customUrlInput = it },
            label = { Text("Base URL") },
            placeholder = { Text("http://10.0.2.2:5000/") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("backend_url_input_field")
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "• Android Emulator: http://10.0.2.2:5000/\n• Local Wi-Fi Device: http://192.168.1.X:5000/\n• Cloud Deployed: https://your-server.app/",
            style = MaterialTheme.typography.labelSmall.copy(color = InkNavy.copy(alpha = 0.5f), fontSize = 10.sp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val trimmed = customUrlInput.trim()
            if (trimmed.isNotBlank()) {
              viewModel.updateBaseUrl(trimmed)
            }
            showServerConfigDialog = false
          },
          modifier = Modifier.testTag("dialog_save_backend_url_btn")
        ) {
          Text("Save & Apply", color = SignalBlue, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showServerConfigDialog = false }) {
          Text("Cancel", color = InkNavy)
        }
      },
      containerColor = PureWhite,
      shape = RoundedCornerShape(16.dp)
    )
  }

  // Logout Dialog
  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutDialog = false },
      title = {
        Text(
          text = "Sign Out of Quantiml?",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = InkNavy)
        )
      },
      text = {
        Text(
          text = "Your spare change round-ups and invested portfolios remain completely safe and protected under SEBI mutual fund custody.",
          style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showLogoutDialog = false
            viewModel.logout(onLogoutSuccess)
          },
          modifier = Modifier.testTag("dialog_confirm_logout_btn")
        ) {
          Text("Sign Out", color = AlertCoral, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutDialog = false }) {
          Text("Cancel", color = InkNavy)
        }
      },
      containerColor = PureWhite,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
