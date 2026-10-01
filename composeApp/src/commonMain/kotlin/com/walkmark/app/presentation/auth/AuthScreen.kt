package com.walkmark.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.auth.AuthSessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("auth_back_button")) {
                        Text("Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("auth_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val session = state.sessionState) {
                is AuthSessionState.Authenticated -> {
                    Text(
                        text = "Logged in as: ${session.user.email ?: session.user.id}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag("authenticated_user_label")
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.signOut() },
                        enabled = !state.isLoading,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Text("Log Out")
                    }
                }
                is AuthSessionState.Restoring -> {
                    CircularProgressIndicator(modifier = Modifier.testTag("auth_loading_indicator"))
                    Spacer(Modifier.height(8.dp))
                    Text("Restoring session...")
                }
                is AuthSessionState.Guest -> {
                    Text(
                        text = if (state.isRegisterMode) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )
                    Spacer(Modifier.height(16.dp))

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("auth_error_text")
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (state.successMessage != null) {
                        Text(
                            text = state.successMessage!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("auth_success_text")
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.testTag("auth_loading_indicator"))
                    } else {
                        Button(
                            onClick = { viewModel.submit() },
                            modifier = Modifier.fillMaxWidth().testTag("auth_submit_button")
                        ) {
                            Text(if (state.isRegisterMode) "Register" else "Sign In")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.toggleMode() },
                        modifier = Modifier.testTag("auth_toggle_mode_button")
                    ) {
                        Text(
                            if (state.isRegisterMode) "Already have an account? Sign In"
                            else "Need an account? Register"
                        )
                    }
                }
            }
        }
    }
}
