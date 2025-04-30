package com.example.brainlog.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainlog.viewmodel.SearchUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brainlog.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val searchState by viewModel.uiState.collectAsState()

    SearchLogic(viewModel = viewModel) { isSearchExpanded, searchText, onSearchExpandedChange, onSearchTextChange ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                ) {
                    if (!isSearchExpanded) {
                        Text(
                            text = "BrainLog",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp),
                            color = White,
                            fontSize = 23.sp
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 16.dp)
                                .size(40.dp),
                            shape = CircleShape,
                            color = Color(0x80D04242),
                            tonalElevation = 4.dp,
                        ) {
                            // IconButton *innerhalb* der gestalteten Surface
                            IconButton(
                                onClick = { onSearchExpandedChange(true) },
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = White
                                )
                            }
                        }
                    }
                        SearchBar(
                            isSearchExpanded = isSearchExpanded,
                            searchText = searchText,
                            onSearchExpandedChange = onSearchExpandedChange,
                            onSearchTextChange = onSearchTextChange,
                        )

                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (searchState) {
                    is SearchUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is SearchUiState.Success -> {
                        val results = (searchState as SearchUiState.Success).results
                        SearchResultsList(results = results) { clickedMedium ->
                            val mediaType = clickedMedium.type
                            val mediaId = clickedMedium.id
                            navController.navigate("mediaDetail/${mediaId}/${mediaType.name}")
                        }
                    }
                    is SearchUiState.Error -> {
                        Text(
                            text = "Fehler: ${(searchState as SearchUiState.Error).message}",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    SearchUiState.Idle -> Unit
                }
            }
        }
    }
}