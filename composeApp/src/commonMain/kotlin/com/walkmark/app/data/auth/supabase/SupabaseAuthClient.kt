package com.walkmark.app.data.auth.supabase

import com.walkmark.app.domain.auth.AuthFailure
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.User
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal data class SupabaseSignUpResult(
    val user: AuthUser,
    val hasActiveSession: Boolean,
)

internal interface SupabaseAuthClient {
    val sessionStates: Flow<AuthSessionState>

    suspend fun signUp(email: String, password: String): AuthOperationResult<SupabaseSignUpResult>
    suspend fun signIn(email: String, password: String): AuthOperationResult<AuthUser>
    suspend fun signOut(): AuthOperationResult<Unit>
    suspend fun restoreSession(): AuthOperationResult<AuthSessionState>
}

internal class DefaultSupabaseAuthClient(
    private val client: SupabaseClient,
) : SupabaseAuthClient {

    private val auth: Auth
        get() = client.auth

    override val sessionStates: Flow<AuthSessionState> = auth.sessionStatus.map(::mapSession)

    override suspend fun signUp(
        email: String,
        password: String,
    ): AuthOperationResult<SupabaseSignUpResult> = providerCall {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = auth.currentUserOrNull()
            ?: return@providerCall AuthOperationResult.Failure(AuthFailure.Unknown)
        AuthOperationResult.Success(
            SupabaseSignUpResult(
                user = user.toDomainUser(),
                hasActiveSession = auth.currentSessionOrNull() != null,
            )
        )
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): AuthOperationResult<AuthUser> = providerCall {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = auth.currentUserOrNull()
            ?: return@providerCall AuthOperationResult.Failure(AuthFailure.Unknown)
        AuthOperationResult.Success(user.toDomainUser())
    }

    override suspend fun signOut(): AuthOperationResult<Unit> = providerCall {
        auth.signOut()
        AuthOperationResult.Success(Unit)
    }

    override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> = providerCall {
        auth.awaitInitialization()
        AuthOperationResult.Success(mapSession(auth.sessionStatus.value))
    }

    private suspend fun <T> providerCall(
        operation: suspend () -> AuthOperationResult<T>,
    ): AuthOperationResult<T> = try {
        operation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AuthOperationResult.Failure(SupabaseAuthErrorMapper.map(throwable))
    }

    private fun mapSession(status: SessionStatus): AuthSessionState = when (status) {
        SessionStatus.Initializing -> AuthSessionState.Restoring
        is SessionStatus.NotAuthenticated -> AuthSessionState.Guest
        is SessionStatus.RefreshFailure -> AuthSessionState.Guest
        is SessionStatus.Authenticated -> status.session.user
            ?.toDomainUser()
            ?.let(AuthSessionState::Authenticated)
            ?: AuthSessionState.Guest
    }

    private fun User.toDomainUser(): AuthUser = mapSupabaseUser(id = id, email = email)
}

internal fun mapSupabaseUser(id: String, email: String?): AuthUser = AuthUser(
    id = id,
    email = email,
)

internal class UnavailableSupabaseAuthClient : SupabaseAuthClient {
    override val sessionStates: Flow<AuthSessionState> =
        MutableStateFlow(AuthSessionState.Guest)

    override suspend fun signUp(
        email: String,
        password: String,
    ): AuthOperationResult<SupabaseSignUpResult> = unavailable()

    override suspend fun signIn(
        email: String,
        password: String,
    ): AuthOperationResult<AuthUser> = unavailable()

    override suspend fun signOut(): AuthOperationResult<Unit> = unavailable()

    override suspend fun restoreSession(): AuthOperationResult<AuthSessionState> = unavailable()

    private fun <T> unavailable(): AuthOperationResult<T> =
        AuthOperationResult.Failure(AuthFailure.Unavailable)
}

internal fun createSupabaseAuthClient(config: SupabaseAuthConfig): SupabaseAuthClient {
    if (!config.isValid) return UnavailableSupabaseAuthClient()

    val client = createSupabaseClient(
        supabaseUrl = config.projectUrl,
        supabaseKey = config.publishableKey,
    ) {
        install(Auth)
    }
    return DefaultSupabaseAuthClient(client)
}
