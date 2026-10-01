package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.InkNavy
import com.example.ui.theme.MistGrey
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalBlue

enum class SleekTab {
  Home,
  History,
  Growth,
  Profile
}

/**
 * Sleek Interface Navigation Bar:
 * White background, border top 1dp #E4E8F0, active Signal Blue indicator,
 * elevated centered round FAB button with + icon and blue glow shadow.
 */
@Composable
fun SleekBottomNav(
  selectedTab: SleekTab = SleekTab.Home,
  onTabSelected: (SleekTab) -> Unit = {},
  onFabClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .testTag("sleek_bottom_nav")
  ) {
    // Base nav bar
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(72.dp),
      color = PureWhite,
      tonalElevation = 0.dp
    ) {
      Column {
        HorizontalDivider(color = BorderGrey, thickness = 1.dp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(71.dp)
            .padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Home Tab
          SleekNavItem(
            label = "Home",
            icon = Icons.Rounded.Home,
            isSelected = selectedTab == SleekTab.Home,
            onClick = { onTabSelected(SleekTab.Home) }
          )

          // History Tab
          SleekNavItem(
            label = "History",
            icon = Icons.Rounded.ReceiptLong,
            isSelected = selectedTab == SleekTab.History,
            onClick = { onTabSelected(SleekTab.History) }
          )

          // Center FAB Spacer reservation
          Box(modifier = Modifier.size(52.dp))

          // Growth Tab
          SleekNavItem(
            label = "Growth",
            icon = Icons.Rounded.TrendingUp,
            isSelected = selectedTab == SleekTab.Growth,
            onClick = { onTabSelected(SleekTab.Growth) }
          )

          // Profile Tab
          SleekNavItem(
            label = "Profile",
            icon = Icons.Rounded.Person,
            isSelected = selectedTab == SleekTab.Profile,
            onClick = { onTabSelected(SleekTab.Profile) }
          )
        }
      }
    }

    // Elevated Center Floating Action Button (-mt-10 in html)
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(y = (-18).dp)
        .size(54.dp)
        .shadow(8.dp, CircleShape, spotColor = SignalBlue.copy(alpha = 0.45f))
        .clip(CircleShape)
        .background(SignalBlue)
        .border(4.dp, MistGrey, CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onFabClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Rounded.Add,
        contentDescription = "Quick Add Transaction",
        tint = PureWhite,
        modifier = Modifier.size(28.dp)
      )
    }
  }
}

@Composable
private fun SleekNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val activeColor = SignalBlue
  val inactiveColor = InkNavy.copy(alpha = 0.3f)

  Column(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) activeColor else inactiveColor,
      modifier = Modifier.size(24.dp)
    )
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        color = if (isSelected) activeColor else inactiveColor
      )
    )
  }
}
