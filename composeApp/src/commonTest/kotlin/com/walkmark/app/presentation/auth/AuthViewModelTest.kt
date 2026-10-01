package com.walkmark.app.presentation.auth

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthViewModelTest {

    private val fakeUser = AuthUser(id = "user-123", email = "walker@example.com")

    @Test
    fun initialStateObservesSession() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Guest)
        val vm = AuthViewModel(repo, backgroundScope)
        runCurrent()

        assertEquals(AuthSessionState.Guest, vm.uiState.value.sessionState)
        assertFalse(vm.uiState.value.isRegisterMode)
    }

    @Test
    fun toggleModeSwitchesBetweenLoginAndRegister() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Guest)
        val vm = AuthViewModel(repo, backgroundScope)

        vm.toggleMode()
        assertTrue(vm.uiState.value.isRegisterMode)

        vm.toggleMode()
        assertFalse(vm.uiState.value.isRegisterMode)
    }

    @Test
    fun signInSuccessClearsInputAndSetsSession() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Guest)
        val vm = AuthViewModel(repo, backgroundScope)
        runCurrent()

        vm.onEmailChanged("walker@example.com")
        vm.onPasswordChanged("securePassword123")
        vm.submit()
        runCurrent()

        assertEquals(AuthSessionState.Authenticated(fakeUser), vm.uiState.value.sessionState)
        assertEquals("", vm.uiState.value.email)
        assertEquals("", vm.uiState.value.password)
    }

    @Test
    fun signInFailureShowsErrorMessage() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Guest)
        repo.signInResult = AuthOperationResult.Failure(AuthFailure.InvalidCredentials)
        val vm = AuthViewModel(repo, backgroundScope)
        runCurrent()

        vm.onEmailChanged("walker@example.com")
        vm.onPasswordChanged("wrong")
        vm.submit()
        runCurrent()

        assertEquals("Invalid email or password", vm.uiState.value.errorMessage)
    }

    @Test
    fun signUpSuccessShowsSuccessMessage() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Guest)
        val vm = AuthViewModel(repo, backgroundScope)
        runCurrent()

        vm.toggleMode()
        vm.onEmailChanged("new@example.com")
        vm.onPasswordChanged("password")
        vm.submit()
        runCurrent()

        assertEquals("Registration successful", vm.uiState.value.successMessage)
    }

    @Test
    fun signOutClearsSession() = runTest {
        val repo = FakeAuthRepository(AuthSessionState.Authenticated(fakeUser))
        val vm = AuthViewModel(repo, backgroundScope)
        runCurrent()

        vm.signOut()
        runCurrent()

        assertEquals(AuthSessionState.Guest, vm.uiState.value.sessionState)
    }

    private class FakeAuthRepository(
        initialState: AuthSessionState = AuthSessionState.Guest
    ) : AuthRepository {
        private val session = MutableStateFlow(initialState)
        var signInResult: AuthOperationResult<AuthUser> =
            AuthOperationResult.Success(AuthUser("user-123", "walker@example.com"))
        var signUpResult: AuthOperationResult<AuthUser> =
            AuthOperationResult.Success(AuthUser("user-123", "new@example.com"))

        override fun observeSession(): StateFlow<AuthSessionState> = session.asStateFlow()

        override suspend fun signUp(email: String, password: String): AuthOperationResult<AuthUser> {
            val result = signUpResult
            if (result is AuthOperationResult.Success) {
                session.value = AuthSessionState.Guest
            }
            return result
        }

        override suspend fun signIn(email: String, password: String): AuthOperationResult<AuthUser> {
            val result = signInResult
            if (result is AuthOperationResult.Success) {
                session.value = AuthSessionState.Authenticated(result.value)
            }
            return result
        }

        override suspend fun signOut(): AuthOperationResult<Unit> {
            session.value = AuthSessionState.Guest
            return AuthOperationResult.Success(Unit)
        }

        override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> {
            return AuthOperationResult.Success(session.value)
        }
    }
}
