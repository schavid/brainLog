package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainlog.model.UserDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState
    data class Success(val userDocument: UserDocument) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
    data object NotLoggedIn : UserProfileUiState // Wenn kein User angemeldet ist
    data object ProfileNotFound : UserProfileUiState // User ist angemeldet, aber kein Profildokument
}


class ProfileScreenViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() { // Umbenannt für Klarheit
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
                        _uiState.value = UserProfileUiState.Success(userDocument) // Ganzes Objekt übergeben an uiState
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
}