package com.example.brainlog


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brainlog.view.AuthScreen
import com.example.brainlog.viewmodel.AuthViewModel


// Definiere die Namen für die Top-Level-Routen
object MainDestinations {
    const val AUTH_ROUTE = "auth"
    const val MAIN_APP_ROUTE = "main_app"
}

@Composable
fun BrainLogApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val topLevelNavController = rememberNavController()

    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = if (currentUser == null) {
        MainDestinations.AUTH_ROUTE
    } else {
        MainDestinations.MAIN_APP_ROUTE
    }

    NavHost(
        navController = topLevelNavController,
        startDestination = startDestination // Dynamisches Startziel
    ) {
        composable(MainDestinations.AUTH_ROUTE) {
            AuthScreen(
                authViewModel = authViewModel, // AuthViewModel übergeben
                onAuthSuccess = {
                    // Wenn Login/Register erfolgreich war:
                    // Navigiere zur Haupt-App und entferne den Auth-Screen aus dem Backstack,
                    // damit der Benutzer nicht per "Zurück"-Taste zum Login kommt.
                    topLevelNavController.navigate(MainDestinations.MAIN_APP_ROUTE) {
                        popUpTo(MainDestinations.AUTH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MainDestinations.MAIN_APP_ROUTE) {
            MainAppContent(
                onLogout = {
                    authViewModel.logout()
                    topLevelNavController.navigate(MainDestinations.AUTH_ROUTE) {
                        popUpTo(MainDestinations.MAIN_APP_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

}