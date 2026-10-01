package com.walkmark.app.domain.auth

sealed interface AuthOperationResult<out T> {
    data class Success<T>(val value: T) : AuthOperationResult<T>
    data class Failure(val reason: AuthFailure) : AuthOperationResult<Nothing>
}

sealed interface AuthFailure {
    data object InvalidCredentials : AuthFailure
    data object InvalidInput : AuthFailure
    data object AccountAlreadyExists : AuthFailure
    data object Network : AuthFailure
    data object Unavailable : AuthFailure
    data object Unknown : AuthFailure
}
