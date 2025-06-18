package com.example.brainlog.view


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.brainlog.model.MediumType
import com.example.brainlog.model.Movie
import com.example.brainlog.model.Series
import com.example.brainlog.ui.theme.AppTextStyles
import com.example.brainlog.viewmodel.ProfileScreenViewModel
import com.example.brainlog.viewmodel.UserMediaListUiState
import com.example.brainlog.viewmodel.UserProfileUiState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.style.TextOverflow
import com.example.brainlog.model.Game
import com.example.brainlog.viewmodel.ProfileScreenViewModelFactory


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    // userProfileViewModel: ProfileScreenViewModel = viewModel(), // ALTE Zeile
    userProfileViewModel: ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModelFactory()), // NEUE Zeile mit Factory
    onNavigateToLogin: () -> Unit,
    onNavigateToMediaDetail: (id: Int, type: MediumType) -> Unit,
    onSetTopAppBar: ((@Composable () -> Unit)?) -> Unit
) {

    val uiState by userProfileViewModel.uiState.collectAsStateWithLifecycle()
    val userMediaListState by userProfileViewModel.userMediaListState.collectAsStateWithLifecycle()

    val selectedMediaGlobalIds by userProfileViewModel.selectedMediaGlobalIds.collectAsStateWithLifecycle()
    val isInSelectionMode by userProfileViewModel.isInSelectionMode.collectAsStateWithLifecycle()

    // NEU: DisposableEffect, der die TopAppBar im Parent steuert
    DisposableEffect(isInSelectionMode, selectedMediaGlobalIds.size) {
        if (isInSelectionMode) {
            // Setze die TopAppBar für den Auswahlmodus
            onSetTopAppBar {
                SelectionModeTopAppBar(
                    selectedCount = selectedMediaGlobalIds.size,
                    onCloseSelectionMode = { userProfileViewModel.clearSelection() },
                    onDeleteSelected = { userProfileViewModel.deleteSelectedMedia() },
                    onMarkAsFinishedSelected = { userProfileViewModel.markSelectedAsFinished() }
                )
            }
        } else {
            // Setze die Standard-TopAppBar für das Profil
            onSetTopAppBar {
                if (uiState is UserProfileUiState.Success) {
                    // Wir nutzen eine transparente TopAppBar als intelligenten Container
                    TopAppBar(
                        // Wir platzieren den ProfileHeader im title-Slot,
                        // damit er den verfügbaren Platz einnimmt.
                        title = {
                            ProfileHeader(
                                username = (uiState as UserProfileUiState.Success).userDocument.username,
                                photoUrl = (uiState as UserProfileUiState.Success).userDocument.photoUrl,
                                onLogoutClick = {
                                    onNavigateToLogin()
                                }
                            )
                        },
                        // Wichtig: Mache den Container der TopAppBar transparent
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Wird aufgerufen, wenn der Screen verlassen wird -> räumt die TopAppBar auf
        onDispose {
            onSetTopAppBar(null)
        }
    }

    BackHandler(enabled = isInSelectionMode) {
        userProfileViewModel.clearSelection()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {





        Spacer(modifier = Modifier.height(if (isInSelectionMode) 8.dp else 20.dp))

        when (uiState) { // Variable für Smart Cast

            is UserProfileUiState.Loading -> {
                CircularProgressIndicator()
                Text("Loading Profile...")
            }

            is UserProfileUiState.Success -> {
                val user = (uiState as UserProfileUiState.Success).userDocument

                Text(
                    text = "${user.username}'s Watchlist",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = AppTextStyles.CustomHeader
                )

                when (val mediaState = userMediaListState) {
                    is UserMediaListUiState.Loading -> {
                        CircularProgressIndicator()
                        Text("Lade Medien...") }

                    is UserMediaListUiState.Success -> {
                        if (mediaState.mediaItems.isEmpty()) {
                            Text("Du hast noch keine Medien zu deiner Watchlist hinzugefügt.")
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                mediaState.mediaItems.forEach{ medium ->
                                    MediaListItem(
                                        medium = medium,
                                        isSelected = selectedMediaGlobalIds.contains(medium.globalID),
                                        isInSelectionMode = isInSelectionMode,
                                        onClick = {
                                            if (isInSelectionMode) {
                                                userProfileViewModel.toggleMediaSelection(medium)
                                            } else {
                                                onNavigateToMediaDetail(medium.id, medium.type)
                                            }
                                        },
                                        onLongClick = {
                                            userProfileViewModel.toggleMediaSelection(medium)
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
fun ProfileHeader(
    username: String,
    photoUrl: String?,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profilbild von $username",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_placeholder_profile),
                    error = painterResource(R.drawable.ic_placeholder_profile)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_placeholder_profile),
                    contentDescription = "Profilbild von $username",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                modifier = Modifier
                    .padding(4.dp)
                    .height(34.dp),
                onClick = onLogoutClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListItem(
    medium: Medium,
    onClick: () -> Unit,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp, 150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x505B231D)) // Du kannst hier auch MaterialTheme.colorScheme.surfaceVariant verwenden
            .then(
                if (isSelected && isInSelectionMode) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Ermittle den Poster-Pfad oder die vollständige URL basierend auf dem Typ
            val rawPosterPathOrUrl: String? = when (medium) {
                is Movie -> medium.posterUrl
                is Series -> medium.posterUrl
                is Game -> medium.posterUrl // Dies ist die volle URL vom Game-Domänenmodell
                else -> null // Sollte nicht eintreten, wenn alle Medium-Typen abgedeckt sind
            }

            Box( // Box für das Bild und Overlays
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (rawPosterPathOrUrl != null) {
                    // Erstelle die imageUrl korrekt:
                    // RAWG liefert volle URLs, TMDB relative Pfade.
                    val finalImageUrl = if (medium.apiProvider == "RAWG" || rawPosterPathOrUrl.startsWith("http")) {
                        rawPosterPathOrUrl // Ist bereits eine volle URL (z.B. von RAWG)
                    } else {
                        "https://image.tmdb.org/t/p/w500$rawPosterPathOrUrl" // TMDB-Pfad
                    }

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = finalImageUrl,
                            error = painterResource(id = R.drawable.ic_placeholder_image),
                            placeholder = painterResource(id = R.drawable.ic_placeholder_image)
                        ),
                        contentDescription = medium.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else { // Fallback, wenn kein Poster-URL vorhanden ist
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_placeholder_image),
                            contentDescription = "Kein Bild verfügbar",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Auswahlindikator (dein bestehender Code ist hier gut)
                if (isInSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Ausgewählt" else "Nicht ausgewählt",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .padding(2.dp)
                    )
                }
                // "Fertig" Indikator (dein bestehender Code ist hier gut)
                else if (medium.isFinished) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Fertig angesehen/gespielt",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .padding(2.dp)
                    )
                }
            }

            // Titel (dein bestehender Code ist hier gut)
            Text(
                text = medium.title,
                style = AppTextStyles.normal.copy(fontSize = 13.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// TopAppBar für den Auswahlmodus
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeTopAppBar(
    selectedCount: Int,
    onCloseSelectionMode: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMarkAsFinishedSelected: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount ausgewählt") },
        navigationIcon = {
            IconButton(onClick = onCloseSelectionMode) {
                Icon(Icons.Filled.Close, contentDescription = "Auswahlmodus schließen")
            }
        },
        actions = {
            Column {
                Text("mark as watched", fontSize = 5.sp)
                IconButton(onClick = onMarkAsFinishedSelected) {
                    Icon(Icons.Filled.DoneAll, contentDescription = "Als fertig markieren")
                }

            }


            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Filled.Delete, contentDescription = "Ausgewählte löschen")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer, // Passende Farbe
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
