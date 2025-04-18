package com.example.brainlog.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brainlog.R
import com.example.brainlog.viewmodel.SearchUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainlog.viewmodel.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val viewModel: SearchViewModel = viewModel()
    val searchState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )


        SearchLogic(viewModel = viewModel) { isSearchExpanded, searchText, onSearchExpandedChange, onSearchTextChange ->
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SearchBar(
                                    isSearchExpanded = isSearchExpanded,
                                    searchText = searchText,
                                    onSearchExpandedChange = onSearchExpandedChange,
                                    onSearchTextChange = onSearchTextChange
                                )
                                Text(
                                    text = "BrainLog",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                    color = White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        actions = {
                            Surface(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp),
                                shape = CircleShape,
                                color = Color(0x80D04242),
                                tonalElevation = 4.dp,
                            ) {
                                IconButton(
                                    onClick = { onSearchExpandedChange(!isSearchExpanded) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = "Search",
                                        tint = White
                                    )
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    when (searchState) {
                        is SearchUiState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is SearchUiState.Success -> {
                            val results = (searchState as SearchUiState.Success).results
                            SearchResultsList(results = results)
                        }

                        is SearchUiState.Error -> {
                            Text("Fehler: ${(searchState as SearchUiState.Error).message}")
                        }

                        SearchUiState.Idle -> Unit
                    }
                    /*// Hier die Suchergebnisse anzeigen
                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchResultsList(results = searchResults)
                    }*/
                }
            }
        }
    }
}
