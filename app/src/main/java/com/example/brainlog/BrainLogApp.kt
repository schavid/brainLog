package com.example.brainlog


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brainlog.view.Home
import com.example.brainlog.view.MediaDetailScreen
import com.example.brainlog.viewmodel.MediumType


@Composable
fun BrainLogApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            Home(navController)
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