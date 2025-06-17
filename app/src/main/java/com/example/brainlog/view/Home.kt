package com.example.brainlog.view

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.brainlog.viewmodel.SearchUiState
import com.example.brainlog.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Home(navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val searchState by viewModel.uiState.collectAsState()

    SearchLogic(viewModel = viewModel) { isSearchExpanded, searchText, currentSelectedSearchType, onSearchExpandedChange, onSearchTextChange, onSearchTypeChange ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Oberer Bereich für Titel/Icon oder Suchleiste
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Passt Höhe an den sichtbaren Inhalt an
                    .padding(vertical = 8.dp, horizontal = 12.dp) // Etwas Padding für den gesamten Bereich
            ) {
                // Zustand 1: Titel und Such-Icon (eingeklappt)
                // In Home.kt, Zeile 45
                androidx.compose.animation.AnimatedVisibility( // Vollqualifizierter Name
                    visible = !isSearchExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 200))
                ) {
                    // Dein Row-Code für Titel und Such-Icon hier
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(56.dp), // Beispielhöhe
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BrainLog",
                            fontSize = 23.sp,
                            color = Color.White // Annahme basierend auf deinem vorherigen Styling
                            // Füge hier deine anderen Text-Styles ein
                        )
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0x80D04242), // Deine Farbe
                            tonalElevation = 4.dp,
                        ) {
                            IconButton(onClick = { onSearchExpandedChange(true) }) {
                                Icon(Icons.Filled.Search, contentDescription = "Suche öffnen", tint = Color.White)
                            }
                        }
                    }
                }

                // Zustand 2: SearchBar (ausgeklappt)
                // Die SearchBar selbst hat eine interne AnimatedVisibility für ihren Inhalt (Textfeld + Chips)
                // Wir müssen hier nur sicherstellen, dass die SearchBar-Komponente im Kompositionsbaum ist,
                // wenn isSearchExpanded true ist. Ihre eigene Animation kümmert sich um das Einblenden.
                // Wenn !isSearchExpanded, ist die SearchBar zwar im Baum, aber ihr Inhalt unsichtbar.
                // Um einen "Sprung" zu vermeiden, wenn die Höhe sich ändert, könnte man der SearchBar eine
                // minimale Höhe geben oder die äußere Box mit animateContentSize() versehen.
                // Für den Anfang ist es so aber oft schon besser:
                if (isSearchExpanded) { // Rendere SearchBar nur wenn sie expandiert ist, um Layout-Überraschungen zu minimieren
                    SearchBar(
                        isSearchExpanded = isSearchExpanded, // true hier
                        searchText = searchText,
                        onSearchExpandedChange = onSearchExpandedChange,
                        onSearchTextChange = onSearchTextChange,
                        selectedSearchType = currentSelectedSearchType,
                        onSearchTypeSelected = onSearchTypeChange
                    )
                }
            } // Ende des oberen Box-Containers

            // Suchergebnisse (dein bestehender Code)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (val currentState = searchState) {
                    is SearchUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                            Text("Lade Ergebnisse...", modifier = Modifier.padding(top = 60.dp))
                        }
                    }
                    is SearchUiState.Success -> {
                        if (currentState.results.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Keine Ergebnisse für deine Suche gefunden.")
                            }
                        } else {
                            SearchResultsList(results = currentState.results) { clickedMedium ->
                                navController.navigate("mediaDetail/${clickedMedium.id}/${clickedMedium.displayMediaType.name}")
                            }
                        }
                    }
                    is SearchUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Fehler: ${currentState.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                    SearchUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Heiße Ladyboys", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
