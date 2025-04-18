package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<SearchMedium>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel(
    private val repository: MediaRepository = MediaRepository(ApiClient.mediaApi)
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val result = repository.searchAllFilms(query)
                _uiState.value = SearchUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = SearchUiState.Idle
    }
}