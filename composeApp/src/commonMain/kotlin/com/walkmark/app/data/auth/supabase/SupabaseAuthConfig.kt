package com.walkmark.app.data.auth.supabase

data class SupabaseAuthConfig(
    val projectUrl: String,
    val publishableKey: String,
) {
    internal val isValid: Boolean
        get() = projectUrl.startsWith("https://") &&
            publishableKey.isNotBlank() &&
            !publishableKey.startsWith("sb_secret_")
}
