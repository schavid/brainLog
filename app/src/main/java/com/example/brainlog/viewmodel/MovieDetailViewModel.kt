package com.example.brainlog.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.Medium
import com.example.brainlog.model.MediumType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



sealed class MediaDetailUiState {
    object Idle : MediaDetailUiState()
    object Loading : MediaDetailUiState()
    data class Success(val medium: Medium) : MediaDetailUiState()
    data class Error(val message: String) : MediaDetailUiState()
}

sealed class AddMediumToUserUiState {
    object Idle : AddMediumToUserUiState()
    object Loading : AddMediumToUserUiState()
    object Success : AddMediumToUserUiState()
    data class Error(val message: String) : AddMediumToUserUiState()
    object UserNotLoggedIn : AddMediumToUserUiState() // Spezifischer Fall
}

class MovieDetailViewModel(
    private val repository: MediaRepository = MediaRepository(ApiClient.mediaApi),
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Idle)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    private val _addMediumToUserUiState = MutableStateFlow<AddMediumToUserUiState>(AddMediumToUserUiState.Idle)
    val addMediumToUserUiState: StateFlow<AddMediumToUserUiState> = _addMediumToUserUiState.asStateFlow()

    fun loadMovieDetail(mediaId: Int, type: MediumType) {
        Log.d("MovieDetailVM", "loadMovieDetail called with ID: $mediaId, Type: $type")
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
                Log.e("MovieDetailVM", "Error loading details for $mediaId ($type)", e)
                _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addMediumToUser(medium: Medium) {
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser == null) {
            _addMediumToUserUiState.value = AddMediumToUserUiState.UserNotLoggedIn
            return
        }

        if (medium.globalID.isBlank()) {
            _addMediumToUserUiState.value = AddMediumToUserUiState.Error("Medium hat eine ungültige ID.")
            return
        }

        _addMediumToUserUiState.value = AddMediumToUserUiState.Loading

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.update("addedMedias", FieldValue.arrayUnion(medium.globalID))
                    .await()
                _addMediumToUserUiState.value = AddMediumToUserUiState.Success
            } catch (e: Exception) {
                _addMediumToUserUiState.value = AddMediumToUserUiState.Error(e.message ?: "Fehler beim Hinzufügen des Mediums")
            }
        }
    }

    fun resetAddMediumState() {
        _addMediumToUserUiState.value = AddMediumToUserUiState.Idle
    }

    fun reset() {
        _uiState.value = MediaDetailUiState.Idle
    }
}

class MovieDetailViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(auth = auth, db = db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

