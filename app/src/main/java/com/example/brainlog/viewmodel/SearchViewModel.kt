package com.example.brainlog.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient // Dein TMDB ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.RawgApiClient // Der neue RAWG ApiClient
import com.example.brainlog.model.SearchMedium // Deine angepasste SearchMedium Klasse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.brainlog.model.MediumType

class SearchViewModel(
    private val repository: MediaRepository = MediaRepository(
        mediaApi = ApiClient.mediaApi,
        rawgApi = RawgApiClient.rawgApi
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String, searchType: MediumType) { // <<-- SIGNATUR MIT ZWEI PARAMETERN
        Log.d("SearchViewModel", "Search called. Query: '$query', Type: $searchType")

        if (query.isBlank()) {
            _uiState.value = SearchUiState.Success(emptyList())
            return
        }
        if (searchType == MediumType.BOOK) { // Nicht nach Büchern suchen
            _uiState.value = SearchUiState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                // Ruft die Repository-Methode auf, die wir gleich anpassen/umbenennen
                val result = repository.searchMediaByType(query, searchType) // <<-- AUFRUF AN REPOSITORY
                Log.d("SearchViewModel", "Search results count: ${result.size} for type: $searchType")
                _uiState.value = SearchUiState.Success(result)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error during search for type $searchType, query '$query': ${e.message}", e)
                _uiState.value = SearchUiState.Error("Fehler bei der Suche: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = SearchUiState.Idle
    }


}
sealed class SearchUiState {
    object Idle : SearchUiState() // Zustand, wenn nichts passiert oder die Suche zurückgesetzt wurde
    object Loading : SearchUiState() // Zustand, während die Suche lädt
    data class Success(
        val results: List<SearchMedium> // Zustand, wenn die Suche erfolgreich war und Ergebnisse hat
    ) : SearchUiState()
    data class Error(
        val message: String // Zustand, wenn ein Fehler bei der Suche aufgetreten ist
    ) : SearchUiState()
}