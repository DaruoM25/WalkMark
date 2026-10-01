package com.walkmark.app.presentation.auth

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val sessionState: AuthSessionState = AuthSessionState.Guest,
    val isRegisterMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            authRepository.observeSession().collect { session ->
                _uiState.value = _uiState.value.copy(
                    sessionState = session,
                    isLoading = false
                )
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = !_uiState.value.isRegisterMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submit() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        scope.launch {
            if (_uiState.value.isRegisterMode) {
                when (val result = authRepository.signUp(email, password)) {
                    is AuthOperationResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Registration successful"
                        )
                    }
                    is AuthOperationResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = mapError(result.reason)
                        )
                    }
                }
            } else {
                when (val result = authRepository.signIn(email, password)) {
                    is AuthOperationResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            email = "",
                            password = ""
                        )
                    }
                    is AuthOperationResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = mapError(result.reason)
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        scope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun mapError(failure: AuthFailure): String = when (failure) {
        AuthFailure.InvalidCredentials -> "Invalid email or password"
        AuthFailure.InvalidInput -> "Invalid email or password format"
        AuthFailure.AccountAlreadyExists -> "An account with this email already exists"
        AuthFailure.Network -> "Network error. Please check your connection"
        AuthFailure.Unavailable -> "Authentication service is temporarily unavailable"
        AuthFailure.Unknown -> "Authentication failed. Please try again"
    }
}
