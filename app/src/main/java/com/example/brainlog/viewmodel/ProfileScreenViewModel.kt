package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.Medium
import com.example.brainlog.model.MediumType
import com.example.brainlog.model.UserDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState
    data class Success(val userDocument: UserDocument) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
    data object NotLoggedIn : UserProfileUiState
    data object ProfileNotFound : UserProfileUiState
}

data class ParsedGlobalId(
    val apiProvider: String,
    val mediaType: MediumType,
    val originalId: Int
)

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

    init {
        fetchUserProfileThenMedia()
    }
    //holt den current user aus der firebase auth und holt danach den profildokument aus der db
    fun fetchUserProfileThenMedia() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            val firebaseUser: FirebaseUser? = auth.currentUser //currentUser

            if (firebaseUser == null) {
                _uiState.value = UserProfileUiState.NotLoggedIn
                return@launch
            }

            try {
                val userId = firebaseUser.uid
                val documentSnapshot = db.collection("users").document(userId).get().await() //Datenbankabfrage mittels currentuser

                if (documentSnapshot.exists()) {
                    val userDocument = documentSnapshot.toObject<UserDocument>()
                    if (userDocument != null) {
                        _uiState.value = UserProfileUiState.Success(userDocument)
                        fetchUserMedia(userDocument.addedMedias) //Aufruf an fetchUserMedia
                    } else {

                        _uiState.value = UserProfileUiState.Error("Fehler beim Konvertieren der Profildaten.")
                    }
                } else {
                    _uiState.value = UserProfileUiState.ProfileNotFound
                }
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error("Fehler beim Laden des Profils: ${e.localizedMessage}")
            }
        }
    }

    // parsed die globale ID um es daraus ein objekt zu mache, das ich wieder für den API call nutzen kann
    fun parseGlobalIdToComponents(globalID: String): ParsedGlobalId? {
        val parts = globalID.split("_")
        if (parts.size == 3) {
            try {
                val apiProvider = parts[0]
                val typeString = parts[1]
                val originalId = parts[2].toInt()

                val mediaType = when (typeString.uppercase()) {
                    "MOVIE" -> MediumType.MOVIE
                    "SERIES" -> MediumType.SERIES
                    "BOOK" -> MediumType.BOOK
                    "GAME" -> MediumType.GAME
                    else -> return null
                }
                return ParsedGlobalId(apiProvider, mediaType, originalId)
            } catch (e: NumberFormatException) {
                return null
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    // Funktion, um die Medien basierend auf den globalIDs zu laden
    private fun fetchUserMedia(globalIds: List<String>) {
        if (globalIds.isEmpty()) {
            _userMediaListState.value = UserMediaListUiState.NoMediaFound
            return
        }

        _userMediaListState.value = UserMediaListUiState.Loading
        //hier wird der api call gestartet und über parsedId  die orignalID aufgerufen und der call abgesetzt und das asynchron
        viewModelScope.launch {
            try {
                val deferredMediaItems = globalIds.mapNotNull { globalIdString ->
                    parseGlobalIdToComponents(globalIdString)?.let { parsedId ->
                        async {
                            try {
                                when (parsedId.mediaType) {
                                    MediumType.MOVIE -> mediaRepository.getMovieDetails(parsedId.originalId)
                                    MediumType.SERIES -> mediaRepository.getSeriesDetails(parsedId.originalId)
                                    // MediumType.BOOK -> mediaRepository.getBookDetails(parsedId.originalId) // Falls implementiert
                                    // MediumType.GAME -> mediaRepository.getGameDetails(parsedId.originalId) // Falls implementiert
                                    else -> {
                                        println("Unbekannter Medientyp in fetchUserMedia: ${parsedId.mediaType}")
                                        null
                                    }
                                }
                            } catch (e: Exception) {
                                println("Fehler beim Abrufen der Details für ${parsedId.mediaType} ID ${parsedId.originalId} ($globalIdString): ${e.message}")
                                null
                            }
                        }
                    }
                }

                // Warte auf alle Ergebnisse und filtere null-Werte (fehlerhafte Aufrufe) heraus
                val fetchedMediaItems = deferredMediaItems.awaitAll().filterNotNull()

                if (fetchedMediaItems.isEmpty() && globalIds.isNotEmpty()) {
                    // Es gab IDs, aber keine konnte erfolgreich geladen werden
                    _userMediaListState.value = UserMediaListUiState.Error("Konnte keine Mediendetails laden.")
                } else if (fetchedMediaItems.isEmpty()) {
                    _userMediaListState.value = UserMediaListUiState.NoMediaFound
                }
                else {
                    _userMediaListState.value = UserMediaListUiState.Success(fetchedMediaItems)
                }

            } catch (e: Exception) {
                // Allgemeiner Fehler beim Versuch, die Medienliste zu orchestrieren
                _userMediaListState.value = UserMediaListUiState.Error("Fehler beim Laden der Medienliste: ${e.message}")
            }
        }
    }




}