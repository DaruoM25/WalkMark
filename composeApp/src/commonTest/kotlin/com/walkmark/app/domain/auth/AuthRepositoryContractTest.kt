package com.walkmark.app.domain.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthRepositoryContractTest {

    private val user = AuthUser("opaque-provider-id", "walker@example.com")

    @Test
    fun signUpCanReturnSuccessAndTypedFailure() = runTest {
        val repository = ContractAuthRepository()
        repository.signUpResult = AuthOperationResult.Success(user)
        assertEquals(AuthOperationResult.Success(user), repository.signUp("walker@example.com", "secret"))

        repository.signUpResult = AuthOperationResult.Failure(AuthFailure.AccountAlreadyExists)
        assertEquals(
            AuthOperationResult.Failure(AuthFailure.AccountAlreadyExists),
            repository.signUp("walker@example.com", "secret"),
        )
    }

    @Test
    fun signInCanReturnSuccessAndTypedFailure() = runTest {
        val repository = ContractAuthRepository()
        repository.signInResult = AuthOperationResult.Success(user)
        assertEquals(AuthOperationResult.Success(user), repository.signIn("walker@example.com", "secret"))

        repository.signInResult = AuthOperationResult.Failure(AuthFailure.InvalidCredentials)
        assertEquals(
            AuthOperationResult.Failure(AuthFailure.InvalidCredentials),
            repository.signIn("walker@example.com", "secret"),
        )
    }

    @Test
    fun signOutCanReturnSuccessAndTypedFailure() = runTest {
        val repository = ContractAuthRepository()
        repository.signOutResult = AuthOperationResult.Success(Unit)
        assertIs<AuthOperationResult.Success<Unit>>(repository.signOut())

        repository.signOutResult = AuthOperationResult.Failure(AuthFailure.Network)
        assertEquals(AuthOperationResult.Failure(AuthFailure.Network), repository.signOut())
    }

    @Test
    fun restoreSessionRepresentsRestoringThenAuthenticated() = runTest {
        val repository = ContractAuthRepository()
        repository.restoreResult = AuthOperationResult.Success(AuthSessionState.Authenticated(user))
        assertEquals(AuthSessionState.Guest, repository.observeSession().value)

        val result = repository.restoreSession()

        assertEquals(AuthOperationResult.Success(AuthSessionState.Authenticated(user)), result)
        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
        assertEquals(
            listOf(AuthSessionState.Restoring, AuthSessionState.Authenticated(user)),
            repository.restoreTransitions,
        )
    }

    @Test
    fun restoreSessionMaySettleOnGuestWithoutError() = runTest {
        val repository = ContractAuthRepository()
        repository.restoreResult = AuthOperationResult.Success(AuthSessionState.Guest)

        val result = repository.restoreSession()

        assertEquals(AuthOperationResult.Success(AuthSessionState.Guest), result)
        assertEquals(AuthSessionState.Guest, repository.observeSession().value)
    }

    private class ContractAuthRepository : AuthRepository {
        private val session = MutableStateFlow<AuthSessionState>(AuthSessionState.Guest)

        var signUpResult: AuthOperationResult<AuthUser> = AuthOperationResult.Failure(AuthFailure.Unknown)
        var signInResult: AuthOperationResult<AuthUser> = AuthOperationResult.Failure(AuthFailure.Unknown)
        var signOutResult: AuthOperationResult<Unit> = AuthOperationResult.Failure(AuthFailure.Unknown)
        var restoreResult: AuthOperationResult<AuthSessionState> =
            AuthOperationResult.Success(AuthSessionState.Guest)
        val restoreTransitions = mutableListOf<AuthSessionState>()

        override fun observeSession(): StateFlow<AuthSessionState> = session

        override suspend fun signUp(email: String, password: String): AuthOperationResult<AuthUser> =
            signUpResult

        override suspend fun signIn(email: String, password: String): AuthOperationResult<AuthUser> =
            signInResult

        override suspend fun signOut(): AuthOperationResult<Unit> = signOutResult

        override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> {
            session.value = AuthSessionState.Restoring
            restoreTransitions += session.value
            val result = restoreResult
            if (result is AuthOperationResult.Success) {
                session.value = result.value
                restoreTransitions += session.value
            }
            return result
        }
    }
}
