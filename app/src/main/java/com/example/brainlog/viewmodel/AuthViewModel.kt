package com.example.brainlog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    fun register(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
            }
    }




}