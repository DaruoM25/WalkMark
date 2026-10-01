package com.walkmark.app.domain.monetization

import com.walkmark.app.domain.promo.JuryPromoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class JuryPromoSubscriptionManager(
    private val base: SubscriptionManager,
    private val promoManager: JuryPromoManager,
    scope: CoroutineScope? = null
) : SubscriptionManager {

    private val _manualState = MutableStateFlow(
        if (promoManager.isJuryAccessActive.value) SubscriptionState.entitled() else base.subscriptionState.value
    )

    private val combinedFlow: StateFlow<SubscriptionState>? = scope?.let { coroutineScope ->
        combine(base.subscriptionState, promoManager.isJuryAccessActive) { baseState, promoActive ->
            if (promoActive) {
                SubscriptionState.entitled()
            } else {
                baseState
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = if (promoManager.isJuryAccessActive.value) SubscriptionState.entitled() else base.subscriptionState.value
        )
    }

    override val subscriptionState: StateFlow<SubscriptionState>
        get() = combinedFlow ?: _manualState

    override val offerings: StateFlow<OfferingsState> = base.offerings

    private fun updateState() {
        _manualState.value = if (promoManager.isJuryAccessActive.value) {
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
        val state = if (promoManager.isJuryAccessActive.value) {
            SubscriptionState.entitled()
        } else {
            baseState
        }
        _manualState.value = state
        return state
    }

    override suspend fun purchase(product: PaywallProduct): PurchaseResult = base.purchase(product)

    override suspend fun restorePurchases(): RestoreResult = base.restorePurchases()
}
