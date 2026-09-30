package com.walkmark.app.domain.monetization

import kotlinx.coroutines.flow.StateFlow

interface SubscriptionManager {

    val subscriptionState: StateFlow<SubscriptionState>

    val offerings: StateFlow<OfferingsState>

    suspend fun start()

    suspend fun refresh(): SubscriptionState

    suspend fun purchase(product: PaywallProduct): PurchaseResult

    suspend fun restorePurchases(): RestoreResult
}

sealed interface PurchaseResult {
    data class Completed(val subscriptionState: SubscriptionState) : PurchaseResult

    data object Cancelled : PurchaseResult

    data object Pending : PurchaseResult

    data class Failed(val message: String) : PurchaseResult

    data object Unavailable : PurchaseResult
}

sealed interface RestoreResult {
    data class Restored(val subscriptionState: SubscriptionState) : RestoreResult

    data object NotRestored : RestoreResult

    data class Failed(val message: String) : RestoreResult

    data object Unavailable : RestoreResult
}
