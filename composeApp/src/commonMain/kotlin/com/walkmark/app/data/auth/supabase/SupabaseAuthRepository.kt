package com.walkmark.app.data.auth.supabase

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SupabaseAuthRepository internal constructor(
    private val client: SupabaseAuthClient,
    observationScope: CoroutineScope,
) : AuthRepository {

    constructor(
        config: SupabaseAuthConfig,
        observationScope: CoroutineScope,
    ) : this(createSupabaseAuthClient(config), observationScope)

    private val sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Restoring)

    init {
        observationScope.launch {
            client.sessionStates.collect(sessionState::emit)
        }
    }

    override fun observeSession(): StateFlow<AuthSessionState> = sessionState.asStateFlow()

    override suspend fun signUp(
        email: String,
        password: String,
    ): AuthOperationResult<AuthUser> {
        if (email.isBlank() || password.isBlank()) return invalidInput()

        return when (val result = client.signUp(email, password)) {
            is AuthOperationResult.Failure -> result
            is AuthOperationResult.Success -> {
                sessionState.value = if (result.value.hasActiveSession) {
                    AuthSessionState.Authenticated(result.value.user)
                } else {
                    AuthSessionState.Guest
                }
                AuthOperationResult.Success(result.value.user)
            }
        }
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): AuthOperationResult<AuthUser> {
        if (email.isBlank() || password.isBlank()) return invalidInput()

        return when (val result = client.signIn(email, password)) {
            is AuthOperationResult.Failure -> result
            is AuthOperationResult.Success -> {
                sessionState.value = AuthSessionState.Authenticated(result.value)
                result
            }
        }
    }

    override suspend fun signOut(): AuthOperationResult<Unit> = when (val result = client.signOut()) {
        is AuthOperationResult.Failure -> result
        is AuthOperationResult.Success -> {
            sessionState.value = AuthSessionState.Guest
            result
        }
    }

    override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> {
        sessionState.value = AuthSessionState.Restoring
        return when (val result = client.restoreSession()) {
            is AuthOperationResult.Failure -> result
            is AuthOperationResult.Success -> {
                sessionState.value = result.value
                result
            }
        }
    }

    private fun <T> invalidInput(): AuthOperationResult<T> =
        AuthOperationResult.Failure(AuthFailure.InvalidInput)
}
