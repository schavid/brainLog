package com.example.brainlog.view


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun SearchLogic(content: @Composable (isSearchExpanded: Boolean, searchText: String, onSearchExpandedChange: (Boolean) -> Unit, onSearchTextChange: (String) -> Unit, searchResults: List<String>) -> Unit) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }

    // "Fake API Call"
    LaunchedEffect(searchText) {
        if (searchText.isNotBlank()) {
            delay(500) // simuliere Netzwerklatenz
            searchResults = listOf(
                "$searchText Ergebnis 1",
                "$searchText Ergebnis 2",
                "$searchText Ergebnis 3"
            )
        } else {
            searchResults = emptyList()
        }
    }

    content(isSearchExpanded, searchText, { isSearchExpanded = it }, { searchText = it }, searchResults)
}