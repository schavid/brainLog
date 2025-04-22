package com.example.brainlog.view


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brainlog.viewmodel.MovieDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainlog.viewmodel.MovieDetailUiState


@Composable
fun MediaDetailScreen(
    movieId: Int,
    viewModel: MovieDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMovieDetail(movieId)
    }

    when (uiState) {
        is MovieDetailUiState.Loading,
        is MovieDetailUiState.Idle -> {
            Text("Loading...")
        }

        is MovieDetailUiState.Success -> {
            val movie = (uiState as MovieDetailUiState.Success).movie

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Title: ${movie.title}")
                Text("Release Date: ${movie.releaseDate}")
                Text("Runtime: ${movie.runtime} minutes")
                /*Text("Genres: ${movie.genres.joinToString { it.name }}")*/
                Text("Description: ${movie.description}")
            }
        }

        is MovieDetailUiState.Error -> {
            val errorMsg = (uiState as MovieDetailUiState.Error).message
            Text("Error: $errorMsg")
        }
    }
}
