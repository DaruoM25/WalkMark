package com.walkmark.app.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthContractsTest {

    @Test
    fun guestIsAStableSessionState() {
        val state: AuthSessionState = AuthSessionState.Guest
        assertEquals(AuthSessionState.Guest, state)
    }

    @Test
    fun restoringRepresentsSessionRecovery() {
        val state: AuthSessionState = AuthSessionState.Restoring
        assertEquals(AuthSessionState.Restoring, state)
    }

    @Test
    fun authenticatedContainsOnlyTheDomainUser() {
        val user = AuthUser(id = "opaque-provider-id", email = "walker@example.com")
        val state = AuthSessionState.Authenticated(user)
        assertEquals(user, state.user)
    }

    @Test
    fun authUserSupportsTheSafeMinimumIdentity() {
        val user = AuthUser(id = "opaque-provider-id")
        assertEquals("opaque-provider-id", user.id)
        assertNull(user.email)
    }

    @Test
    fun successCarriesOnlyTheTypedOperationValue() {
        val user = AuthUser(id = "user-id")
        val result: AuthOperationResult<AuthUser> = AuthOperationResult.Success(user)
        assertIs<AuthOperationResult.Success<AuthUser>>(result)
        assertEquals(user, result.value)
    }

    @Test
    fun failureCarriesOnlyAProviderNeutralReason() {
        val result: AuthOperationResult<AuthUser> =
            AuthOperationResult.Failure(AuthFailure.InvalidCredentials)
        assertIs<AuthOperationResult.Failure>(result)
        assertEquals(AuthFailure.InvalidCredentials, result.reason)
    }

    @Test
    fun everyApprovedFailureCategoryIsRepresentable() {
        val failures = listOf(
            AuthFailure.InvalidCredentials,
            AuthFailure.InvalidInput,
            AuthFailure.AccountAlreadyExists,
            AuthFailure.Network,
            AuthFailure.Unavailable,
            AuthFailure.Unknown,
        )

        assertEquals(6, failures.distinct().size)
        failures.forEach { failure ->
            assertEquals(failure, AuthOperationResult.Failure(failure).reason)
        }
    }
}
