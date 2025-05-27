package com.example.brainlog

import android.util.Log
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
                    onNavigateToLogin = onLogout,
                    onNavigateToMediaDetail = { mediaId, mediaType ->
                        navController.navigate("mediaDetail/$mediaId/${mediaType.name}")
                    }
                )
            }
            composable("mediaDetail/{mediaId}/{mediaType}") { backStackEntry ->
                val mediaID = backStackEntry.arguments?.getString("mediaId")?.toIntOrNull()
                val mediaType = backStackEntry.arguments?.getString("mediaType")?.let { MediumType.valueOf(it) }
                Log.d("MediaDetailNav", "Received raw ID: $mediaID, raw Type: $mediaType")

                if (mediaID != null && mediaType != null) {
                    MediaDetailScreen(
                        movieId = mediaID,
                        mediaType = mediaType,
                        navController = navController // <<<< DIESEN PARAMETER HINZUFÜGEN
                    )
                }  else {
                    Log.e(
                        "MediaDetailNav",
                        "Failed to parse arguments. Cannot show MediaDetailScreen. Parsed ID: $mediaID, Parsed Type: $mediaType"
                    )
                }
            }
        }
    }
}