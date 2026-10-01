package com.walkmark.app.data.auth.supabase

import com.walkmark.app.domain.auth.AuthFailure
import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseAuthErrorMapperTest {

    @Test
    fun structuredProviderCodesMapToDomainFailures() {
        val expected = mapOf(
            "invalid_credentials" to AuthFailure.InvalidCredentials,
            "validation_failed" to AuthFailure.InvalidInput,
            "email_address_invalid" to AuthFailure.InvalidInput,
            "weak_password" to AuthFailure.InvalidInput,
            "email_exists" to AuthFailure.AccountAlreadyExists,
            "phone_exists" to AuthFailure.AccountAlreadyExists,
            "user_already_exists" to AuthFailure.AccountAlreadyExists,
            "request_timeout" to AuthFailure.Network,
            "signup_disabled" to AuthFailure.Unavailable,
            "provider_disabled" to AuthFailure.Unavailable,
        )

        expected.forEach { (code, failure) ->
            assertEquals(failure, SupabaseAuthErrorMapper.mapAuthCode(code))
        }
    }

    @Test
    fun unknownProviderCodeDoesNotExposeProviderDetails() {
        assertEquals(
            AuthFailure.Unknown,
            SupabaseAuthErrorMapper.mapAuthCode("provider_detail_not_in_domain"),
        )
    }

    @Test
    fun invalidLocalArgumentMapsToInvalidInput() {
        assertEquals(
            AuthFailure.InvalidInput,
            SupabaseAuthErrorMapper.map(IllegalArgumentException()),
        )
    }

    @Test
    fun providerUserMappingKeepsOnlySafeDomainFields() {
        assertEquals(
            expected = com.walkmark.app.domain.auth.AuthUser(
                id = "opaque-provider-id",
                email = "walker@example.com",
            ),
            actual = mapSupabaseUser(
                id = "opaque-provider-id",
                email = "walker@example.com",
            ),
        )
    }
}
