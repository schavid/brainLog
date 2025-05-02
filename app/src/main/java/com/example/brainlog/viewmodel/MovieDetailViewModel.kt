package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.Medium
import com.example.brainlog.model.MediumType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


sealed class MediaDetailUiState {
    object Idle : MediaDetailUiState()
    object Loading : MediaDetailUiState()
    data class Success(val medium: Medium) : MediaDetailUiState()
    data class Error(val message: String) : MediaDetailUiState()
}


class MovieDetailViewModel(
    private val repository: MediaRepository = MediaRepository(ApiClient.mediaApi)
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Idle)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    fun loadMovieDetail(mediaId: Int, type: MediumType) {
        viewModelScope.launch {
            _uiState.value = MediaDetailUiState.Loading
            try {
                when (type) {
                    MediumType.MOVIE -> {
                        // Filme: Rufe die Details vom Film-Repository ab
                        val movieDetails = repository.getMovieDetails(mediaId) // Aufruf für Filme
                        _uiState.value = MediaDetailUiState.Success(movieDetails)
                    }

                    MediumType.SERIES -> {
                        // Serien: Rufe die Details vom Serien-Repository ab
                        val seriesDetails =
                            repository.getSeriesDetails(mediaId) // Aufruf für Serien
                        _uiState.value = MediaDetailUiState.Success(seriesDetails)
                    }

                    MediumType.BOOK -> {
                        // Beispiel für zukünftige Erweiterung (Bücher, etc.)
                        TODO("Implementiere die Logik für Bücher")
                    }

                    MediumType.GAME -> {
                        // Beispiel für zukünftige Erweiterung (Spiele, etc.)
                        TODO("Implementiere die Logik für Spiele")
                    }
                }
            }catch (e: Exception) {
                _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _uiState.value = MediaDetailUiState.Idle
    }
}

