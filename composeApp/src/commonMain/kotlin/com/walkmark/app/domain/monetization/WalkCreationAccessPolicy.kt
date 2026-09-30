package com.walkmark.app.domain.monetization

class WalkCreationAccessPolicy(val freeWalkLimit: Int = FREE_WALK_LIMIT) {

    fun evaluate(
        subscriptionState: SubscriptionState,
        freeWalkCount: Int
    ): WalkCreationAccess = when {
        subscriptionState.isEntitled -> WalkCreationAccess.Allowed
        freeWalkCount < freeWalkLimit -> WalkCreationAccess.Allowed
        else -> WalkCreationAccess.RequiresSubscription(PaywallReason.FreeWalkQuotaReached)
    }

    companion object {
        const val FREE_WALK_LIMIT: Int = 3
    }
}
