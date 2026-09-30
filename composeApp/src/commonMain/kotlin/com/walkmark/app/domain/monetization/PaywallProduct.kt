package com.walkmark.app.domain.monetization

data class PaywallProduct(
    val id: String,
    val title: String,
    val period: String,
    val priceFormatted: String,
    val isRecommended: Boolean = false
)

data class Offering(
    val id: String,
    val products: List<PaywallProduct>
)

sealed interface OfferingsState {
    data object NotLoaded : OfferingsState

    data class Loaded(val offering: Offering) : OfferingsState

    data class Unavailable(val message: String? = null) : OfferingsState
}
