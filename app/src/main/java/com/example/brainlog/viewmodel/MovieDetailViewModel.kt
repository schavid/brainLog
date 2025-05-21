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



sealed class MediaDetailUiState {
    object Idle : MediaDetailUiState()
    object Loading : MediaDetailUiState()
    data class Success(val medium: Medium) : MediaDetailUiState()
    data class Error(val message: String) : MediaDetailUiState()
}

sealed class UpdateUserMediaListUiState {
    object Idle : UpdateUserMediaListUiState()
    object Loading : UpdateUserMediaListUiState()
    object AddSuccess : UpdateUserMediaListUiState()       // Spezifisch für Hinzufügen erfolgreich
    object DeleteSuccess : UpdateUserMediaListUiState()
    data class Error(val message: String) : UpdateUserMediaListUiState()
    object UserNotLoggedIn : UpdateUserMediaListUiState()
}

class MovieDetailViewModel(
    private val repository: MediaRepository = MediaRepository(ApiClient.mediaApi),
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Idle)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    private val _updateUserMediaListUiState = MutableStateFlow<UpdateUserMediaListUiState>(UpdateUserMediaListUiState.Idle)
    val updateUserMediaListUiState: StateFlow<UpdateUserMediaListUiState> = _updateUserMediaListUiState.asStateFlow()

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
            _updateUserMediaListUiState.value = UpdateUserMediaListUiState.UserNotLoggedIn
            return
        }

        if (medium.globalID.isBlank()) {
            // Wichtig: globalID muss im Medium Objekt korrekt gesetzt sein!
            // z.B. movie_123, series_456
            _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Error("Medium hat eine ungültige GlobalID.")
            return
        }

        _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Loading

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                userDocRef.update("addedMedias", FieldValue.arrayUnion(medium.globalID))
                    .await()
                _updateUserMediaListUiState.value = UpdateUserMediaListUiState.AddSuccess // Spezifischer Erfolgszustand
            } catch (e: Exception) {
                Log.e("MovieDetailVM", "Error adding medium ${medium.globalID} to user ${currentUser.uid}", e)
                _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Error(e.message ?: "Fehler beim Hinzufügen des Mediums")
            }
        }
    }


    fun deleteMediumFromUser(medium: Medium) {
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser == null) {
            _updateUserMediaListUiState.value = UpdateUserMediaListUiState.UserNotLoggedIn
            return
        }

        if (medium.globalID.isBlank()) {
            // Wichtig: globalID muss im Medium Objekt korrekt gesetzt sein!
            _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Error("Medium hat eine ungültige GlobalID zum Löschen.")
            return
        }

        _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Loading

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                // Firestore-Operation zum Entfernen eines Elements aus einem Array
                userDocRef.update("addedMedias", FieldValue.arrayRemove(medium.globalID))
                    .await()
                _updateUserMediaListUiState.value = UpdateUserMediaListUiState.DeleteSuccess // Spezifischer Erfolgszustand
            } catch (e: Exception) {
                Log.e("MovieDetailVM", "Error deleting medium ${medium.globalID} from user ${currentUser.uid}", e)
                _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Error(e.message ?: "Fehler beim Entfernen des Mediums")
            }
        }
    }

    fun resetUpdateUserMediaListState() {
        _updateUserMediaListUiState.value = UpdateUserMediaListUiState.Idle
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

