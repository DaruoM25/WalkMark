package com.walkmark.app.domain.auth

sealed interface AuthSessionState {
    data object Restoring : AuthSessionState
    data object Guest : AuthSessionState
    data class Authenticated(val user: AuthUser) : AuthSessionState
}
