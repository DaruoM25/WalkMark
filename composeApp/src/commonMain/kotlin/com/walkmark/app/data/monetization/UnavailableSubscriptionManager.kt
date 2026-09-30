package com.walkmark.app.data.monetization

import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.PaywallProduct
import com.walkmark.app.domain.monetization.PurchaseResult
import com.walkmark.app.domain.monetization.RestoreResult
import com.walkmark.app.domain.monetization.SubscriptionManager
import com.walkmark.app.domain.monetization.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnavailableSubscriptionManager : SubscriptionManager {

    private val _subscriptionState = MutableStateFlow(SubscriptionState.unknown(isLoading = false))
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _offerings = MutableStateFlow<OfferingsState>(
        OfferingsState.Unavailable(PROVIDER_NOT_CONFIGURED)
    )
    override val offerings: StateFlow<OfferingsState> = _offerings.asStateFlow()

    override suspend fun start() {
        _subscriptionState.value = SubscriptionState.unknown(isLoading = false)
    }

    override suspend fun refresh(): SubscriptionState = subscriptionState.value

    override suspend fun purchase(product: PaywallProduct): PurchaseResult = PurchaseResult.Unavailable

    override suspend fun restorePurchases(): RestoreResult = RestoreResult.Unavailable

    companion object {
        const val PROVIDER_NOT_CONFIGURED: String = "Provider not configured"
    }
}
