package com.walkmark.app.domain.walk

import com.walkmark.app.data.monetization.FakeSubscriptionManager
import com.walkmark.app.domain.monetization.PaywallReason
import com.walkmark.app.domain.monetization.SubscriptionState
import com.walkmark.app.domain.monetization.WalkCreationAccess
import com.walkmark.app.testing.CountingLocationRepository
import com.walkmark.app.testing.StubPersistedWalkCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class StartWalkUseCaseTest {

    private fun TestScope.useCase(
        subscriptionState: SubscriptionState = SubscriptionState.free(),
        location: CountingLocationRepository = CountingLocationRepository(),
        countStub: StubPersistedWalkCount
    ): StartWalkUseCase = StartWalkUseCase(
        subscriptionManager = FakeSubscriptionManager(subscriptionState),
        walkCount = countStub,
        locationRepository = location,
        scope = backgroundScope
    )

    @Test
    fun allowBelowLimitStartsTracking() = runTest {
        val location = CountingLocationRepository()
        val useCase = useCase(location = location, countStub = StubPersistedWalkCount(0))

        val result = useCase.startWalk()

        assertEquals(WalkStartResult.Started, result)
        assertEquals(1, location.startTrackingCallCount)
    }

    @Test
    fun allowAtSecondFreeWalkStartsTracking() = runTest {
        val location = CountingLocationRepository()
        val useCase = useCase(location = location, countStub = StubPersistedWalkCount(2))

        assertEquals(WalkStartResult.Started, useCase.startWalk())
        assertEquals(1, location.startTrackingCallCount)
    }

    @Test
    fun denyAtFourthWalkIsSideEffectFree() = runTest {
        val location = CountingLocationRepository()
        val useCase = useCase(location = location, countStub = StubPersistedWalkCount(3))

        val result = useCase.startWalk()

        val denied = assertIs<WalkStartResult.RequiresSubscription>(result)
        assertEquals(PaywallReason.FreeWalkQuotaReached, denied.reason)
        assertEquals(0, location.startTrackingCallCount)
    }

    @Test
    fun entitledUserBypassesQuota() = runTest {
        val location = CountingLocationRepository()
        val useCase = useCase(
            subscriptionState = SubscriptionState.entitled(),
            location = location,
            countStub = StubPersistedWalkCount(99)
        )

        assertEquals(WalkStartResult.Started, useCase.startWalk())
        assertEquals(1, location.startTrackingCallCount)
    }

    @Test
    fun staleRetainedStateCannotGrantAWalk() = runTest {
        val location = CountingLocationRepository()
        val countStub = StubPersistedWalkCount(0)
        val useCase = useCase(location = location, countStub = countStub)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase.access.collect { }
        }
        runCurrent()

        assertEquals(WalkCreationAccess.Allowed, useCase.access.value)
        assertEquals(0, useCase.freeWalkCount.value)

        countStub.setWalkCount(3)

        val result = useCase.startWalk()

        assertIs<WalkStartResult.RequiresSubscription>(result)
        assertEquals(0, location.startTrackingCallCount)
    }

    @Test
    fun everyStartRereadsTheCountSource() = runTest {
        val location = CountingLocationRepository()
        val countStub = StubPersistedWalkCount(0)
        val useCase = useCase(location = location, countStub = countStub)

        assertEquals(WalkStartResult.Started, useCase.startWalk())
        countStub.setWalkCount(3)
        assertIs<WalkStartResult.RequiresSubscription>(useCase.startWalk())
        countStub.setWalkCount(3)
        assertIs<WalkStartResult.RequiresSubscription>(useCase.startWalk())

        assertEquals(1, location.startTrackingCallCount)
    }

    @Test
    fun accessStateFollowsTheCountSource() = runTest {
        val countStub = StubPersistedWalkCount(0)
        val useCase = useCase(countStub = countStub)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase.access.collect { }
        }
        runCurrent()

        assertEquals(WalkCreationAccess.Allowed, useCase.access.value)

        countStub.setWalkCount(3)
        runCurrent()

        val denied = assertIs<WalkCreationAccess.RequiresSubscription>(useCase.access.value)
        assertEquals(PaywallReason.FreeWalkQuotaReached, denied.reason)
    }

    @Test
    fun freeWalkCountStateTracksTheCountSource() = runTest {
        val countStub = StubPersistedWalkCount(0)
        val useCase = useCase(countStub = countStub)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase.freeWalkCount.collect { }
        }
        runCurrent()

        assertEquals(0, useCase.freeWalkCount.value)

        countStub.setWalkCount(2)
        runCurrent()

        assertEquals(2, useCase.freeWalkCount.value)
    }
}
