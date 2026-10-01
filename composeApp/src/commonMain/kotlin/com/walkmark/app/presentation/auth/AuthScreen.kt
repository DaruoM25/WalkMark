package com.walkmark.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.presentation.components.AccessPresentation
import com.walkmark.app.presentation.components.AccessStatusCard

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBack: (() -> Unit)? = null,
    accessPresentation: AccessPresentation? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToSupport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Confirm password is presentation-only
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("auth_screen")
            .semantics { contentDescription = "Account Screen" }
    ) {
        when (val session = state.sessionState) {
            is AuthSessionState.Authenticated -> {
                // === Authenticated state ===

                // Profile / email card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = session.user.email ?: session.user.id,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.testTag("authenticated_user_label")
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Plan / access card
                if (accessPresentation != null) {
                    AccessStatusCard(
                        access = accessPresentation,
                        modifier = Modifier.testTag("account_access_status")
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Settings
                if (onNavigateToSettings != null) {
                    AccountActionCard(
                        title = "Settings",
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_entry_button")
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Help & Support
                if (onNavigateToSupport != null) {
                    AccountActionCard(
                        title = "Help & Support",
                        icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Help") },
                        onClick = onNavigateToSupport,
                        modifier = Modifier.testTag("support_entry_button")
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Logout
                Button(
                    onClick = { viewModel.signOut() },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button")
                        .semantics { contentDescription = "Log Out" },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }
            }

            is AuthSessionState.Restoring -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("auth_loading_indicator")
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Restoring session…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.weight(1f))
            }

            is AuthSessionState.Guest -> {
                // === Guest state: Sign In / Register form card ===

                // Access card at top for guest too
                if (accessPresentation != null) {
                    AccessStatusCard(
                        access = accessPresentation,
                        compact = true,
                        modifier = Modifier.testTag("guest_access_status")
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.isRegisterMode) "Create Account" else "Sign in to WalkMark",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChanged,
                            label = { Text("Email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChanged,
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        // Confirm password in register mode
                        if (state.isRegisterMode) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    confirmPasswordError = null
                                },
                                label = { Text("Confirm Password") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = confirmPasswordError != null,
                                supportingText = confirmPasswordError?.let { err ->
                                    { Text(err, color = MaterialTheme.colorScheme.error) }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input")
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Error banner
                        if (state.errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("auth_error_text")
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Success message
                        if (state.successMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.successMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("auth_success_text")
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.testTag("auth_loading_indicator")
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (state.isRegisterMode) {
                                        // Validate confirm password before submitting
                                        if (state.password != confirmPassword) {
                                            confirmPasswordError = "Passwords do not match."
                                            return@Button
                                        }
                                    }
                                    viewModel.submit()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_submit_button"),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    if (state.isRegisterMode) "Register" else "Sign In",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                confirmPassword = ""
                                confirmPasswordError = null
                                viewModel.toggleMode()
                            },
                            modifier = Modifier.testTag("auth_toggle_mode_button")
                        ) {
                            Text(
                                if (state.isRegisterMode) "Already have an account? Sign In"
                                else "Need an account? Register"
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Settings & Support for guest users too
                if (onNavigateToSettings != null) {
                    AccountActionCard(
                        title = "Settings",
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("guest_settings_button")
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (onNavigateToSupport != null) {
                    AccountActionCard(
                        title = "Help & Support",
                        icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Help") },
                        onClick = onNavigateToSupport,
                        modifier = Modifier.testTag("guest_support_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountActionCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.padding(start = 12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
