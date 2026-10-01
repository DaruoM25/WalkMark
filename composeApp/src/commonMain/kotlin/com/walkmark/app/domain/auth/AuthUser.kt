package com.walkmark.app.domain.auth

data class AuthUser(
    val id: String,
    val email: String? = null,
)
