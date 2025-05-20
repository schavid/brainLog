package com.example.brainlog.view


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.brainlog.R
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header mit Benutzername und Logout-Button
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (uiState is UserProfileUiState.Success) {
                val user = (uiState as UserProfileUiState.Success).userDocument

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    val photoUrl = user?.photoUrl

                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "User profile picture",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape), // Macht das Bild rund
                            contentScale = ContentScale.Crop, // Skaliert das Bild, um den Kreis zu füllen
                            placeholder = painterResource(id = R.drawable.ic_placeholder_profile), // Optional: Platzhalterbild
                            error = painterResource(id = R.drawable.ic_placeholder_profile) // Optional: Bild bei Ladefehler
                        )
                    } else {
                        Image( // Oder Icon, je nachdem was dein Platzhalter ist
                            painter = painterResource(id = R.drawable.ic_placeholder_profile),
                            contentDescription = "Default profile picture placeholder",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(34.dp),
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout Icon",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))

                        Text("Logout", fontSize = 10.sp)
                    }


                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "${user.username}'s WatchList",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = CustomTypography.titleLarge
                )
            }


        }

        Spacer(modifier = Modifier.height(40.dp))

        // Dynamischer Teil, abhängig vom uiState
        when (uiState) {
            is UserProfileUiState.Loading -> {
                CircularProgressIndicator()
                Text("Loading Profile...")
            }

            is UserProfileUiState.Success -> {

                Spacer(modifier = Modifier.height(8.dp))

                when (val mediaState = userMediaListState) {
                    is UserMediaListUiState.Loading -> {
                        CircularProgressIndicator()
                        Text("Lade Medien...") }

                    is UserMediaListUiState.Success -> {
                        if (mediaState.mediaItems.isEmpty()) {
                            Text("Du hast noch keine Medien zu deiner Watchlist hinzugefügt.")
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                mediaState.mediaItems.forEach { medium ->
                                    MediaListItem(
                                        medium = medium,
                                        onClick = {
                                            onNavigateToMediaDetail(medium.id, medium.type)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is UserMediaListUiState.Error -> {
                        Text("Fehler beim Laden der Medien: ${mediaState.message}")
                    }

                    is UserMediaListUiState.NoMediaFound -> {
                        Text("Keine Medien in deiner Watchlist gefunden.")
                    }

                    is UserMediaListUiState.Idle -> Unit
                }
            }

            is UserProfileUiState.Error -> {
                Text("Fehler: ${(uiState as UserProfileUiState.Error).message}")
                Button(onClick = { userProfileViewModel.fetchUserProfileThenMedia() }) {
                    Text("Erneut versuchen")
                }
            }

            is UserProfileUiState.NotLoggedIn -> {
                Text("Du bist nicht angemeldet.")
                Button(onClick = onNavigateToLogin) {
                    Text("Zum Login")
                }
            }

            is UserProfileUiState.ProfileNotFound -> {
                Text("Profil nicht gefunden.")
                Button(onClick = { userProfileViewModel.fetchUserProfileThenMedia() }) {
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
