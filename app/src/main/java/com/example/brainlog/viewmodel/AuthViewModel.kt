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

class AuthViewModel:  ViewModel(){

    private val auth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)
        Log.d("AuthViewModel", "Current User: ${auth.currentUser?.email}")
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try { 
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: FirebaseAuthInvalidUserException) {
                _errorMessage.value = "No account found with this email address."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // --- HIER: Falsches Passwort (oder manchmal auch User nicht gefunden, Firebase ist da nicht 100% konsistent) ---
                _errorMessage.value = "Incorrect password. Please try again."
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Login failed. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun register(email: String, password: String, username: String) {

        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _errorMessage.value = "Email, password, and username cannot be empty."
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters long."
            return
        }


        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {

                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val userDoc = UserDocument(
                        userId = firebaseUser.uid,
                        email = email,
                        username = username
                    )
                    saveUserData(firebaseUser.uid, userDoc)

                } else {
                    throw Exception("Failed to create user account.")
                }

            } catch (e: FirebaseAuthUserCollisionException) {
                _errorMessage.value = ("This email address is already in use.")
            }  catch (e: FirebaseAuthWeakPasswordException) {
                _errorMessage.value = ("Password is too weak.")
            } catch (e: Exception) {
                _errorMessage.value = (e.localizedMessage ?: "Registration failed")
            }

            finally {
                _isLoading.value = false
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
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }



}