package com.example.brainlog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brainlog.model.MediumType
import com.example.brainlog.view.Home
import com.example.brainlog.view.MediaDetailScreen
import com.example.brainlog.view.MyBottomAppBar
import com.example.brainlog.view.ProfileScreen
import com.example.brainlog.viewmodel.AuthViewModel

@Composable
fun MainAppContent(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {

            MyBottomAppBar(
                onHomeClick = { navController.navigate("home") },
                onProfileClick = { navController.navigate("profile") },
                navController = navController
            )

        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home",  modifier = Modifier.padding(innerPadding)) {
            composable("home") {
                Home(navController)
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToLogin = onLogout
                )
            }
            composable("mediaDetail/{mediaId}/{mediaType}") { backStackEntry ->
                val mediaID = backStackEntry.arguments?.getString("mediaId")?.toIntOrNull()
                val mediaType = backStackEntry.arguments?.getString("mediaType")?.let { MediumType.valueOf(it) }

                // Falls movieId oder mediaType null sind, handle diesen Fall
                if (mediaID != null && mediaType != null) {
                    MediaDetailScreen(movieId = mediaID, mediaType = mediaType)
                }
            }
        }
    }
}