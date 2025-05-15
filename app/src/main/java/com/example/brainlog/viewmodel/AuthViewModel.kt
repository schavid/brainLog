package com.example.brainlog.viewmodel


import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.brainlog.model.UserDocument
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object LoggedOut : AuthUiState()
}


class AuthViewModel:  ViewModel(){

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()


    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Unexpected login error.")
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.value = AuthUiState.Error("No account found with this email address.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = AuthUiState.Error("Incorrect password.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Login failed.")
            }
        }
    }




    fun register(email: String, password: String, username: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _uiState.value = AuthUiState.Error("Email, password, and username cannot be empty.")
            return
        }

        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val userDoc = UserDocument(
                        userId = firebaseUser.uid,
                        email = email,
                        username = username,
                        addedMedias = emptyList()
                    )
                    saveUserData(firebaseUser.uid, userDoc)
                    _uiState.value = AuthUiState.Success(firebaseUser)
                } else {
                    _uiState.value = AuthUiState.Error("Failed to create user account: Firebase user is null.")
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.value = AuthUiState.Error("This email address is already in use.")
            } catch (e: FirebaseAuthWeakPasswordException) {
                _uiState.value = AuthUiState.Error("Password is too weak. It must be at least 6 characters long.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed due to an unknown error.")
            }
        }
    }


    private suspend fun saveUserData(userId: String, userDoc: UserDocument) {
        try {
            db.collection("users").document(userId).set(userDoc).await()
        } catch (e: Exception) {
            // Leite den Fehler nach oben, damit er im 'register' catch-Block behandelt wird
            throw Exception("Failed to save user profile data: ${e.localizedMessage}", e)
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.LoggedOut
    }


    fun resetStateToIdle() {
        _uiState.value = AuthUiState.Idle
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }



}