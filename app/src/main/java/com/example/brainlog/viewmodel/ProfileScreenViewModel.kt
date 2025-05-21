package com.example.brainlog.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.Medium
import com.example.brainlog.model.MediumType
import com.example.brainlog.model.UserDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// UserProfileUiState bleibt gleich
sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState
    data class Success(val userDocument: UserDocument) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
    data object NotLoggedIn : UserProfileUiState
    data object ProfileNotFound : UserProfileUiState
}

// ParsedGlobalId bleibt gleich
data class ParsedGlobalId(
    val apiProvider: String,
    val mediaType: MediumType,
    val originalId: Int
)

// UserMediaListUiState: parsedIds aus Success entfernt, da nicht direkt von UI benötigt
sealed interface UserMediaListUiState {
    data object Idle : UserMediaListUiState
    data object Loading : UserMediaListUiState
    data class Success(val mediaItems: List<Medium>) : UserMediaListUiState
    data class Error(val message: String) : UserMediaListUiState
    data object NoMediaFound : UserMediaListUiState
}


class ProfileScreenViewModel(
    private val mediaRepository: MediaRepository = MediaRepository(ApiClient.mediaApi)
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _userMediaListState = MutableStateFlow<UserMediaListUiState>(UserMediaListUiState.Idle)
    val userMediaListState: StateFlow<UserMediaListUiState> = _userMediaListState.asStateFlow()

    // NEU: Speichert die globalIDs der ausgewählten Medien
    private val _selectedMediaGlobalIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMediaGlobalIds: StateFlow<Set<String>> = _selectedMediaGlobalIds.asStateFlow()

    // StateFlow für die globalen IDs der als "finished" (gesehen) markierten Medien
    private val _finishedMediaGlobalIds = MutableStateFlow<Set<String>>(emptySet())

    private var finishedMediasListener: ListenerRegistration? = null

    val isInSelectionMode: StateFlow<Boolean> = _selectedMediaGlobalIds // Prüft jetzt diesen State
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        observeFinishedMediaGlobalIds()
        fetchUserProfileThenMedia()
    }

    private fun observeFinishedMediaGlobalIds() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _finishedMediaGlobalIds.value = emptySet()
            return
        }
        finishedMediasListener?.remove()
        finishedMediasListener = db.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ProfileScreenVM", "Listen failed for finishedMedias.", error)
                    _finishedMediaGlobalIds.value = emptySet()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Name des Feldes in Firestore: "finishedMediasGlobalIds" (oder wie du es nennen willst)
                    val ids = snapshot.get("finishedMediasGlobalIds") as? List<String> ?: emptyList()
                    _finishedMediaGlobalIds.value = ids.toSet()
                } else {
                    _finishedMediaGlobalIds.value = emptySet()
                }
            }
    }

    fun fetchUserProfileThenMedia() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            val firebaseUser: FirebaseUser? = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = UserProfileUiState.NotLoggedIn
                _userMediaListState.value = UserMediaListUiState.Idle
                return@launch
            }
            try {
                val userId = firebaseUser.uid
                val documentSnapshot = db.collection("users").document(userId).get().await()
                if (documentSnapshot.exists()) {
                    val userDocument = documentSnapshot.toObject<UserDocument>()
                    if (userDocument != null) {
                        _uiState.value = UserProfileUiState.Success(userDocument)
                        fetchUserMedia(userDocument.addedMedias)
                    } else {
                        _uiState.value = UserProfileUiState.Error("Fehler beim Konvertieren der Profildaten.")
                    }
                } else {
                    _uiState.value = UserProfileUiState.ProfileNotFound
                }
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error("Laden des Profils: ${e.localizedMessage}")
            }
        }
    }

    // parseGlobalIdToComponents bleibt gleich, stelle sicher, dass es mit deinem globalID-Format übereinstimmt
    fun parseGlobalIdToComponents(globalID: String): ParsedGlobalId? {
        val parts = globalID.split("_")
        // Erwartet 3 Teile: "PROVIDER_TYP_ID" z.B. "TMDB_MOVIE_123"
        if (parts.size == 3) {
            try {
                val apiProvider = parts[0]
                val typeString = parts[1]
                val originalId = parts[2].toInt()

                val mediaType = when (typeString.uppercase()) {
                    MediumType.MOVIE.name -> MediumType.MOVIE // Verwende .name für Enum-Vergleich
                    MediumType.SERIES.name -> MediumType.SERIES
                    MediumType.BOOK.name -> MediumType.BOOK
                    MediumType.GAME.name -> MediumType.GAME
                    else -> {
                        Log.w("ProfileScreenVM", "Unbekannter Typ-String '$typeString' in globalID '$globalID'")
                        return null
                    }
                }
                return ParsedGlobalId(apiProvider, mediaType, originalId)
            } catch (e: NumberFormatException) {
                Log.e("ProfileScreenVM", "Fehler beim Parsen der ID in '$globalID'", e)
                return null
            } catch (e: Exception) {
                Log.e("ProfileScreenVM", "Allgemeiner Fehler beim Parsen von '$globalID'", e)
                return null
            }
        } else {
            Log.w("ProfileScreenVM", "Ungültiges Format für globalID '$globalID'. Erwartet 3 Teile (PROVIDER_TYP_ID).")
        }
        return null
    }

    private fun fetchUserMedia(globalIdsFromUserDoc: List<String>) {
        if (globalIdsFromUserDoc.isEmpty()) {
            _userMediaListState.value = UserMediaListUiState.NoMediaFound
            return
        }
        _userMediaListState.value = UserMediaListUiState.Loading

        viewModelScope.launch {
            try {
                val currentFinishedIds = _finishedMediaGlobalIds.value

                val deferredMediaItems = globalIdsFromUserDoc.mapNotNull { globalIdString ->
                    parseGlobalIdToComponents(globalIdString)?.let { parsedId ->
                        async {
                            try {
                                val medium: Medium? = when (parsedId.mediaType) {
                                    MediumType.MOVIE -> mediaRepository.getMovieDetails(parsedId.originalId)
                                    MediumType.SERIES -> mediaRepository.getSeriesDetails(parsedId.originalId)
                                    MediumType.BOOK -> null // Implementiere, falls benötigt
                                    MediumType.GAME -> null  // Implementiere, falls benötigt
                                }
                                medium?.apply {
                                    // Stelle sicher, dass 'isFinished' in deiner Medium-Definition existiert und var ist
                                    isFinished = currentFinishedIds.contains(this.globalID) // Verwende this.globalID
                                }
                            } catch (e: Exception) {
                                Log.e("ProfileScreenVM","Details für ${parsedId.mediaType} ID ${parsedId.originalId}: ${e.message}")
                                null
                            }
                        }
                    }
                }
                val fetchedMediaItems = deferredMediaItems.awaitAll().filterNotNull()

                if (fetchedMediaItems.isEmpty() && globalIdsFromUserDoc.isNotEmpty()) {
                    _userMediaListState.value = UserMediaListUiState.Error("Konnte keine Mediendetails laden.")
                } else if (fetchedMediaItems.isEmpty()) {
                    _userMediaListState.value = UserMediaListUiState.NoMediaFound
                } else {
                    _userMediaListState.value = UserMediaListUiState.Success(fetchedMediaItems)
                }
            } catch (e: Exception) {
                _userMediaListState.value = UserMediaListUiState.Error("Laden der Medienliste: ${e.message}")
            }
        }
    }

    // NEU: toggleMediaSelection nimmt jetzt ein Medium-Objekt
    fun toggleMediaSelection(medium: Medium) {
        val currentSelection = _selectedMediaGlobalIds.value.toMutableSet()
        // Verwende die globalID vom Medium-Objekt
        if (currentSelection.contains(medium.globalID)) {
            currentSelection.remove(medium.globalID)
        } else {
            currentSelection.add(medium.globalID)
        }
        _selectedMediaGlobalIds.value = currentSelection
    }

    fun clearSelection() {
        _selectedMediaGlobalIds.value = emptySet()
    }

    // Hilfsfunktion, um die aktuell ausgewählten Medium-Objekte zu bekommen
    private fun getCurrentlySelectedMediaObjects(): List<Medium> {
        val currentMediaListState = userMediaListState.value
        val selectedIds = _selectedMediaGlobalIds.value

        if (currentMediaListState is UserMediaListUiState.Success && selectedIds.isNotEmpty()) {
            return currentMediaListState.mediaItems.filter { medium ->
                selectedIds.contains(medium.globalID)
            }
        }
        return emptyList()
    }

    fun deleteSelectedMedia() {
        val mediaObjectsToDelete = getCurrentlySelectedMediaObjects()
        if (mediaObjectsToDelete.isEmpty()) {
            clearSelection() // Falls _selectedMediaGlobalIds nicht leer war, aber keine Objekte gefunden
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("ProfileScreenVM", "User not logged in for delete operation.")
            clearSelection()
            return
        }

        _userMediaListState.value = UserMediaListUiState.Loading // UI Feedback

        viewModelScope.launch {
            val userDocRef = db.collection("users").document(currentUser.uid)
            // globalIDs von den Objekten nehmen
            val globalIdsForFirestore = mediaObjectsToDelete.map { it.globalID }

            Log.d("ProfileScreenVM", "Attempting to delete from Firestore: $globalIdsForFirestore")
            val originalStateBeforeLoading = userMediaListState.value // Für Fehlerfall

            try {
                userDocRef.update("addedMedias", FieldValue.arrayRemove(*globalIdsForFirestore.toTypedArray()))
                    .await()
                // Auch aus finishedMediasGlobalIds entfernen
                userDocRef.update("finishedMediasGlobalIds", FieldValue.arrayRemove(*globalIdsForFirestore.toTypedArray()))
                    .await()

                fetchUserProfileThenMedia() // Lädt die Listen neu
            } catch (e: Exception) {
                Log.e("ProfileScreenVM", "Error deleting selected media from Firestore", e)
                if (_userMediaListState.value is UserMediaListUiState.Loading) {
                    _userMediaListState.value = originalStateBeforeLoading // Alten Zustand wiederherstellen
                }
                // Optional: spezifischen Fehler an UI melden
            } finally {
                clearSelection()
            }
        }
    }

    // Die alte generateGlobalId(id, type) ist nicht mehr primär nötig,
    // da wir jetzt mit Medium-Objekten arbeiten, die ihre globalID haben.
    // Behalte sie, falls du sie an anderer Stelle noch brauchst, ansonsten kann sie weg.

    fun markSelectedAsFinished() {
        val mediaObjectsToMark = getCurrentlySelectedMediaObjects()
        if (mediaObjectsToMark.isEmpty()) {
            clearSelection()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("ProfileScreenVM", "User not logged in for mark as finished operation.")
            clearSelection()
            return
        }

        // _userMediaListState.value = UserMediaListUiState.Loading // Optional: UI Feedback
        val originalStateBeforeLoading = userMediaListState.value // Für Fehlerfall

        viewModelScope.launch {
            val userDocRef = db.collection("users").document(currentUser.uid)

            // globalIDs von den Objekten nehmen
            val globalIdsForFirestore = mediaObjectsToMark.map { it.globalID }


            try {
                // Name des Feldes: "finishedMediasGlobalIds"
                userDocRef.update("finishedMediasGlobalIds", FieldValue.arrayUnion(*globalIdsForFirestore.toTypedArray()))
                    .await()

                // Die _finishedMediaGlobalIds werden durch den Listener aktualisiert.
                // Damit die Medium-Objekte in der UI ihr isFinished-Flag aktualisieren,
                // müssen wir die Medienliste neu laden.
                fetchUserMedia((_uiState.value as? UserProfileUiState.Success)?.userDocument?.addedMedias ?: emptyList())

            } catch (e: Exception) {
                Log.e("ProfileScreenVM", "Error marking media as finished", e)
                if (_userMediaListState.value is UserMediaListUiState.Loading && _userMediaListState.value != originalStateBeforeLoading) { // Nur wenn wir es explizit auf Loading gesetzt haben
                    _userMediaListState.value = originalStateBeforeLoading
                }
                // Optional: spezifischen Fehler an UI melden
            } finally {
                clearSelection()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        finishedMediasListener?.remove()
        finishedMediasListener = null
    }
}