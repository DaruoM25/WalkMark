package com.walkmark.app.domain.monetization

import com.walkmark.app.data.monetization.FakeSubscriptionManager
import com.walkmark.app.domain.promo.JuryPromoManager
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JuryPromoSubscriptionManagerTest {

    @Test
    fun baseFreeStateYieldsFreeStateWithoutPromo() = runTest {
        val base = FakeSubscriptionManager(SubscriptionState.free())
        val promo = JuryPromoManager()
        val manager = JuryPromoSubscriptionManager(base, promo, backgroundScope)

        assertEquals(SubscriptionStatus.Free, manager.subscriptionState.value.status)
        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun activePromoYieldsEntitledStateEvenIfBaseIsFree() = runTest {
        val base = FakeSubscriptionManager(SubscriptionState.free())
        val promo = JuryPromoManager()
        val manager = JuryPromoSubscriptionManager(base, promo, backgroundScope)

        promo.activate("WALKMARK-JURY-2026", date = "2026-10-05")
        testScheduler.advanceUntilIdle()

        assertEquals(SubscriptionStatus.Entitled, manager.subscriptionState.value.status)
        assertTrue(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun baseEntitledStateYieldsEntitledStateWithoutPromo() = runTest {
        val base = FakeSubscriptionManager(SubscriptionState.entitled())
        val promo = JuryPromoManager()
        val manager = JuryPromoSubscriptionManager(base, promo, backgroundScope)

        assertEquals(SubscriptionStatus.Entitled, manager.subscriptionState.value.status)
        assertTrue(manager.subscriptionState.value.isEntitled)
    }
}
