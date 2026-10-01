package com.walkmark.app.domain.monetization

import com.walkmark.app.domain.promo.JuryPromoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class JuryPromoSubscriptionManager(
    private val base: SubscriptionManager,
    private val promoManager: JuryPromoManager,
    scope: CoroutineScope? = null
) : SubscriptionManager {

    override val subscriptionState: StateFlow<SubscriptionState> = object : StateFlow<SubscriptionState> {
        override val replayCache: List<SubscriptionState>
            get() = listOf(value)

        override val value: SubscriptionState
            get() = if (promoManager.isJuryAccessActive.value) {
                SubscriptionState.entitled()
            } else {
                base.subscriptionState.value
            }

        override suspend fun collect(collector: FlowCollector<SubscriptionState>): Nothing {
            combine(base.subscriptionState, promoManager.isJuryAccessActive) { baseState, promoActive ->
                if (promoActive) SubscriptionState.entitled() else baseState
            }.collect(collector)
            throw IllegalStateException("StateFlow collector should not terminate")
        }
    }

    override val offerings: StateFlow<OfferingsState> = base.offerings

    override suspend fun start() {
        base.start()
    }

    override suspend fun refresh(): SubscriptionState {
        val baseState = base.refresh()
        return if (promoManager.isJuryAccessActive.value) {
            SubscriptionState.entitled()
        } else {
            baseState
        }
    }

    override suspend fun purchase(product: PaywallProduct): PurchaseResult = base.purchase(product)

    override suspend fun restorePurchases(): RestoreResult = base.restorePurchases()
}
