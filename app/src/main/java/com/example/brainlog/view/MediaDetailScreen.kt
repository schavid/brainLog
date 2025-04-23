package com.example.brainlog.view


import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainlog.viewmodel.MovieDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.brainlog.R
import com.example.brainlog.viewmodel.MovieDetailUiState
import com.example.brainlog.viewmodel.SearchMedium



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    movieId: Int,
    viewModel: MovieDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

    Scaffold(
        modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent),

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
                ),
                modifier = Modifier.background(Color.Transparent)


            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            viewModel.loadMovieDetail(movieId)
        }

        when (uiState) {
            is MovieDetailUiState.Loading,
            is MovieDetailUiState.Idle -> {
                CircularProgressIndicator()
            }

            is MovieDetailUiState.Success -> {
                val movie = (uiState as MovieDetailUiState.Success).movie

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0x505B231D), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = movie.title,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        color = White
                    )

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
                                        text = movie.releaseDate,
                                        fontSize = 12.sp,
                                        color = White
                                    )
                                }

                                // Genre
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Genre",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = White
                                    )
                                    Text(
                                        text = movie.genres.joinToString(),
                                        fontSize = 12.sp,
                                        color = White
                                    )
                                }

                                // Runtime
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Runtime",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = White
                                    )
                                    Text(
                                        text = "${movie.runtime} min",
                                        fontSize = 12.sp,
                                        color = White
                                    )
                                }
                            }

                        }
                        movie.posterUrl?.let { path ->
                            val imageUrl = "https://image.tmdb.org/t/p/w500$path"
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
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
                        text = movie.description,
                        textAlign = TextAlign.Justify,
                        fontSize = 12.sp,
                        color = White
                    )
                }


            }

            is MovieDetailUiState.Error -> {
                val errorMsg = (uiState as MovieDetailUiState.Error).message
                Text("Error: $errorMsg")
            }
        }
    }
    }
}
