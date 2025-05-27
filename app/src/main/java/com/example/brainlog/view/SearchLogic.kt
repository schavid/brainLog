package com.example.brainlog.view // Oder dein passendes Package

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.example.brainlog.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.FlowPreview
import com.example.brainlog.model.MediumType // Importiere MediumType

@OptIn(FlowPreview::class)
@Composable
fun SearchLogic(
    viewModel: SearchViewModel,
    // Die Content-Lambda muss jetzt auch die neuen Parameter für die SearchBar erhalten
    content: @Composable (
        isSearchExpanded: Boolean,
        searchText: String,
        selectedSearchType: MediumType, // << NEU
        onSearchExpandedChange: (Boolean) -> Unit,
        onSearchTextChange: (String) -> Unit,
        onSearchTypeSelected: (MediumType) -> Unit // << NEU
    ) -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedSearchType by remember { mutableStateOf(MediumType.MOVIE) } // << NEU: State für Suchtyp

    LaunchedEffect(searchText, selectedSearchType) { // << Reagiert jetzt auch auf Änderungen des Suchtyps
        snapshotFlow { Pair(searchText, selectedSearchType) } // Kombiniere beide für den Flow
            .debounce(300) // Etwas kürzeres Debounce, da der Typ auch ein Trigger ist
            .distinctUntilChanged()
            .collectLatest { (query, type) -> // Entpacke das Paar
                if (query.length >= 2 && type != MediumType.BOOK) { // Suche nicht für Bücher
                    viewModel.search(query, type) // << Suche mit Query UND Typ
                } else if (query.isEmpty()) {
                    viewModel.reset()
                }
            }
    }

    content(
        isSearchExpanded,
        searchText,
        selectedSearchType, // << NEU: Ausgewählten Typ an Content weitergeben
        { isSearchExpanded = it },
        { newText -> searchText = newText },
        { newType -> selectedSearchType = newType } // << NEU: Callback für Typänderung weitergeben
    )
}