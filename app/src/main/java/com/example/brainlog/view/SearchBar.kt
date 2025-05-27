package com.example.brainlog.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // Importiere alles aus layout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.* // Importiere alles aus material3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brainlog.model.MediumType

// Hilfsfunktion, falls du sie nicht schon woanders hast
fun String.capitalizeV2(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // OptIn für FilterChip und FlowRow
@Composable
fun SearchBar(
    isSearchExpanded: Boolean,
    searchText: String,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit,
    // NEUE Parameter:
    selectedSearchType: MediumType,
    onSearchTypeSelected: (MediumType) -> Unit
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp) // Etwas Platz nach unten
        ) {
            // Bestehende Suchleiste (Textfeld und Schließen-Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Padding für die Suchleiste selbst
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface), // Theme-Farbe
                    placeholder = { Text("Suche nach Titel...") },
                    singleLine = true
                )
                IconButton(onClick = {
                    onSearchExpandedChange(false)
                    onSearchTextChange("") // Setzt auch den Suchtext zurück
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Suche schließen")
                }
            }

            // NEU: Filter-Chips für die Medientyp-Auswahl
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), // Zentriert die Chips, wenn Platz ist
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filterTypes = listOf(
                    MediumType.MOVIE,
                    MediumType.SERIES,
                    MediumType.GAME,
                    MediumType.BOOK
                )

                filterTypes.forEach { type ->
                    FilterChip(
                        selected = (type == selectedSearchType),
                        onClick = {
                            if (type != MediumType.BOOK) { // Buch-Auswahl verhindern
                                onSearchTypeSelected(type)
                            }
                        },
                        label = {
                            Text(
                                when (type) { // Angepasste Namen für die UI
                                    MediumType.MOVIE -> "Filme"
                                    MediumType.SERIES -> "Serien"
                                    MediumType.GAME -> "Spiele"
                                    MediumType.BOOK -> "Bücher"
                                    MediumType.UNKNOWN -> "Unbekannt"
                                }
                            )
                        },
                        enabled = type != MediumType.BOOK, // Buch-Chip ist deaktiviert
                        leadingIcon = if (type == selectedSearchType && type != MediumType.BOOK) {
                            { Icon(Icons.Filled.Check, contentDescription = "Ausgewählt") }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}