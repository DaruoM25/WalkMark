package com.walkmark.app.presentation.paywall

import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.Offering
import com.walkmark.app.domain.monetization.PaywallProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HardPaywallUiStateTest {

    private val weekly = PaywallProduct(
        id = "weekly",
        title = "WalkMark Pro Weekly",
        period = "week",
        priceFormatted = "2.99"
    )

    private val annual = PaywallProduct(
        id = "annual",
        title = "WalkMark Pro Annual",
        period = "year",
        priceFormatted = "19.99"
    )

    @Test
    fun unavailableOfferingsDisplayNoStorePrices() {
        val state = HardPaywallUiState(
            offerings = OfferingsState.Unavailable("Provider not configured"),
            freeWalkCount = 3,
            freeWalkLimit = 3
        )

        assertFalse(state.showsStorePrices)
        assertTrue(state.products.isEmpty())
        assertFalse(state.purchasesAvailable)
        assertNull(state.products.firstOrNull())
    }

    @Test
    fun unavailableOfferingsNeverExposeAnEnabledPurchaseControl() {
        val state = HardPaywallUiState(
            offerings = OfferingsState.Unavailable("Provider not configured")
        )

        assertFalse(state.hasEnabledPurchaseControl())
        assertFalse(state.hasEnabledRestoreControl())
    }

    @Test
    fun notLoadedOfferingsDisplayNoStorePrices() {
        val state = HardPaywallUiState(offerings = OfferingsState.NotLoaded)

        assertFalse(state.showsStorePrices)
        assertTrue(state.products.isEmpty())
        assertFalse(state.hasEnabledPurchaseControl())
    }

    @Test
    fun unavailableReasonIsSurfacedForDisplay() {
        val state = HardPaywallUiState(
            offerings = OfferingsState.Unavailable("Provider not configured")
        )

        assertEquals("Provider not configured", state.unavailableMessage)
    }

    @Test
    fun loadedOfferingsExposeProviderSuppliedPricesOnly() {
        val state = HardPaywallUiState(
            offerings = OfferingsState.Loaded(
                Offering(id = "default", products = listOf(weekly, annual))
            )
        )

        assertTrue(state.showsStorePrices)
        assertEquals(listOf("2.99", "19.99"), state.products.map { it.priceFormatted })
        assertTrue(state.hasEnabledPurchaseControl())
    }

    @Test
    fun phase1UnavailableStateExposesNoRecommendedBadge() {
        val state = HardPaywallUiState(
            offerings = OfferingsState.Unavailable("Provider not configured")
        )

        assertTrue(state.products.none { it.isRecommended })
    }
}
