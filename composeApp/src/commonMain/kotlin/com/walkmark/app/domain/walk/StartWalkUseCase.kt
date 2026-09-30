package com.walkmark.app.domain.walk

import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.monetization.PaywallReason
import com.walkmark.app.domain.monetization.PersistedWalkCount
import com.walkmark.app.domain.monetization.SubscriptionManager
import com.walkmark.app.domain.monetization.WalkCreationAccess
import com.walkmark.app.domain.monetization.WalkCreationAccessPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface WalkStartResult {
    data object Started : WalkStartResult

    data class RequiresSubscription(val reason: PaywallReason) : WalkStartResult
}

class StartWalkUseCase(
    private val subscriptionManager: SubscriptionManager,
    private val walkCount: PersistedWalkCount,
    private val locationRepository: LocationRepository,
    private val policy: WalkCreationAccessPolicy = WalkCreationAccessPolicy(),
    scope: CoroutineScope
) {

    val freeWalkCount: StateFlow<Int> = walkCount.walkCount.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = 0
    )

    val access: StateFlow<WalkCreationAccess> = combine(
        subscriptionManager.subscriptionState,
        freeWalkCount
    ) { subscriptionState, count ->
        policy.evaluate(subscriptionState, count)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = policy.evaluate(subscriptionManager.subscriptionState.value, 0)
    )

    suspend fun startWalk(): WalkStartResult {
        val decision = policy.evaluate(
            subscriptionManager.subscriptionState.value,
            walkCount.currentWalkCount()
        )
        return when (decision) {
            WalkCreationAccess.Allowed -> {
                locationRepository.startTracking()
                WalkStartResult.Started
            }

            is WalkCreationAccess.RequiresSubscription -> WalkStartResult.RequiresSubscription(decision.reason)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
