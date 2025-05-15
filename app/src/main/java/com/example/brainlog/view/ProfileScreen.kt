package com.example.brainlog.view


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.brainlog.model.Medium
import com.example.brainlog.model.Movie
import com.example.brainlog.model.Series
import com.example.brainlog.model.UserDocument
import com.example.brainlog.ui.theme.CustomTypography
import com.example.brainlog.viewmodel.ProfileScreenViewModel
import com.example.brainlog.viewmodel.UserMediaListUiState
import com.example.brainlog.viewmodel.UserProfileUiState


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfileViewModel: ProfileScreenViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMediaDetail: (id: Int, type: String) -> Unit
) {
    val uiState by userProfileViewModel.uiState.collectAsStateWithLifecycle()
    val userMediaListState by userProfileViewModel.userMediaListState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Dieses Padding ist für den *Inhalt innerhalb* des Screens
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState){
            is UserProfileUiState.Loading -> {
                CircularProgressIndicator()
                Text("Loading Profile...")
            }
            is UserProfileUiState.Success -> {
                val user: UserDocument = (uiState as UserProfileUiState.Success).userDocument

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(user.username, style = CustomTypography.headlineLarge)

                        Button(
                            onClick = onNavigateToLogin, // Löst den Logout-Prozess aus
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout Icon",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Logout")
                        }

                    }

                Text("WatchList", style = MaterialTheme.typography.headlineLarge)

                when (val mediaState = userMediaListState) {
                    is UserMediaListUiState.Idle -> {
                        // Nichts anzeigen oder eine initiale Nachricht
                    }
                    is UserMediaListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Lade Medien...", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is UserMediaListUiState.Success -> {
                        if (mediaState.mediaItems.isEmpty()) {
                            Text("Du hast noch keine Medien zu deiner Watchlist hinzugefügt.")
                        } else {
                            FlowRow (
                                modifier = Modifier.fillMaxSize(),
                                mainAxisSpacing = 8.dp, // Abstand zwischen den Elementen
                                crossAxisSpacing = 8.dp, // Abstand zwischen den Zeilen

                            // Damit die LazyColumn die Breite füllt
                            ) {
                                mediaState.mediaItems.forEach{ medium ->
                                    // Erstelle eine Composable für ein einzelnes Listenelement
                                    MediaListItem(
                                        medium = medium,
                                        onClick = {
                                            onNavigateToMediaDetail(medium.id, medium.type)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp)) // Abstand zwischen Elementen
                                }
                            }
                        }
                    }
                    is UserMediaListUiState.Error -> {
                        Text(
                            text = "Fehler beim Laden der Medien: ${mediaState.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is UserMediaListUiState.NoMediaFound -> {
                        Text("Keine Medien in deiner Watchlist gefunden.")
                    }
                }

            }
            is UserProfileUiState.Error -> {
                Text(
                    text = "Fehler:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = (uiState as UserProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { userProfileViewModel.fetchUserProfileThenMedia() }) { // ViewModel-Funktion umbenannt
                    Text("Erneut versuchen")
                }
            }
            is UserProfileUiState.NotLoggedIn -> {
                Text("Du bist nicht angemeldet.", style = MaterialTheme.typography.titleMedium)
                Text("Bitte melde dich an, um dein Profil zu sehen.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToLogin) {
                    Text("Zum Login")
                }
            }
            is UserProfileUiState.ProfileNotFound -> {
                Text("Profil nicht gefunden.", style = MaterialTheme.typography.titleMedium)
                Text("Es scheint, als ob dein Profil noch nicht vollständig eingerichtet wurde.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { userProfileViewModel.fetchUserProfileThenMedia() }) { // ViewModel-Funktion umbenannt
                    Text("Erneut versuchen")
                }
            }

        }

    }
}

@Composable
fun MediaListItem(medium: Medium, onClick: () -> Unit) {

    Column (
        modifier = Modifier
            .width(100.dp)
            .wrapContentHeight()
            .clickable {
                onClick()
            }
            .background(Color(0x505B231D), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        val posterUrl = when (medium) {
            is Movie -> medium.posterUrl
            is Series -> medium.posterUrl
            else -> null
        }

        posterUrl?.let { path ->
            val imageUrl = "https://image.tmdb.org/t/p/w500$path"
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = medium.title,
                modifier = Modifier
                    .width(20.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = medium.title, style = MaterialTheme.typography.titleMedium)
    }
}
