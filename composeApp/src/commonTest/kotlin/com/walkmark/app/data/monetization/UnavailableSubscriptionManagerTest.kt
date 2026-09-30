package com.walkmark.app.data.monetization

import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.PaywallProduct
import com.walkmark.app.domain.monetization.PurchaseResult
import com.walkmark.app.domain.monetization.RestoreResult
import com.walkmark.app.domain.monetization.SubscriptionStatus
import com.walkmark.app.domain.monetization.SubscriptionState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UnavailableSubscriptionManagerTest {

    private val sampleProduct = PaywallProduct(
        id = "sample-id",
        title = "WalkMark Pro",
        period = "year",
        priceFormatted = "19.99"
    )

    private fun manager() = UnavailableSubscriptionManager()

    @Test
    fun initialStateIsUnknownAndNotEntitled() {
        val state = manager().subscriptionState.value

        assertEquals(SubscriptionStatus.Unknown, state.status)
        assertFalse(state.isEntitled)
        assertFalse(state.isLoading)
    }

    @Test
    fun offeringsAreUnavailableWithHonestReason() {
        val offerings = manager().offerings.value

        assertIs<OfferingsState.Unavailable>(offerings)
        assertEquals(UnavailableSubscriptionManager.PROVIDER_NOT_CONFIGURED, offerings.message)
    }

    @Test
    fun purchaseNeverSucceeds() = runTest {
        val result = manager().purchase(sampleProduct)

        assertEquals(PurchaseResult.Unavailable, result)
        assertFalse(manager().subscriptionState.value.isEntitled)
    }

    @Test
    fun restoreNeverSucceeds() = runTest {
        val result = manager().restorePurchases()

        assertEquals(RestoreResult.Unavailable, result)
        assertFalse(manager().subscriptionState.value.isEntitled)
    }

    @Test
    fun startNeverFabricatesEntitlement() = runTest {
        val manager = manager()

        manager.start()

        assertFalse(manager.subscriptionState.value.isEntitled)
        assertEquals(SubscriptionStatus.Unknown, manager.subscriptionState.value.status)
    }

    @Test
    fun refreshNeverFabricatesEntitlement() = runTest {
        val manager = manager()

        val refreshed = manager.refresh()

        assertFalse(refreshed.isEntitled)
        assertEquals(SubscriptionStatus.Unknown, refreshed.status)
    }

    @Test
    fun repeatedCallsRemainUnavailable() = runTest {
        val manager = manager()

        repeat(5) {
            assertEquals(PurchaseResult.Unavailable, manager.purchase(sampleProduct))
            assertEquals(RestoreResult.Unavailable, manager.restorePurchases())
        }
        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun providerStatusDeclaresNotConfigured() {
        assertEquals("NOT_CONFIGURED", MonetizationProviderStatus.MONETIZATION_PROVIDER)
        assertFalse(MonetizationProviderStatus.REVENUECAT_SDK_INTEGRATED)
        assertFalse(MonetizationProviderStatus.REAL_PURCHASES_SUPPORTED)
        assertFalse(MonetizationProviderStatus.IS_PRODUCTION_READY)
    }

    @Test
    fun productionProviderNeverReportsALoadedOffering() = runTest {
        val manager = manager()

        manager.start()
        manager.refresh()

        assertFalse(manager.offerings.value is OfferingsState.Loaded)
    }
}
