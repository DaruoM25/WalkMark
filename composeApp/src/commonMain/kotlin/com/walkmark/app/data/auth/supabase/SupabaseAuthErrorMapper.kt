package com.walkmark.app.data.auth.supabase

import com.walkmark.app.domain.auth.AuthFailure
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

internal object SupabaseAuthErrorMapper {

    fun map(throwable: Throwable): AuthFailure = when (throwable) {
        is AuthRestException -> mapAuthCode(throwable.error)
        is RestException -> mapAuthCode(throwable.error)
        is HttpRequestException -> AuthFailure.Network
        is IllegalArgumentException -> AuthFailure.InvalidInput
        else -> {
            val name = throwable::class.simpleName ?: ""
            if (name.contains("Timeout") || name.contains("Socket") || name.contains("Network") || name.contains("Connect")) {
                AuthFailure.Network
            } else {
                AuthFailure.Unknown
            }
        }
    }

    fun mapAuthCode(code: String): AuthFailure = when (code) {
        "invalid_credentials" -> AuthFailure.InvalidCredentials
        "validation_failed",
        "bad_json",
        "email_address_invalid",
        "weak_password",
        -> AuthFailure.InvalidInput
        "email_exists",
        "phone_exists",
        "user_already_exists",
        -> AuthFailure.AccountAlreadyExists
        "request_timeout" -> AuthFailure.Network
        "signup_disabled",
        "provider_disabled",
        "email_provider_disabled",
        -> AuthFailure.Unavailable
        else -> AuthFailure.Unknown
    }
}
