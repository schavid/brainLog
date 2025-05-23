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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import kotlinx.coroutines.flow.Flow
import androidx.compose.material.icons.filled.DoneAll


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfileViewModel: ProfileScreenViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMediaDetail: (id: Int, type: MediumType) -> Unit
) {
    val uiState by userProfileViewModel.uiState.collectAsStateWithLifecycle()
    val userMediaListState by userProfileViewModel.userMediaListState.collectAsStateWithLifecycle()

    val selectedMediaGlobalIds by userProfileViewModel.selectedMediaGlobalIds.collectAsStateWithLifecycle()
    val isInSelectionMode by userProfileViewModel.isInSelectionMode.collectAsStateWithLifecycle()

    BackHandler(enabled = isInSelectionMode) {
        userProfileViewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            if (isInSelectionMode) {
                SelectionModeTopAppBar(
                    selectedCount = selectedMediaGlobalIds.size,
                    onCloseSelectionMode = { userProfileViewModel.clearSelection() },
                    onDeleteSelected = { userProfileViewModel.deleteSelectedMedia() },
                    onMarkAsFinishedSelected = { userProfileViewModel.markSelectedAsFinished() }
                )
            } else {

            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            if (!isInSelectionMode && uiState is UserProfileUiState.Success) {
                val user = (uiState as UserProfileUiState.Success).userDocument
                ProfileHeader(
                    username = user.username,
                    photoUrl = user.photoUrl,
                    onLogoutClick = {
                        onNavigateToLogin()
                    }
                )
            }


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
}

@Composable
fun ProfileHeader(
    username: String,
    photoUrl: String?,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
fun MediaListItem(medium: Medium,
                  onClick: () -> Unit,
                  isSelected: Boolean,
                  isInSelectionMode: Boolean,
                  onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp, 150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x505B231D))
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
            IconButton(onClick = onMarkAsFinishedSelected) {
                Icon(Icons.Filled.DoneAll, contentDescription = "Als fertig markieren") // Passendes Icon
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
