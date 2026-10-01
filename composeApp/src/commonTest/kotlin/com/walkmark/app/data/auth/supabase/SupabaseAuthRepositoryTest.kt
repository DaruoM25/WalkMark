package com.walkmark.app.data.auth.supabase

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SupabaseAuthRepositoryTest {

    private val user = AuthUser("opaque-provider-id", "walker@example.com")

    @Test
    fun initialStateIsRestoring() = runTest {
        val repository = SupabaseAuthRepository(FakeSupabaseAuthClient(), backgroundScope)

        assertEquals(AuthSessionState.Restoring, repository.observeSession().value)
    }

    @Test
    fun restoreCanSettleOnGuest() = runTest {
        val client = FakeSupabaseAuthClient()
        client.restoreResult = AuthOperationResult.Success(AuthSessionState.Guest)
        val repository = SupabaseAuthRepository(client, backgroundScope)

        val result = repository.restoreSession()

        assertEquals(AuthOperationResult.Success(AuthSessionState.Guest), result)
        assertEquals(AuthSessionState.Guest, repository.observeSession().value)
    }

    @Test
    fun restoreCanSettleOnAuthenticated() = runTest {
        val client = FakeSupabaseAuthClient()
        client.restoreResult = AuthOperationResult.Success(AuthSessionState.Authenticated(user))
        val repository = SupabaseAuthRepository(client, backgroundScope)

        val result = repository.restoreSession()

        assertEquals(AuthOperationResult.Success(AuthSessionState.Authenticated(user)), result)
        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
    }

    @Test
    fun providerSessionChangesRemainObservable() = runTest {
        val client = FakeSupabaseAuthClient()
        val repository = SupabaseAuthRepository(client, backgroundScope)
        runCurrent()

        client.session.value = AuthSessionState.Authenticated(user)
        runCurrent()

        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
    }

    @Test
    fun signInSuccessPublishesAuthenticated() = runTest {
        val client = FakeSupabaseAuthClient()
        client.signInResult = AuthOperationResult.Success(user)
        val repository = SupabaseAuthRepository(client, backgroundScope)

        assertEquals(
            AuthOperationResult.Success(user),
            repository.signIn("walker@example.com", "password"),
        )
        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
    }

    @Test
    fun signInFailureDoesNotPublishAuthenticated() = runTest {
        val client = FakeSupabaseAuthClient()
        client.signInResult = AuthOperationResult.Failure(AuthFailure.InvalidCredentials)
        val repository = SupabaseAuthRepository(client, backgroundScope)

        val result = repository.signIn("walker@example.com", "wrong-password")

        assertEquals(AuthOperationResult.Failure(AuthFailure.InvalidCredentials), result)
        assertFalse(repository.observeSession().value is AuthSessionState.Authenticated)
    }

    @Test
    fun signUpWithActiveSessionPublishesAuthenticated() = runTest {
        val client = FakeSupabaseAuthClient()
        client.signUpResult = AuthOperationResult.Success(SupabaseSignUpResult(user, true))
        val repository = SupabaseAuthRepository(client, backgroundScope)

        assertEquals(
            AuthOperationResult.Success(user),
            repository.signUp("walker@example.com", "password"),
        )
        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
    }

    @Test
    fun signUpRequiringConfirmationRemainsGuest() = runTest {
        val client = FakeSupabaseAuthClient()
        client.signUpResult = AuthOperationResult.Success(SupabaseSignUpResult(user, false))
        val repository = SupabaseAuthRepository(client, backgroundScope)

        assertEquals(
            AuthOperationResult.Success(user),
            repository.signUp("walker@example.com", "password"),
        )
        assertEquals(AuthSessionState.Guest, repository.observeSession().value)
    }

    @Test
    fun signUpFailureIsReturnedWithoutAuthenticating() = runTest {
        val client = FakeSupabaseAuthClient()
        client.signUpResult = AuthOperationResult.Failure(AuthFailure.AccountAlreadyExists)
        val repository = SupabaseAuthRepository(client, backgroundScope)

        assertEquals(
            AuthOperationResult.Failure(AuthFailure.AccountAlreadyExists),
            repository.signUp("walker@example.com", "password"),
        )
        assertFalse(repository.observeSession().value is AuthSessionState.Authenticated)
    }

    @Test
    fun signOutSuccessPublishesGuest() = runTest {
        val client = FakeSupabaseAuthClient()
        client.session.value = AuthSessionState.Authenticated(user)
        client.signOutResult = AuthOperationResult.Success(Unit)
        val repository = SupabaseAuthRepository(client, backgroundScope)
        runCurrent()

        assertIs<AuthOperationResult.Success<Unit>>(repository.signOut())
        assertEquals(AuthSessionState.Guest, repository.observeSession().value)
    }

    @Test
    fun signOutFailureDoesNotFalselyPublishGuest() = runTest {
        val client = FakeSupabaseAuthClient()
        client.session.value = AuthSessionState.Authenticated(user)
        client.signOutResult = AuthOperationResult.Failure(AuthFailure.Network)
        val repository = SupabaseAuthRepository(client, backgroundScope)
        runCurrent()

        assertEquals(AuthOperationResult.Failure(AuthFailure.Network), repository.signOut())
        assertEquals(AuthSessionState.Authenticated(user), repository.observeSession().value)
    }

    @Test
    fun invalidInputIsRejectedWithoutCallingProvider() = runTest {
        val client = FakeSupabaseAuthClient()
        val repository = SupabaseAuthRepository(client, backgroundScope)

        assertEquals(
            AuthOperationResult.Failure(AuthFailure.InvalidInput),
            repository.signIn("", "password"),
        )
        assertFalse(client.signInCalled)
    }

    @Test
    fun invalidConfigurationIsUnavailableWithoutNetwork() = runTest {
        val repository = SupabaseAuthRepository(
            config = SupabaseAuthConfig(projectUrl = "", publishableKey = ""),
            observationScope = backgroundScope,
        )

        assertEquals(
            AuthOperationResult.Failure(AuthFailure.Unavailable),
            repository.signIn("walker@example.com", "password"),
        )
    }

    @Test
    fun secretProviderKeyIsRejected() {
        assertFalse(
            SupabaseAuthConfig(
                projectUrl = "https://project.supabase.co",
                publishableKey = "sb_secret_forbidden",
            ).isValid
        )
        assertTrue(
            SupabaseAuthConfig(
                projectUrl = "https://project.supabase.co",
                publishableKey = "sb_publishable_example",
            ).isValid
        )
    }

    private class FakeSupabaseAuthClient : SupabaseAuthClient {
        val session = MutableStateFlow<AuthSessionState>(AuthSessionState.Restoring)
        override val sessionStates: Flow<AuthSessionState> = session

        var signUpResult: AuthOperationResult<SupabaseSignUpResult> =
            AuthOperationResult.Failure(AuthFailure.Unknown)
        var signInResult: AuthOperationResult<AuthUser> =
            AuthOperationResult.Failure(AuthFailure.Unknown)
        var signOutResult: AuthOperationResult<Unit> =
            AuthOperationResult.Failure(AuthFailure.Unknown)
        var restoreResult: AuthOperationResult<AuthSessionState> =
            AuthOperationResult.Success(AuthSessionState.Guest)
        var signInCalled = false

        override suspend fun signUp(
            email: String,
            password: String,
        ): AuthOperationResult<SupabaseSignUpResult> = signUpResult

        override suspend fun signIn(
            email: String,
            password: String,
        ): AuthOperationResult<AuthUser> {
            signInCalled = true
            return signInResult
        }

        override suspend fun signOut(): AuthOperationResult<Unit> = signOutResult

        override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> = restoreResult
    }
}
