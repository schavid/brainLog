package com.example.brainlog.view


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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.brainlog.viewmodel.AddMediumToUserUiState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainlog.viewmodel.MovieDetailViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    movieId: Int,
    mediaType: MediumType,
) {

    val firebaseAuth = remember { Firebase.auth }
    val firebaseFirestore = remember { Firebase.firestore }
    val movieDetailViewModelFactory = remember {
        MovieDetailViewModelFactory(auth = firebaseAuth, db = firebaseFirestore)
    }
    val viewModel: MovieDetailViewModel = viewModel(factory = movieDetailViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val addMediumState by viewModel.addMediumToUserUiState.collectAsState() // State für Hinzufüge-Aktion

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(addMediumState) {
        when (val state = addMediumState) {
            is AddMediumToUserUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Medium erfolgreich hinzugefügt!")
                }
                viewModel.resetAddMediumState() // State zurücksetzen, um wiederholte Nachrichten zu vermeiden
            }
            is AddMediumToUserUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Fehler: ${state.message}")
                }
                viewModel.resetAddMediumState()
            }
            is AddMediumToUserUiState.UserNotLoggedIn -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Bitte zuerst anmelden.")
                }
                viewModel.resetAddMediumState()
            }
            else -> Unit // Idle oder Loading
        }
    }



    Scaffold(
        modifier = Modifier
            .fillMaxSize(),

        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "BrainLog",
                            textAlign = TextAlign.Center,
                            color = White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            // Zeige den FAB nur an, wenn die Daten erfolgreich geladen wurden
            if (uiState is MediaDetailUiState.Success) {
                val medium = (uiState as MediaDetailUiState.Success).medium
                FloatingActionButton(
                    onClick = {
                        // Rufe die ViewModel-Funktion auf, um das Medium hinzuzufügen
                        viewModel.addMediumToUser(medium)
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFF490D0D),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp, // Schatten im Normalzustand
                        pressedElevation = 12.dp, // Schatten, wenn gedrückt
                        focusedElevation = 10.dp, // Schatten, wenn fokussiert
                        hoveredElevation = 10.dp
                    )

                ) {
                    // Ändere das Icon vielleicht zu einem "Add"-Icon
                    Icon(
                        imageVector = Icons.Filled.Add, // z.B. "Add" statt "Edit"
                        contentDescription = "Medium hinzufügen"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color.Transparent
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            viewModel.loadMovieDetail(movieId, mediaType)
        }

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            when (uiState) {
                is MediaDetailUiState.Loading,
                is MediaDetailUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }


                is MediaDetailUiState.Success -> {
                    val medium = (uiState as MediaDetailUiState.Success).medium

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0x505B231D), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

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
                                    .width(80.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween // gleichmäßige Verteilung
                                ) {
                                    // Release Date
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Release Date",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = White
                                        )
                                        Text(
                                            text = medium.releaseDate,
                                            fontSize = 12.sp,
                                            color = White
                                        )
                                    }

                                    // Genre
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Text(
                                            text = "Genre",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = White
                                        )
                                        Text(
                                            text = medium.genres.joinToString(),
                                            fontSize = 12.sp,
                                            color = White,

                                            )
                                    }

                                    // Runtime
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                        when (medium) {

                                            is Movie -> {
                                                Text(
                                                    text = "Runtime",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = White
                                                )
                                                Text(
                                                    text = "${medium.runtime} min",
                                                    fontSize = 12.sp,
                                                    color = White
                                                )
                                            }

                                            is Series -> {
                                                Text(
                                                    text = "Seasons",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = White
                                                )
                                                Text(
                                                    text = "${medium.numberOfSeasons}",
                                                    fontSize = 12.sp,
                                                    color = White
                                                )
                                            }
                                        }
                                    }
                                }

                            }

                        }
                        Spacer(modifier = Modifier.height(16.dp))


                        Text(
                            "Description:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = White
                        )

                        Text(
                            text = medium.description,
                            textAlign = TextAlign.Justify,
                            fontSize = 12.sp,
                            color = White
                        )

                    }
                }

                is MediaDetailUiState.Error -> {
                    val errorMsg = (uiState as MediaDetailUiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { // Zentrieren in neuer Box
                        Text(
                            text = "Error: $errorMsg",
                            color = Color.Red,
                            modifier = Modifier.background(
                                White.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            ).padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
