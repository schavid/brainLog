package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient // Dein TMDB ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.RawgApiClient // Der neue RAWG ApiClient
import com.example.brainlog.model.SearchMedium // Deine angepasste SearchMedium Klasse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Dein SearchUiState sollte so bleiben können, da es bereits List<SearchMedium> erwartet.
// Falls du es nicht schon hast, hier ist es zur Vollständigkeit:
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val results: List<SearchMedium> // Hält jetzt eine Liste von SearchMedium (Filme, Serien, Spiele)
    ) : SearchUiState()
    data class Error(
        val message: String
    ) : SearchUiState()
}

class SearchViewModel(
    // Passe die Initialisierung des Repositories an:
    private val repository: MediaRepository = MediaRepository(
        tmdbApi = ApiClient.mediaApi,      // Dein bestehender TMDB API Service
        rawgApi = RawgApiClient.rawgApi    // Der neue RAWG API Service
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        // Optional: Wenn der Query leer ist, direkt leere Ergebnisse oder Idle-State setzen.
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Success(emptyList()) // Oder SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                // Verwende die neue Methode, die alle Medientypen durchsucht
                val result = repository.searchAllMedia(query)
                _uiState.value = SearchUiState.Success(result)
            } catch (e: Exception) {
                // Eine etwas spezifischere Fehlermeldung für den Nutzer
                _uiState.value = SearchUiState.Error("Fehler bei der Suche: ${e.localizedMessage ?: "Unbekannter Fehler"}")
                // Optional: Logge den detaillierten Fehler für Debugging-Zwecke
                // Log.e("SearchViewModel", "Search failed", e)
            }
        }
    }

    fun reset() {
        _uiState.value = SearchUiState.Idle
    }
}