package com.walkmark.app.data.monetization

import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.Offering
import com.walkmark.app.domain.monetization.PaywallProduct
import com.walkmark.app.domain.monetization.PurchaseResult
import com.walkmark.app.domain.monetization.RestoreResult
import com.walkmark.app.domain.monetization.SubscriptionState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FakeSubscriptionManagerTest {

    private val product = PaywallProduct(
        id = "fake-product",
        title = "WalkMark Pro",
        period = "year",
        priceFormatted = "19.99"
    )

    @Test
    fun freeStateIsNotEntitled() {
        val manager = FakeSubscriptionManager(SubscriptionState.free())

        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun entitledStateIsSimulable() {
        val manager = FakeSubscriptionManager(SubscriptionState.entitled())

        assertTrue(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun errorStateIsSimulable() {
        val manager = FakeSubscriptionManager(SubscriptionState.error("offline"))

        assertEquals("offline", manager.subscriptionState.value.errorMessage)
        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun purchaseSuccessIsSimulableForTestsOnly() = runTest {
        val manager = FakeSubscriptionManager(SubscriptionState.free())
        manager.purchaseResult = PurchaseResult.Completed(SubscriptionState.entitled())

        val result = manager.purchase(product)

        assertIs<PurchaseResult.Completed>(result)
        assertEquals(listOf(product), manager.purchaseCalls)
        assertTrue(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun restoreSuccessIsSimulableForTestsOnly() = runTest {
        val manager = FakeSubscriptionManager(SubscriptionState.free())
        manager.restoreResult = RestoreResult.Restored(SubscriptionState.entitled())

        val result = manager.restorePurchases()

        assertIs<RestoreResult.Restored>(result)
        assertEquals(1, manager.restoreCallCount)
        assertTrue(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun defaultResultsAreUnavailable() = runTest {
        val manager = FakeSubscriptionManager()

        assertEquals(PurchaseResult.Unavailable, manager.purchase(product))
        assertEquals(RestoreResult.Unavailable, manager.restorePurchases())
        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun offeringsAreFullySimulable() {
        val manager = FakeSubscriptionManager()

        manager.setOfferings(
            OfferingsState.Loaded(
                Offering(
                    id = "default",
                    products = listOf(product.copy(isRecommended = true))
                )
            )
        )

        val loaded = assertIs<OfferingsState.Loaded>(manager.offerings.value)
        assertEquals("19.99", loaded.offering.products.single().priceFormatted)
        assertTrue(loaded.offering.products.single().isRecommended)
    }

    @Test
    fun startClearsLoadingWithoutChangingEntitlement() = runTest {
        val manager = FakeSubscriptionManager(SubscriptionState.unknown(isLoading = true))

        manager.start()

        assertFalse(manager.subscriptionState.value.isLoading)
        assertFalse(manager.subscriptionState.value.isEntitled)
    }
}
