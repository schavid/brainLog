package com.example.brainlog.view


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brainlog.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    var showUsernameField by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Fehlermeldung anzeigen (unverändert)
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
                authViewModel.clearErrorMessage()
            }
        }
    }

    // nach erfolgreichem login wegnavigieren (unverändert)
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Haupt-Column für das Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login or Register", fontSize = 23.sp)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage?.contains("email", ignoreCase = true) == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = errorMessage?.contains("password", ignoreCase = true) == true
            )
            Spacer(modifier = Modifier.height(8.dp))


            AnimatedVisibility(visible = showUsernameField) {
                // Nur das Textfeld hier rein!
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage?.contains("username", ignoreCase = true) == true
                )
            }
            // --- Ende Benutzername-Feld ---

            // Spacer vor den Buttons/Indicator
            Spacer(modifier = Modifier.height(16.dp))



            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Login Button
                    Button(
                        onClick = {
                            showUsernameField = false // Verstecke Username-Feld beim Login-Versuch
                            authViewModel.login(email.trim(), password)
                        },
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Login")
                    }

                    // Register Button
                    Button(
                        onClick = {
                            if (showUsernameField) {
                                authViewModel.register(
                                    email.trim(), password, username.trim()
                                )
                            } else {
                                showUsernameField = true
                                authViewModel.clearErrorMessage()
                            }
                        },
                        enabled = true
                    ) {
                        Text(if (showUsernameField) "Register Now" else "Register")
                    }
                }
            }
        }
    }
}
