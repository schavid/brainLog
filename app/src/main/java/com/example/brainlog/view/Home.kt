package com.example.brainlog.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brainlog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hintergrundbild
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BrainLog",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                color = Color.White,
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
                                .size(48.dp)
                                .shadow(8.dp, CircleShape, clip = false),
                            shape = CircleShape,
                            color = Color(0x80D04242), // 50% Transparenz, dein Farbcode
                            tonalElevation = 4.dp,
                        ) {
                            IconButton(

                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                },
                                modifier = Modifier
                                    .size(48.dp)
                            )
                            {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars) // Wichtig: Hier Padding anwenden
                )
            },
            containerColor = Color.Transparent // Wichtig: Scaffold transparent machen
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeOut(animationSpec = tween(durationMillis = 500))
                )
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search") }
                        )
                        IconButton(onClick = {
                            isSearchExpanded = false
                            searchText = ""
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }
                Text(text = searchText)
            }
        }
    }
}