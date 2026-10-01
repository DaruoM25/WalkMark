package com.walkmark.app.data.auth

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnavailableAuthRepository : AuthRepository {
    private val _session = MutableStateFlow<AuthSessionState>(AuthSessionState.Guest)
    override fun observeSession(): StateFlow<AuthSessionState> = _session.asStateFlow()

    override suspend fun signUp(email: String, password: String): AuthOperationResult<AuthUser> =
        AuthOperationResult.Failure(AuthFailure.Unavailable)

    override suspend fun signIn(email: String, password: String): AuthOperationResult<AuthUser> =
        AuthOperationResult.Failure(AuthFailure.Unavailable)

    override suspend fun signOut(): AuthOperationResult<Unit> {
        _session.value = AuthSessionState.Guest
        return AuthOperationResult.Success(Unit)
    }

    override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> {
        _session.value = AuthSessionState.Guest
        return AuthOperationResult.Success(AuthSessionState.Guest)
    }
}
