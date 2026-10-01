package com.walkmark.app.domain.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    fun observeSession(): StateFlow<AuthSessionState>

    suspend fun signUp(email: String, password: String): AuthOperationResult<AuthUser>

    suspend fun signIn(email: String, password: String): AuthOperationResult<AuthUser>

    suspend fun signOut(): AuthOperationResult<Unit>

    suspend fun restoreSession(): AuthOperationResult<AuthSessionState>
}
