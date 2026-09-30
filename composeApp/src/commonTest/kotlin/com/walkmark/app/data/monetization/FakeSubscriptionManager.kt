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

class FakeSubscriptionManager(
    initialState: SubscriptionState = SubscriptionState.free(),
    initialOfferings: OfferingsState = OfferingsState.Unavailable("Provider not configured")
) : SubscriptionManager {

    private val _subscriptionState = MutableStateFlow(initialState)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _offerings = MutableStateFlow<OfferingsState>(initialOfferings)
    override val offerings: StateFlow<OfferingsState> = _offerings.asStateFlow()

    val purchaseCalls = mutableListOf<PaywallProduct>()
    var restoreCallCount: Int = 0
        private set

    var purchaseResult: PurchaseResult = PurchaseResult.Unavailable
    var restoreResult: RestoreResult = RestoreResult.Unavailable

    override suspend fun start() {
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
    }

    override suspend fun refresh(): SubscriptionState = _subscriptionState.value

    override suspend fun purchase(product: PaywallProduct): PurchaseResult {
        purchaseCalls += product
        return when (val result = purchaseResult) {
            is PurchaseResult.Completed -> {
                _subscriptionState.value = result.subscriptionState
                result
            }

            else -> result
        }
    }

    override suspend fun restorePurchases(): RestoreResult {
        restoreCallCount++
        return when (val result = restoreResult) {
            is RestoreResult.Restored -> {
                _subscriptionState.value = result.subscriptionState
                result
            }

            else -> result
        }
    }

    fun setSubscriptionState(state: SubscriptionState) {
        _subscriptionState.value = state
    }

    fun setOfferings(state: OfferingsState) {
        _offerings.value = state
    }
}
