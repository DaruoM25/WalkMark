package com.walkmark.app.domain.monetization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WalkCreationAccessPolicyTest {

    private val policy = WalkCreationAccessPolicy()

    @Test
    fun freeWalkLimitIsThree() {
        assertEquals(3, WalkCreationAccessPolicy.FREE_WALK_LIMIT)
    }

    @Test
    fun entitledUserIsAllowedAtAnyWalkCount() {
        listOf(0, 2, 3, 99).forEach { count ->
            assertEquals(
                WalkCreationAccess.Allowed,
                policy.evaluate(SubscriptionState.entitled(), count),
                "entitled user must be allowed at count=$count"
            )
        }
    }

    @Test
    fun freeUserBelowLimitIsAllowed() {
        assertEquals(WalkCreationAccess.Allowed, policy.evaluate(SubscriptionState.free(), 0))
        assertEquals(WalkCreationAccess.Allowed, policy.evaluate(SubscriptionState.free(), 2))
    }

    @Test
    fun freeUserAtLimitRequiresSubscription() {
        val decision = policy.evaluate(SubscriptionState.free(), 3)

        assertIs<WalkCreationAccess.RequiresSubscription>(decision)
        assertEquals(PaywallReason.FreeWalkQuotaReached, decision.reason)
    }

    @Test
    fun freeUserAboveLimitRequiresSubscription() {
        val decision = policy.evaluate(SubscriptionState.free(), 4)

        assertIs<WalkCreationAccess.RequiresSubscription>(decision)
        assertEquals(PaywallReason.FreeWalkQuotaReached, decision.reason)
    }

    @Test
    fun unknownSubscriptionIsTreatedAsNotEntitled() {
        assertEquals(
            WalkCreationAccess.Allowed,
            policy.evaluate(SubscriptionState.unknown(isLoading = true), 0)
        )
        assertIs<WalkCreationAccess.RequiresSubscription>(
            policy.evaluate(SubscriptionState.unknown(isLoading = true), 3)
        )
    }

    @Test
    fun errorSubscriptionIsTreatedAsNotEntitled() {
        assertIs<WalkCreationAccess.RequiresSubscription>(
            policy.evaluate(SubscriptionState.error("offline"), 3)
        )
    }

    @Test
    fun customLimitIsHonoured() {
        val strictPolicy = WalkCreationAccessPolicy(freeWalkLimit = 1)

        assertEquals(WalkCreationAccess.Allowed, strictPolicy.evaluate(SubscriptionState.free(), 0))
        assertIs<WalkCreationAccess.RequiresSubscription>(
            strictPolicy.evaluate(SubscriptionState.free(), 1)
        )
        assertTrue(strictPolicy.freeWalkLimit == 1)
    }
}
