package com.example.brainlog.view


import android.util.Log // Importiere Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Für Zurück-Pfeil
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Für Zurück-Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost // Für Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Für Toast/Snackbar Kontext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainlog.viewmodel.MovieDetailViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.brainlog.model.MediumType
import com.example.brainlog.viewmodel.MediaDetailUiState
import com.example.brainlog.model.Movie
import com.example.brainlog.model.Series
import com.example.brainlog.model.Game // Importiere Game
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // Importiere NavController
import com.example.brainlog.viewmodel.MovieDetailViewModelFactory
import com.example.brainlog.viewmodel.UpdateUserMediaListUiState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    movieId: Int, // Beachte: dieser Parameter heißt movieId, im LaunchedEffect verwendest du mediaId. Vereinheitliche das.
    mediaType: MediumType,
    navController: NavController // Hinzugefügt, um Zurück-Navigation zu ermöglichen
) {

    val firebaseAuth = remember { Firebase.auth }
    val firebaseFirestore = remember { Firebase.firestore }
    val movieDetailViewModelFactory = remember(firebaseAuth, firebaseFirestore) { // Keys für remember
        MovieDetailViewModelFactory(auth = firebaseAuth, db = firebaseFirestore)
    }
    val viewModel: MovieDetailViewModel = viewModel(factory = movieDetailViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val updatedUserMediaList by viewModel.updateUserMediaListUiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Für Toasts oder andere Kontext-abhängige Aktionen

    // Lade Details, wenn sich movieId oder mediaType ändern
    LaunchedEffect(movieId, mediaType) {
        viewModel.loadMovieDetail(movieId, mediaType)
    }

    LaunchedEffect(updatedUserMediaList) {
        when (val state = updatedUserMediaList) {
            is UpdateUserMediaListUiState.AddSuccess -> {
                scope.launch { snackbarHostState.showSnackbar("Medium erfolgreich hinzugefügt!") }
                viewModel.resetUpdateUserMediaListState()
            }
            is UpdateUserMediaListUiState.DeleteSuccess -> { // Für später, falls du Löschen implementierst
                scope.launch { snackbarHostState.showSnackbar("Medium erfolgreich entfernt!") }
                viewModel.resetUpdateUserMediaListState()
            }
            is UpdateUserMediaListUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar("Fehler: ${state.message}") }
                viewModel.resetUpdateUserMediaListState()
            }
            is UpdateUserMediaListUiState.UserNotLoggedIn -> {
                scope.launch { snackbarHostState.showSnackbar("Bitte zuerst anmelden.") }
                viewModel.resetUpdateUserMediaListState()
            }
            else -> Unit // Idle, Loading
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val titleText = (uiState as? MediaDetailUiState.Success)?.medium?.title ?: "Details"
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) { // Geändert zu CenterStart für Platz für Zurück-Button
                        Text(text = titleText, textAlign = TextAlign.Center, color = White, maxLines = 1)
                    }
                },
                navigationIcon = { // Zurück-Button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (uiState is MediaDetailUiState.Success) {
                val medium = (uiState as MediaDetailUiState.Success).medium
                FloatingActionButton(
                    onClick = { viewModel.addMediumToUser(medium) },
                    shape = CircleShape,
                    containerColor = Color(0xFF490D0D), // Deine Farbe
                    contentColor = White, // Farbe für das Icon
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Medium hinzufügen")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // SnackbarHost hinzugefügt
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) { // uiState einmal entpacken
                is MediaDetailUiState.Loading, MediaDetailUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MediaDetailUiState.Success -> {
                    val medium = state.medium // medium ist hier vom Typ Medium
                    Column(
                        modifier = Modifier
                            .fillMaxSize() // Geändert zu fillMaxSize, um Scrollen der gesamten Spalte zu ermöglichen
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp) // Padding für die gesamte Spalte
                    ) {
                        Spacer(modifier = Modifier.height(10.dp)) // Abstand von TopAppBar

                        // --- Poster/Bild ---
                        val rawPosterPathOrUrl: String? = when (medium) {
                            is Movie -> medium.posterUrl
                            is Series -> medium.posterUrl
                            is Game -> medium.posterUrl // Game hat jetzt posterUrl
                            else -> null
                        }

                        if (rawPosterPathOrUrl != null) {
                            val finalImageUrl = if (medium.apiProvider == "RAWG" || rawPosterPathOrUrl.startsWith("http", ignoreCase = true)) {
                                rawPosterPathOrUrl
                            } else {
                                "https://image.tmdb.org/t/p/w780$rawPosterPathOrUrl" // w780 für größere Detailbilder
                            }
                            Image(
                                painter = rememberAsyncImagePainter(model = finalImageUrl),
                                contentDescription = medium.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp) // Höhe angepasst
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant), // Hintergrund für den Ladefall
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) { Text("Kein Bild verfügbar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Box mit Hintergrund für Textdetails
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x505B231D), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = medium.title,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Serif,
                                color = White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Release, Genre, Spezifische Infos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround // Bessere Verteilung
                            ) {
                                DetailInfoColumn("Release", medium.releaseDate)
                                DetailInfoColumn("Genre(s)", medium.genres.take(2).joinToString(", ")) // Zeige max 2 Genres

                                // Typ-spezifische Info (Runtime, Seasons, Metacritic)
                                when (medium) {
                                    is Movie -> DetailInfoColumn("Laufzeit", "${medium.runtime ?: "N/A"} Min.")
                                    is Series -> DetailInfoColumn("Staffeln", "${medium.numberOfSeasons ?: "N/A"}")
                                    is Game -> DetailInfoColumn("Metacritic", "${medium.metacriticScore ?: "N/A"}")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Weitere Spiel-spezifische Details (falls es ein Spiel ist)
                            if (medium is Game) {
                                if (medium.developers.isNotEmpty()) DetailInfoGrid("Entwickler:", medium.developers.joinToString(", "))
                                if (medium.publishers.isNotEmpty()) DetailInfoGrid("Publisher:", medium.publishers.joinToString(", "))
                                medium.esrbRatingName?.let { DetailInfoGrid("ESRB:", it) }
                                medium.website?.let { if (it.isNotBlank()) DetailInfoGrid("Webseite:", it) } // TODO: Klickbar machen
                                medium.playtime?.let { if (it > 0) DetailInfoGrid("Spielzeit (Std.):", it.toString()) }
                            }
                            // TODO: Füge hier ähnliche Blöcke für Movie- und Series-spezifische Details ein, falls gewünscht

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Beschreibung:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
                            Text(text = medium.description, textAlign = TextAlign.Justify, fontSize = 12.sp, color = White)
                        }
                        Spacer(modifier = Modifier.height(80.dp)) // Platz für den FAB
                    }
                }
                is MediaDetailUiState.Error -> {
                    // Dein bestehender Error-Handler
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color.Red,
                            modifier = Modifier
                                .background(White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Hilfs-Composable für Detail-Spalten
@Composable
fun DetailInfoColumn(label: String, value: String) {
    if (value.isNotBlank() && value.lowercase() != "n/a" && value.lowercase() != "unbekannt") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White, textAlign = TextAlign.Center)
            Text(text = value, fontSize = 12.sp, color = White, textAlign = TextAlign.Center, maxLines = 3)
        }
    }
}

// Hilfs-Composable für Grid-ähnliche Detail-Zeilen
@Composable
fun DetailInfoGrid(label: String, value: String) {
    if (value.isNotBlank() && value.lowercase() != "n/a" && value.lowercase() != "unbekannt") {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = White)
        }
    }
}