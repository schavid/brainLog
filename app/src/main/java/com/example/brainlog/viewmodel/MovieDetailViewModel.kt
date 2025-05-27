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
import com.example.brainlog.model.RawgApiClient




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
    private val repository: MediaRepository, // KEIN Standardwert mehr hier! Wird von der Factory übergeben.
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
                val mediumDetails: Medium = when (type) {
                    MediumType.MOVIE -> {
                        repository.getMovieDetails(mediaId)
                    }
                    MediumType.SERIES -> {
                        repository.getSeriesDetails(mediaId)
                    }
                    MediumType.GAME -> { // Hier die Implementierung
                        repository.getGameDetails(mediaId) // Ruft die neue Repository-Methode auf
                    }
                    MediumType.BOOK -> { // Bleibt als TODO, falls du es später implementieren möchtest
                        Log.w("MovieDetailVM", "Book type not yet implemented")
                        throw NotImplementedError("Book details not implemented.")
                    }
                    MediumType.UNKNOWN -> { // Optional: Fallback für unbekannte Typen
                        Log.e("MovieDetailVM", "Unknown medium type: $type for ID: $mediaId")
                        throw IllegalArgumentException("Unknown medium type for detail view.")
                    }
                }
                _uiState.value = MediaDetailUiState.Success(mediumDetails)

            } catch (e: Exception) {
                Log.e("MovieDetailVM", "Error loading details for $mediaId ($type)", e)
                _uiState.value = MediaDetailUiState.Error(e.message ?: "Unbekannter Fehler beim Laden der Details.")
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
            // Erstelle hier das MediaRepository mit beiden API-Services
            val mediaRepository = MediaRepository(
                tmdbApi = ApiClient.mediaApi,     // Dein TMDB-Service
                rawgApi = RawgApiClient.rawgApi   // Dein RAWG-Service
            )
            // Übergebe das initialisierte Repository an das ViewModel
            return MovieDetailViewModel(
                repository = mediaRepository,
                auth = auth,
                db = db
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

