package com.walkmark.app.presentation.support

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportModelsTest {
    @Test
    fun emailConfigurationAcceptsSimpleProductionAddress() {
        val config = SupportContactConfig.Email("support@example.com")
        assertEquals("support@example.com", config.address)
        assertTrue(isValidSupportEmail(config.address))
    }

    @Test
    fun emailConfigurationRejectsMalformedValues() {
        listOf("", "support", "@example.com", "support@", "support @example.com", "a@b@c")
            .forEach { address ->
                assertFalse(isValidSupportEmail(address))
                assertFailsWith<IllegalArgumentException> { SupportContactConfig.Email(address) }
            }
    }

    @Test
    fun faqCatalogContainsOnlyApprovedTopics() {
        assertEquals(
            listOf("getting_started", "start_stop", "location_permission", "gps_tracking", "privacy", "contact"),
            supportFaqItems.map { it.id }
        )
    }
}
