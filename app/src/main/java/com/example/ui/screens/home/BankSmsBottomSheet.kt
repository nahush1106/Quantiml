package com.example.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.sms.BankSmsParser
import com.example.data.sms.ParsedBankTransaction
import com.example.ui.components.FinancialSize
import com.example.ui.components.FinancialText
import com.example.ui.components.QuantimlBadge
import com.example.ui.components.QuantimlButton
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.TextSecondaryLight

/**
 * Bank SMS Auto-Detection & Testing Bottom Sheet
 * Allows users to:
 * 1. Check & grant SMS permission for live telecom debit alert intercepting.
 * 2. Test live regex parsing with standard Indian bank formats (HDFC, ICICI, SBI, Axis, Paytm).
 * 3. Type or paste any raw bank SMS message to verify extraction.
 * 4. Commit parsed bank transaction directly into their Room database and wallet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSmsBottomSheet(
  onDismiss: () -> Unit,
  onProcessSmsTransaction: (merchant: String, amount: Double, rawSms: String) -> Unit,
  activeRoundUpTarget: Int = 10,
  activeMultiplier: Double = 1.0,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current

  var hasSmsPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECEIVE_SMS
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasSmsPermission = isGranted
  }

  var selectedPresetIndex by remember { mutableStateOf(0) }
  var smsText by remember {
    mutableStateOf(BankSmsParser.SAMPLE_BANK_MESSAGES[0].sampleText)
  }

  var parsedResult by remember(smsText, activeRoundUpTarget, activeMultiplier) {
    mutableStateOf<ParsedBankTransaction?>(
      BankSmsParser.parse(
        body = smsText,
        roundToMultiple = activeRoundUpTarget,
        multiplier = activeMultiplier
      )
    )
  }

  LaunchedEffect(smsText, activeRoundUpTarget, activeMultiplier) {
    parsedResult = BankSmsParser.parse(
      body = smsText,
      roundToMultiple = activeRoundUpTarget,
      multiplier = activeMultiplier
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGrey) },
    modifier = modifier.testTag("bank_sms_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SignalBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Rounded.Sms,
              contentDescription = null,
              tint = SignalBlue,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = "Bank SMS Auto-Detection",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = InkNavy,
                fontSize = 17.sp
              )
            )
            Text(
              text = "Automatic spare-change from debit SMS",
              style = MaterialTheme.typography.bodySmall.copy(
                color = InkNavy.copy(alpha = 0.5f),
                fontSize = 11.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("sms_sheet_close_btn")
        ) {
          Icon(Icons.Rounded.Close, contentDescription = "Close", tint = InkNavy.copy(alpha = 0.5f))
        }
      }

      // Permission status banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (hasSmsPermission) GrowthGreen.copy(alpha = 0.08f) else SignalBlue.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (hasSmsPermission) GrowthGreen.copy(alpha = 0.3f) else SignalBlue.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              if (hasSmsPermission) Icons.Rounded.CheckCircle else Icons.Rounded.AutoAwesome,
              contentDescription = null,
              tint = if (hasSmsPermission) GrowthGreen else SignalBlue,
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = if (hasSmsPermission) "Live SMS Listener Active" else "Enable Background SMS Listener",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = InkNavy,
                  fontSize = 13.sp
                )
              )
              Text(
                text = if (hasSmsPermission)
                  "Incoming bank UPI debits are parsed instantly on device."
                else
                  "Grant SMS permission so Quantiml can auto-detect bank spends.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = InkNavy.copy(alpha = 0.6f),
                  fontSize = 11.sp
                )
              )
            }
          }

          if (!hasSmsPermission) {
            Surface(
              onClick = { permissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
              shape = RoundedCornerShape(8.dp),
              color = SignalBlue,
              modifier = Modifier.testTag("grant_sms_permission_btn")
            ) {
              Text(
                text = "Grant",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Bank Presets Selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Select Sample Bank Debit Alert",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
          )
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(BankSmsParser.SAMPLE_BANK_MESSAGES.indices.toList()) { index ->
            val preset = BankSmsParser.SAMPLE_BANK_MESSAGES[index]
            val isSelected = selectedPresetIndex == index
            Surface(
              onClick = {
                selectedPresetIndex = index
                smsText = preset.sampleText
              },
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) InkNavy else MistGrey,
              modifier = Modifier.testTag("sms_preset_chip_$index")
            ) {
              Text(
                text = preset.bank,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) PureWhite else InkNavy,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }
      }

      // Raw SMS Text Box
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Bank SMS Message Content",
          style = MaterialTheme.typography.labelSmall.copy(
            color = InkNavy.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
          )
        )

        OutlinedTextField(
          value = smsText,
          onValueChange = {
            smsText = it
            selectedPresetIndex = -1
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sms_content_input"),
          minLines = 3,
          maxLines = 4,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SignalBlue,
            unfocusedBorderColor = BorderGrey,
            focusedContainerColor = PureWhite,
            unfocusedContainerColor = MistGrey.copy(alpha = 0.4f),
            focusedTextColor = InkNavy,
            unfocusedTextColor = InkNavy
          ),
          textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp)
        )
      }

      // Parser Result Card
      val result = parsedResult
      if (result != null) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MistGrey,
          modifier = Modifier.fillMaxWidth().testTag("sms_parsed_result_card")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  Icons.Rounded.AccountBalance,
                  contentDescription = null,
                  tint = SignalBlue,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = result.bankName,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 13.sp
                  )
                )
              }

              QuantimlBadge(
                text = result.category,
                textColor = if (result.category == "DISCRETIONARY") SignalBlue else GrowthGreen,
                backgroundColor = (if (result.category == "DISCRETIONARY") SignalBlue else GrowthGreen).copy(alpha = 0.12f)
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "IDENTIFIED MERCHANT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = InkNavy.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                )
                Text(
                  text = result.merchant,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkNavy,
                    fontSize = 16.sp
                  )
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "DEBIT AMOUNT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = InkNavy.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                )
                FinancialText(
                  amount = result.amount,
                  size = FinancialSize.Headline,
                  showDecimals = true,
                  customColor = InkNavy
                )
              }
            }

            // Round-Up Spare Change highlight
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GrowthGreen.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Auto Round-Up to Wallet",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = GrowthGreen,
                  fontSize = 12.sp
                )
              )
              FinancialText(
                amount = result.roundUpAmount,
                size = FinancialSize.Body,
                isPositiveDelta = true,
                showDecimals = true,
                customColor = GrowthGreen
              )
            }
          }
        }

        // Action Button
        QuantimlButton(
          text = "Save & Add to Wallet (+₹${result.roundUpAmount.toInt()})",
          onClick = {
            onProcessSmsTransaction(result.merchant, result.amount, result.rawSms)
            onDismiss()
          },
          testTag = "confirm_process_sms_btn"
        )
      } else {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = AlertCoral.copy(alpha = 0.1f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = AlertCoral, modifier = Modifier.size(18.dp))
            Text(
              text = "Could not detect a bank debit amount in this message. Ensure it contains a currency (Rs / INR) and debit action (debited, paid, spent).",
              style = MaterialTheme.typography.bodySmall.copy(color = InkNavy, fontSize = 12.sp)
            )
          }
        }
      }

      // Security Notice
      Text(
        text = "Quantiml strictly parses bank transactions locally on your phone. OTPs, personal texts, and sensitive credentials are never stored or transmitted.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = InkNavy.copy(alpha = 0.5f),
          fontSize = 11.sp,
          lineHeight = 15.sp
        )
      )
    }
  }
}
