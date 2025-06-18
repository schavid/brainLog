package com.example.brainlog.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.brainlog.R
import com.example.brainlog.model.Game
import com.example.brainlog.model.Medium
import com.example.brainlog.model.MediumType
import com.example.brainlog.model.Movie
import com.example.brainlog.model.Series
import com.example.brainlog.ui.theme.AppTextStyles
import com.example.brainlog.viewmodel.ProfileScreenViewModel
import com.example.brainlog.viewmodel.ProfileScreenViewModelFactory
import com.example.brainlog.viewmodel.UserMediaListUiState
import com.example.brainlog.viewmodel.UserProfileUiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfileViewModel: ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModelFactory()),
    onNavigateToLogin: () -> Unit,
    onNavigateToMediaDetail: (id: Int, type: MediumType) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onSetTopAppBar: ((@Composable () -> Unit)?) -> Unit
) {

    val uiState by userProfileViewModel.uiState.collectAsStateWithLifecycle()
    val userMediaListState by userProfileViewModel.userMediaListState.collectAsStateWithLifecycle()
    val selectedMediaGlobalIds by userProfileViewModel.selectedMediaGlobalIds.collectAsStateWithLifecycle()
    val isInSelectionMode by userProfileViewModel.isInSelectionMode.collectAsStateWithLifecycle()

    DisposableEffect(uiState, isInSelectionMode, selectedMediaGlobalIds.size) {
        if (isInSelectionMode) {
            onSetTopAppBar {
                SelectionModeTopAppBar(
                    selectedCount = selectedMediaGlobalIds.size,
                    onCloseSelectionMode = { userProfileViewModel.clearSelection() },
                    onDeleteSelected = { userProfileViewModel.deleteSelectedMedia() },
                    onMarkAsFinishedSelected = { userProfileViewModel.toggleFinishedStateForSelected() }
                )
            }
        } else if (uiState is UserProfileUiState.Success) {
            val user = (uiState as UserProfileUiState.Success).userDocument
            val photoUrl = user.photoUrl
            val username = user.username

            onSetTopAppBar {
                MediumTopAppBar(
                    title = {
                        // Der große, aufgeklappte Header (wird ausgeblendet).
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start

                        ) {
                            // Die Logik vom alten ProfileHeader ist jetzt hier drin.
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profilbild von $username",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_placeholder_profile),
                                error = painterResource(R.drawable.ic_placeholder_profile)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = username,
                                style = AppTextStyles.HugeHeader
                            )


                        }
                    },
                    actions = {
                        ElevatedButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.size(40.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color(0xFF5B231D)
                    )
                )
            }
        }
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val currentState = uiState) {
            is UserProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UserProfileUiState.Success -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Watchlist",
                    modifier = Modifier.fillMaxWidth(),
                    style = AppTextStyles.CustomHeader,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (val mediaState = userMediaListState) {
                    is UserMediaListUiState.Loading -> CircularProgressIndicator()
                    is UserMediaListUiState.Success -> {
                        if (mediaState.mediaItems.isEmpty()) {
                            Text("Du hast noch keine Medien zu deiner Watchlist hinzugefügt.")
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                mediaState.mediaItems.forEach { medium ->
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
                    is UserMediaListUiState.Error -> Text("Fehler beim Laden der Medien: ${mediaState.message}")
                    is UserMediaListUiState.NoMediaFound -> Text("Keine Medien in deiner Watchlist gefunden.")
                    is UserMediaListUiState.Idle -> Unit
                }
            }
            is UserProfileUiState.Error -> Text("Fehler: ${currentState.message}")
            is UserProfileUiState.NotLoggedIn -> Text("Du bist nicht angemeldet.")
            is UserProfileUiState.ProfileNotFound -> Text("Profil nicht gefunden.")
        }
    }
}


// Die `ProfileHeader`-Funktion wurde gelöscht.


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

            Text(
                text = medium.title,
                style = AppTextStyles.normal.copy(fontSize = 13.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// TopAppBar für den Auswahlmodus
@OptIn(ExperimentalMaterial3Api::class)
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
            TextButton(
                onClick = onMarkAsFinishedSelected,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Als gesehen markieren"
                    )
                    Text("Gesehen")
                }
            }

            TextButton(
                onClick = onDeleteSelected,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Ausgewählte löschen"
                    )
                    Text("Löschen")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
