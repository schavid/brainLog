package com.example.brainlog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

class AuthViewModel:  ViewModel(){

    private val auth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _registrationResult = MutableLiveData<Result<FirebaseUser>>()
    val registrationResult: LiveData<Result<FirebaseUser>> = _registrationResult

    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(_authStateListener)
        _currentUser.value = auth.currentUser
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(_authStateListener)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val authResult: com.google.firebase.auth.AuthResult = auth.signInWithEmailAndPassword(email, password).await()
                val loggedInUser = authResult.user
                _loginResult.postValue(Result.success(loggedInUser!!))

            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Login failed")
                _loginResult.postValue(Result.failure(e))
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
                    // 3. Wenn Auth User erfolgreich erstellt wurde, speichere die Benutzerdaten in Firestore
                    val userDoc = UserDocument(
                        userId = firebaseUser.uid,
                        email = email,
                        username = username // Speichere den Benutzernamen wie eingegeben
                    )
                    saveUserData(firebaseUser.uid, userDoc)
                    _registrationResult.postValue(Result.success(firebaseUser))
                } else {
                    throw Exception("Failed to create user account.")
                }

            } catch (e: FirebaseAuthUserCollisionException) {
                _errorMessage.postValue("This email address is already in use.")
                _registrationResult.postValue(Result.failure(e))
            }  catch (e: FirebaseAuthWeakPasswordException) {
                _errorMessage.postValue("Password is too weak.")
                _registrationResult.postValue(Result.failure(e))
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Registration failed")
                _registrationResult.postValue(Result.failure(e))
                // HINWEIS: Wenn hier ein Fehler auftritt, NACHDEM der Auth User erstellt wurde,
                // haben wir einen Auth User ohne Firestore-Dokument. Das muss man ggf. behandeln.
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
            _errorMessage.postValue(e.localizedMessage ?: "Error saving user data")
            _registrationResult.postValue(Result.failure(e))
        }
    }

    fun logout() {
        auth.signOut()
    }

    // --- Hilfsfunktion zum Zurücksetzen der Fehlermeldung ---
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // --- Hilfsfunktion für User ID ---
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }




}