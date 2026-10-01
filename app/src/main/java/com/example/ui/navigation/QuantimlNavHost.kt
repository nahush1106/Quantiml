package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.AuthRepository
import com.example.data.database.QuantimlDatabase
import com.example.data.repository.TransactionRepository
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.design.DesignSystemPreviewScreen
import com.example.ui.screens.growth.GrowthScreen
import com.example.ui.screens.growth.GrowthViewModel
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.history.HistoryViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.splash.SplashScreen

/**
 * Main Navigation Host coordinating App flow across Splash, Onboarding, Authentication, and Main Experience
 */
@Composable
fun QuantimlNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  startDestination: String = Screen.Splash.route
) {
  val context = LocalContext.current
  val database = remember { QuantimlDatabase.getDatabase(context) }
  val authRepository = remember { AuthRepository(context, database.userDao()) }
  val authViewModel = remember { AuthViewModel(authRepository) }

  val transactionRepository = remember { TransactionRepository(database.transactionDao(), authRepository) }
  val homeViewModel = remember { HomeViewModel(transactionRepository, authRepository) }
  val historyViewModel = remember { HistoryViewModel(transactionRepository) }
  val growthViewModel = remember { GrowthViewModel(transactionRepository) }
  val profileViewModel = remember { ProfileViewModel(authRepository) }

  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier
  ) {
    // Splash Screen with Auth State Detection
    composable(Screen.Splash.route) {
      val session by authRepository.userSessionFlow.collectAsState(initial = null)
      SplashScreen(
        onAnimationFinished = {
          val destination = if (session?.isLoggedIn == true) Screen.Home.route else Screen.Onboarding.route
          navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
          }
        }
      )
    }

    // Onboarding Screen
    composable(Screen.Onboarding.route) {
      OnboardingScreen(
        onFinishOnboarding = {
          navController.navigate(Screen.Register.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
          }
        },
        onLoginClick = {
          navController.navigate(Screen.Login.route)
        }
      )
    }

    // Login Screen
    composable(Screen.Login.route) {
      LoginScreen(
        onLoginSuccess = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
          }
        },
        onNavigateToRegister = {
          navController.navigate(Screen.Register.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
          }
        },
        viewModel = authViewModel
      )
    }

    // Register Screen
    composable(Screen.Register.route) {
      RegisterScreen(
        onRegisterSuccess = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Register.route) { inclusive = true }
          }
        },
        onNavigateToLogin = {
          navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Register.route) { inclusive = true }
          }
        },
        viewModel = authViewModel
      )
    }

    // Home Screen (Phase 4)
    composable(Screen.Home.route) {
      HomeScreen(
        viewModel = homeViewModel,
        onNavigateToHistory = { navController.navigate(Screen.Transactions.route) },
        onNavigateToGrowth = { navController.navigate(Screen.Investments.route) },
        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
        onViewThresholdDetails = { navController.navigate(Screen.Investments.route) }
      )
    }

    // Transactions History Screen (Phase 5)
    composable(Screen.Transactions.route) {
      HistoryScreen(
        viewModel = historyViewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToHome = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
          }
        },
        onNavigateToGrowth = { navController.navigate(Screen.Investments.route) },
        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
        onFabClick = {
          navController.navigate(Screen.Home.route)
        }
      )
    }

    // Growth / Investment Portfolio Screen (Phase 6)
    composable(Screen.Investments.route) {
      GrowthScreen(
        viewModel = growthViewModel,
        onNavigateToHome = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
          }
        },
        onNavigateToHistory = {
          navController.navigate(Screen.Transactions.route)
        },
        onNavigateToProfile = {
          navController.navigate(Screen.Profile.route)
        },
        onFabClick = {
          navController.navigate(Screen.Home.route)
        }
      )
    }

    // Profile & Settings Screen (Phase 7)
    composable(Screen.Profile.route) {
      ProfileScreen(
        viewModel = profileViewModel,
        onNavigateToHome = {
          navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
          }
        },
        onNavigateToHistory = {
          navController.navigate(Screen.Transactions.route)
        },
        onNavigateToGrowth = {
          navController.navigate(Screen.Investments.route)
        },
        onLogoutSuccess = {
          navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
          }
        },
        onFabClick = {
          navController.navigate(Screen.Home.route)
        }
      )
    }

    // Design System / Component Sandbox Preview
    composable(Screen.DesignSystemPreview.route) {
      DesignSystemPreviewScreen()
    }
  }
}
