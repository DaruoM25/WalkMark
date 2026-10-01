package com.walkmark.app.domain.monetization

import com.walkmark.app.domain.promo.JuryPromoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class JuryPromoSubscriptionManager(
    private val base: SubscriptionManager,
    private val promoManager: JuryPromoManager,
    scope: CoroutineScope? = null
) : SubscriptionManager {

    private val _subscriptionState = MutableStateFlow(base.subscriptionState.value)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    override val offerings: StateFlow<OfferingsState> = base.offerings

    init {
        if (scope != null) {
            scope.launch {
                combine(base.subscriptionState, promoManager.isJuryAccessActive) { baseState, promoActive ->
                    if (promoActive) {
                        SubscriptionState.entitled()
                    } else {
                        baseState
                    }
                }.collect {
                    _subscriptionState.value = it
                }
            }
        } else {
            updateState()
        }
    }

    private fun updateState() {
        _subscriptionState.value = if (promoManager.isJuryAccessActive.value) {
            SubscriptionState.entitled()
        } else {
            base.subscriptionState.value
        }
    }

    override suspend fun start() {
        base.start()
        updateState()
    }

    override suspend fun refresh(): SubscriptionState {
        val baseState = base.refresh()
        return if (promoManager.isJuryAccessActive.value) {
            val state = SubscriptionState.entitled()
            _subscriptionState.value = state
            state
        } else {
            _subscriptionState.value = baseState
            baseState
        }
    }

    override suspend fun purchase(product: PaywallProduct): PurchaseResult = base.purchase(product)

    override suspend fun restorePurchases(): RestoreResult = base.restorePurchases()
}
