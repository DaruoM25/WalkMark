package com.walkmark.app.domain.promo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JuryPromoManagerTest {

    @Test
    fun initialStateIsInactive() {
        val manager = JuryPromoManager()
        assertFalse(manager.isJuryAccessActive.value)
        assertEquals(null, manager.validUntil.value)
    }

    @Test
    fun validCodeWithinWindowActivatesAccess() {
        val manager = JuryPromoManager()
        val result = manager.activate("WALKMARK-JURY-2026", date = "2026-10-01")

        assertIs<PromoActivationResult.Success>(result)
        assertEquals("2026-10-15", result.validUntil)
        assertTrue(manager.isJuryAccessActive.value)
        assertEquals("2026-10-15", manager.validUntil.value)
    }

    @Test
    fun codeIsCaseAndWhitespaceInsensitive() {
        val manager = JuryPromoManager()
        val result = manager.activate("  walkmark-jury-2026  ", date = "2026-10-10")

        assertIs<PromoActivationResult.Success>(result)
        assertTrue(manager.isJuryAccessActive.value)
    }

    @Test
    fun invalidCodeIsRejected() {
        val manager = JuryPromoManager()
        val result = manager.activate("INVALID-CODE", date = "2026-10-05")

        assertEquals(PromoActivationResult.InvalidCode, result)
        assertFalse(manager.isJuryAccessActive.value)
    }

    @Test
    fun expiredDateIsRejected() {
        val manager = JuryPromoManager()
        val result = manager.activate("WALKMARK-JURY-2026", date = "2026-10-16")

        assertEquals(PromoActivationResult.Expired, result)
        assertFalse(manager.isJuryAccessActive.value)
    }

    @Test
    fun deactivationResetsState() {
        val manager = JuryPromoManager()
        manager.activate("WALKMARK-JURY-2026", date = "2026-10-05")
        assertTrue(manager.isJuryAccessActive.value)

        manager.deactivate()
        assertFalse(manager.isJuryAccessActive.value)
        assertEquals(null, manager.validUntil.value)
    }
}
