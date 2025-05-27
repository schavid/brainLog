package com.example.brainlog.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brainlog.model.SearchMedium
import com.example.brainlog.model.MediumType // Stelle sicher, dass dieser Import korrekt ist

@Composable
fun SearchResultsList(results: List<SearchMedium>, onClick: (SearchMedium) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { result ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick(result)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.surface, // Nutze Theme-Farben
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp), // Etwas weniger Padding innen
                    verticalAlignment = Alignment.Top // Besser für Textblöcke unterschiedlicher Höhe
                ) {
                    // --- Bildanzeige ---
                    // displayPosterUrl enthält jetzt immer die komplette URL
                    result.displayPosterUrl?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = result.displayTitle, // Nutze displayTitle
                            modifier = Modifier
                                .width(90.dp) // Leicht angepasst
                                .height(135.dp) // Leicht angepasst
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box( // Platzhalter, falls kein Bild vorhanden
                        modifier = Modifier
                            .width(90.dp)
                            .height(135.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kein Bild", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // --- Textuelle Details ---
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.displayTitle, // Nutze displayTitle
                            style = MaterialTheme.typography.titleMedium, // Angepasst für bessere Hierarchie
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        result.displayReleaseDate?.let { releaseDate ->
                            if (releaseDate.isNotBlank()) {
                                Text(
                                    "Veröffentlicht: $releaseDate", // Nutze displayReleaseDate
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Spezifische Infos je nach Typ (optional erweiterbar)
                        when (result) {
                            is SearchMedium.Game -> {
                                result.platforms?.take(2)?.joinToString(", ")?.let { platforms ->
                                    if (platforms.isNotBlank()){
                                        Text(
                                            "Plattformen: $platforms",
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            // Für Movie und Series könntest du hier spezifische Infos aus
                            // den jeweiligen data classes anzeigen, falls du sie in SearchMedium.Movie/Series
                            // mit 'overview' etc. erweitert hast.
                            is SearchMedium.Movie -> {
                                // Optional: zeige z.B. result.overview (wenn in SearchMedium.Movie verfügbar)
                            }
                            is SearchMedium.Series -> {
                                // Optional: zeige z.B. result.overview (wenn in SearchMedium.Series verfügbar)
                            }
                        }

                        // Typ-Anzeige unten rechts
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = when (result.displayMediaType) { // Nutze displayMediaType
                                    MediumType.MOVIE -> "Film"
                                    MediumType.SERIES -> "Serie"
                                    MediumType.GAME -> "Spiel"
                                    MediumType.UNKNOWN -> "Unbekannt"
                                    MediumType.BOOK -> TODO()
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}