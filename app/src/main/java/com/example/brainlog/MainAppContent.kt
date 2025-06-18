package com.example.brainlog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brainlog.model.MediumType
import com.example.brainlog.view.Home
import com.example.brainlog.view.MediaDetailScreen
import com.example.brainlog.view.MyBottomAppBar
import com.example.brainlog.view.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // 1. Das "Gehirn" für die TopAppBar wird hier, auf der höchsten Ebene, erstellt.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // 2. Der Zustand für den Inhalt der TopAppBar, genau wie vorher.
    var topAppBarContent: (@Composable () -> Unit)? by remember { mutableStateOf(null) }

    // Wichtig: Setzt die TopAppBar zurück, wenn wir den ProfileScreen verlassen,
    // damit andere Screens keine TopAppBar haben (oder ihre eigene setzen können).
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        if (currentRoute != "profile") {
            topAppBarContent = null
        }
    }

    // 3. Das Haupt-Scaffold. Es bekommt jetzt den nestedScroll-Modifier.
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            topAppBarContent?.invoke()
        },
        bottomBar = {
            MyBottomAppBar(
                onHomeClick = { navController.navigate("home") },
                onProfileClick = { navController.navigate("profile") },
                navController = navController
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                Home(navController)
            }
            composable("profile") {
                // 4. Der ProfileScreen bekommt jetzt BEIDES übergeben:
                //    - die Setter-Funktion UND das scrollBehavior-Objekt.
                ProfileScreen(
                    onNavigateToLogin = onLogout,
                    onNavigateToMediaDetail = { mediaId, mediaType ->
                        navController.navigate("mediaDetail/$mediaId/${mediaType.name}")
                    },
                    scrollBehavior = scrollBehavior,
                    onSetTopAppBar = { newContent ->
                        topAppBarContent = newContent
                    }
                )
            }
            composable("mediaDetail/{mediaId}/{mediaType}") { backStackEntry ->
                val mediaID = backStackEntry.arguments?.getString("mediaId")?.toIntOrNull()
                val mediaType = backStackEntry.arguments?.getString("mediaType")?.let { MediumType.valueOf(it) }

                if (mediaID != null && mediaType != null) {
                    MediaDetailScreen(
                        movieId = mediaID,
                        mediaType = mediaType,
                        navController = navController
                    )
                }
            }
        }
    }
}
