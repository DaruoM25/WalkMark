package com.walkmark.app.presentation.paywall

import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.PaywallProduct

data class HardPaywallUiState(
    val offerings: OfferingsState = OfferingsState.NotLoaded,
    val freeWalkCount: Int = 0,
    val freeWalkLimit: Int = 0
) {
    val showsStorePrices: Boolean = offerings is OfferingsState.Loaded

    val products: List<PaywallProduct> =
        (offerings as? OfferingsState.Loaded)?.offering?.products ?: emptyList()

    val purchasesAvailable: Boolean = showsStorePrices

    val unavailableMessage: String? = (offerings as? OfferingsState.Unavailable)?.message
}

internal fun HardPaywallUiState.hasEnabledPurchaseControl(): Boolean =
    purchasesAvailable && products.isNotEmpty()

internal fun HardPaywallUiState.hasEnabledRestoreControl(): Boolean = purchasesAvailable
