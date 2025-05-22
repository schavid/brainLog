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
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.flow.Flow


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





@Composable
fun MediaListItem(medium: Medium, onClick: () -> Unit) {

    Column (
        modifier = Modifier
            .size(100.dp, 150.dp)
            .clickable {
                onClick()
            }
            .background(Color(0x505B231D), RoundedCornerShape(20.dp))
            .padding(8.dp, 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                    .weight(0.8f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Inside,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = medium.title, style = AppTextStyles.normal, modifier = Modifier.weight(0.2f))
    }
}
