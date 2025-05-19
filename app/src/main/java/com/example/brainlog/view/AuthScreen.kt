package com.example.brainlog.view


import android.app.Activity
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
import com.example.brainlog.viewmodel.AuthUiState
import com.example.brainlog.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.brainlog.R


@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    var showUsernameField by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    val currentErrorMessage = when (uiState) {
        is AuthUiState.Error -> (uiState as AuthUiState.Error).message
        else -> null
    }
    val isLoading = uiState is AuthUiState.Loading

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                result.data?.let { intent ->
                    // Das Bundle aus dem Intent holen
                    val bundle = intent.extras
                    if (bundle != null) {
                        // Jetzt das GoogleIdTokenCredential aus dem Bundle erstellen
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(bundle)
                        val googleIdToken = googleIdTokenCredential.idToken
                        authViewModel.signInWithGoogle(googleIdToken)
                    } else {
                        // Bundle war null, sollte bei Erfolg nicht passieren
                        authViewModel.resetStateToIdle()
                    }
                } ?: run {
                    // intent (result.data) war null
                    authViewModel.resetStateToIdle()
                }
            } catch (e: GoogleIdTokenParsingException) {
                authViewModel.resetStateToIdle()
            } catch (e: Exception) {
                authViewModel.resetStateToIdle()
            }
        } else {
            authViewModel.resetStateToIdle()
        }
    }




    // Seiteneffekte basierend auf Änderungen des uiState vom ViewModel behandeln


    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onAuthSuccess()
                authViewModel.resetStateToIdle()
            }

            is AuthUiState.Error -> { // Reaktion auf AuthUiState.Error vom ViewModel
                // Die Nachricht von AuthUiState.Error verwenden
                (uiState as AuthUiState.Error).message?.let { msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        // ViewModel-Methode aufrufen, um den Zustand zurückzusetzen
                        authViewModel.resetStateToIdle()
                    }
                }
            }

            AuthUiState.Idle -> {}
            AuthUiState.Loading -> {}
            AuthUiState.LoggedOut -> {}
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
                isError = currentErrorMessage?.contains("email", ignoreCase = true) == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = currentErrorMessage?.contains("password", ignoreCase = true) == true
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
                    isError = currentErrorMessage?.contains("username", ignoreCase = true) == true
                )
            }


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
                                if (uiState is AuthUiState.Error) {
                                    authViewModel.resetStateToIdle()
                                }
                            }
                        },
                        enabled = true
                    ) {
                        Text(if (showUsernameField) "Register Now" else "Register")
                    }
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        try {
                            val result = credentialManager.getCredential(context, request)
                            (result.credential as? GoogleIdTokenCredential)?.let {
                                authViewModel.signInWithGoogle(it.idToken)
                            } ?: authViewModel.resetStateToIdle()
                        } catch (e: GetCredentialException) {
                            // Der Launcher (googleSignInLauncher) wird bei Bedarf von getCredential ausgelöst.
                            // Dieser Catch-Block ist für Fehler, die nicht zum Starten eines Intents führen.
                            authViewModel.resetStateToIdle()
                        } catch (e: Exception) {
                            authViewModel.resetStateToIdle()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            ) {
                // Optional: Google Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo), // Erstelle ic_google_logo.xml
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign in with Google")
            }
        }
    }
}

