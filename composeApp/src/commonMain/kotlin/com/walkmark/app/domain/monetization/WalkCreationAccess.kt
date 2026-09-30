package com.walkmark.app.domain.monetization

sealed interface WalkCreationAccess {
    data object Allowed : WalkCreationAccess

    data class RequiresSubscription(val reason: PaywallReason) : WalkCreationAccess
}

enum class PaywallReason {
    FreeWalkQuotaReached
}
