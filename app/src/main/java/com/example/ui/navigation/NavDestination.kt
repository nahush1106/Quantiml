package com.example.ui.navigation

/**
 * Quantiml Navigation destinations and routes
 */
sealed class Screen(val route: String) {
  data object Splash : Screen("splash")
  data object Onboarding : Screen("onboarding")
  data object Login : Screen("login")
  data object Register : Screen("register")

  // Main Bottom Navigation Tabs
  data object Home : Screen("home")
  data object Transactions : Screen("transactions")
  data object Wallet : Screen("wallet")
  data object Investments : Screen("investments")
  data object Profile : Screen("profile")

  // Additional Screens
  data object Insights : Screen("insights")
  data object DesignSystemPreview : Screen("design_system_preview")
}
