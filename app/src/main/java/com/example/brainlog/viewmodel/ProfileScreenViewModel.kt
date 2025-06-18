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
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _userMediaListState = MutableStateFlow<UserMediaListUiState>(UserMediaListUiState.Idle)
    val userMediaListState: StateFlow<UserMediaListUiState> = _userMediaListState.asStateFlow()

    // Speichert die globalIDs der ausgewählten Medien
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
        Log.d("ProfileScreenVM_Auth", "CurrentUser UID: ${auth.currentUser?.uid}, Email: ${auth.currentUser?.email}")
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
    // In deinem ProfileScreenViewModel.kt

    fun parseGlobalIdToComponents(globalID: String): ParsedGlobalId? {
        Log.d("ProfileScreenVM_Parse", "Parse-Attempt: globalID = '$globalID'") // LOG 1
        val parts = globalID.split("_")
        if (parts.size == 3) {
            try {
                val apiProvider = parts[0]
                val typeString = parts[1].uppercase() // Wichtig für zuverlässigen when-Vergleich
                val originalId = parts[2].toInt()

                val mediaType = when (typeString) {
                    MediumType.MOVIE.name -> MediumType.MOVIE
                    MediumType.SERIES.name -> MediumType.SERIES
                    MediumType.BOOK.name -> MediumType.BOOK
                    MediumType.GAME.name -> MediumType.GAME
                    else -> {
                        Log.w("ProfileScreenVM_Parse", "Parse-FAIL: Unknown typeString '$typeString' in globalID '$globalID'") // LOG 2
                        return null
                    }
                }
                val parsed = ParsedGlobalId(apiProvider, mediaType, originalId)
                Log.i("ProfileScreenVM_Parse", "Parse-SUCCESS: '$globalID' -> $parsed") // LOG 3
                return parsed
            } catch (e: NumberFormatException) {
                Log.e("ProfileScreenVM_Parse", "Parse-FAIL: Error parsing originalId from '$globalID'", e) // LOG 4
                return null
            } catch (e: Exception) {
                Log.e("ProfileScreenVM_Parse", "Parse-FAIL: General error parsing '$globalID'", e) // LOG 5
                return null
            }
        } else {
            Log.w("ProfileScreenVM_Parse", "Parse-FAIL: Invalid format for globalID '$globalID'. Expected 3 parts, found ${parts.size}.") // LOG 6
            return null
        }
    }

    // In deinem ProfileScreenViewModel.kt

    private fun fetchUserMedia(globalIdsFromUserDoc: List<String>) {
        Log.i("ProfileScreenVM_Fetch", "fetchUserMedia CALLED with ${globalIdsFromUserDoc.size} globalIDs: $globalIdsFromUserDoc") // LOG A

        if (globalIdsFromUserDoc.isEmpty()) {
            _userMediaListState.value = UserMediaListUiState.NoMediaFound
            Log.i("ProfileScreenVM_Fetch", "No globalIDs to fetch, setting NoMediaFound state.") // LOG B
            return
        }
        _userMediaListState.value = UserMediaListUiState.Loading
        Log.d("ProfileScreenVM_Fetch", "Set UserMediaListUiState to Loading.") // LOG C

        viewModelScope.launch {
            try {
                val currentFinishedIds = _finishedMediaGlobalIds.value
                Log.d("ProfileScreenVM_Fetch", "Current finished IDs count: ${currentFinishedIds.size}") // LOG D

                val deferredMediaItems = globalIdsFromUserDoc.mapNotNull { globalIdString ->
                    Log.d("ProfileScreenVM_Fetch", "Processing globalID: '$globalIdString'") // LOG E
                    val parsedId = parseGlobalIdToComponents(globalIdString) // Nutzt die erweiterte Logging-Funktion

                    if (parsedId == null) {
                        Log.w("ProfileScreenVM_Fetch", "Skipping globalID '$globalIdString' due to parsing failure (parsedId is null).") // LOG F
                        null // mapNotNull wird dies herausfiltern
                    } else {
                        Log.d("ProfileScreenVM_Fetch", "Successfully parsed '$globalIdString' to $parsedId. Starting async fetch for details.") // LOG G
                        async {
                            try {
                                Log.d("ProfileScreenVM_Fetch", "Async fetch START for Type: ${parsedId.mediaType}, OriginalID: ${parsedId.originalId}") // LOG H
                                val medium: Medium? = when (parsedId.mediaType) {
                                    MediumType.MOVIE -> {
                                        Log.d("ProfileScreenVM_Fetch", "Fetching MOVIE details for ID ${parsedId.originalId}")
                                        mediaRepository.getMovieDetails(parsedId.originalId)
                                    }
                                    MediumType.SERIES -> {
                                        Log.d("ProfileScreenVM_Fetch", "Fetching SERIES details for ID ${parsedId.originalId}")
                                        mediaRepository.getSeriesDetails(parsedId.originalId)
                                    }
                                    MediumType.BOOK -> {
                                        Log.w("ProfileScreenVM_Fetch", "BOOK type encountered for ID ${parsedId.originalId}, returning null.")
                                        null
                                    }
                                    MediumType.GAME -> {
                                        Log.d("ProfileScreenVM_Fetch", "Fetching GAME details for ID ${parsedId.originalId}")
                                        mediaRepository.getGameDetails(parsedId.originalId)
                                    }
                                    MediumType.UNKNOWN -> {
                                        Log.w("ProfileScreenVM_Fetch", "UNKNOWN media type for ID ${parsedId.originalId}, returning null.")
                                        null
                                    }
                                }
                                val resultTitle = medium?.title ?: "null (fetch failed or no title)"
                                Log.i("ProfileScreenVM_Fetch", "Async fetch END for Type: ${parsedId.mediaType}, OriginalID: ${parsedId.originalId}. Result: '$resultTitle'") // LOG I

                                medium?.apply {
                                    val isMarkedFinished = currentFinishedIds.contains(this.globalID)
                                    Log.d("ProfileScreenVM_Fetch", "Applying finished state for '${this.globalID}': $isMarkedFinished (currentFinishedIds: $currentFinishedIds)")
                                    isFinished = isMarkedFinished
                                }
                            } catch (e: Exception) {
                                // DIESER LOG IST EXTREM WICHTIG, WENN LOG I "null" ZEIGT
                                Log.e("ProfileScreenVM_Fetch", "Async fetch EXCEPTION for Type: ${parsedId.mediaType}, OriginalID: ${parsedId.originalId}: ${e.message}", e) // LOG J
                                null
                            }
                        }
                    }
                }
                Log.d("ProfileScreenVM_Fetch", "Created ${deferredMediaItems.size} deferred tasks. Awaiting all...") // LOG K
                val fetchedMediaItems = deferredMediaItems.awaitAll().filterNotNull()
                Log.i("ProfileScreenVM_Fetch", "Finished awaiting. Fetched ${fetchedMediaItems.size} non-null media items.") // LOG L

                if (fetchedMediaItems.isEmpty() && globalIdsFromUserDoc.isNotEmpty()) {
                    Log.w("ProfileScreenVM_Fetch", "All detail fetches failed. Setting UserMediaListUiState to Error 'Konnte keine Mediendetails laden.'") // LOG M
                    _userMediaListState.value = UserMediaListUiState.Error("Konnte keine Mediendetails laden.")
                } else if (fetchedMediaItems.isEmpty()) {
                    Log.i("ProfileScreenVM_Fetch", "No media items fetched and globalIdsFromUserDoc was empty. Setting NoMediaFound.") // LOG N
                    _userMediaListState.value = UserMediaListUiState.NoMediaFound
                } else {
                    Log.i("ProfileScreenVM_Fetch", "Successfully fetched ${fetchedMediaItems.size} media items. Setting Success state.") // LOG O
                    _userMediaListState.value = UserMediaListUiState.Success(fetchedMediaItems)
                }
            } catch (e: Exception) { // Äußerer try-catch
                Log.e("ProfileScreenVM_Fetch", "Outer EXCEPTION in fetchUserMedia: ${e.message}", e) // LOG P
                _userMediaListState.value = UserMediaListUiState.Error("Laden der Medienliste: ${e.message}")
            }
        }
    }

    // Ausgewähltes Medium_global id in eine liste
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

    fun toggleFinishedStateForSelected() {
        val mediaObjectsToToggle = getCurrentlySelectedMediaObjects()
        if (mediaObjectsToToggle.isEmpty()) {
            clearSelection()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("ProfileScreenVM", "User not logged in for toggle operation.")
            clearSelection()
            return
        }

        viewModelScope.launch {
            try {
                val userDocRef = db.collection("users").document(currentUser.uid)
                val currentFinishedIds = _finishedMediaGlobalIds.value

                val (alreadyFinished, notYetFinished) = mediaObjectsToToggle.partition {
                    currentFinishedIds.contains(it.globalID)
                }

                // Hole die reinen IDs für die Firestore-Operation
                val idsToMarkAsUnfinished = alreadyFinished.map { it.globalID }
                val idsToMarkAsFinished = notYetFinished.map { it.globalID }

                // 2. Verwende einen WriteBatch für atomare Operationen
                val batch = db.batch()

                // Füge die Operation zum ENTFERNEN zum Batch hinzu (falls nötig)
                if (idsToMarkAsUnfinished.isNotEmpty()) {
                    batch.update(userDocRef, "finishedMediasGlobalIds", FieldValue.arrayRemove(*idsToMarkAsUnfinished.toTypedArray()))
                }

                // Füge die Operation zum HINZUFÜGEN zum Batch hinzu (falls nötig)
                if (idsToMarkAsFinished.isNotEmpty()) {
                    batch.update(userDocRef, "finishedMediasGlobalIds", FieldValue.arrayUnion(*idsToMarkAsFinished.toTypedArray()))
                }

                // 3. Führe den Batch aus
                batch.commit().await()

                fetchUserMedia((_uiState.value as? UserProfileUiState.Success)?.userDocument?.addedMedias ?: emptyList())

            } catch (e: Exception) {
                Log.e("ProfileScreenVM", "Error toggling finished state", e)
                // Optional: Fehler an die UI melden
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