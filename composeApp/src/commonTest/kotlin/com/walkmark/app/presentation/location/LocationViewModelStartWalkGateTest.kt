package com.walkmark.app.presentation.location

import com.walkmark.app.data.monetization.FakeSubscriptionManager
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.monetization.SubscriptionState
import com.walkmark.app.domain.monetization.WalkCreationAccess
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.domain.walk.WalkStartResult
import com.walkmark.app.testing.CountingLocationRepository
import com.walkmark.app.testing.StubPersistedWalkCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelStartWalkGateTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(
        location: LocationRepository,
        walkCount: StubPersistedWalkCount,
        subscriptionState: SubscriptionState = SubscriptionState.free()
    ): LocationViewModel = LocationViewModel(
        locationRepository = location,
        startWalk = StartWalkUseCase(
            subscriptionManager = FakeSubscriptionManager(subscriptionState),
            walkCount = walkCount,
            locationRepository = location,
            scope = backgroundScope
        )
    )

    @Test
    fun fourthAttemptRequiresSubscriptionAndDoesNotTrack() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(3))

        viewModel.startTracking()
        advanceUntilIdle()

        val denied = assertIs<WalkStartResult.RequiresSubscription>(viewModel.startResult.value)
        assertEquals(
            com.walkmark.app.domain.monetization.PaywallReason.FreeWalkQuotaReached,
            denied.reason
        )
        assertEquals(0, location.startTrackingCallCount)
        assertFalse(viewModel.uiState.value.state is LocationTrackingState.Tracking)
    }

    @Test
    fun firstAttemptStartsTracking() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(0))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()

        viewModel.startTracking()
        advanceUntilIdle()

        assertEquals(WalkStartResult.Started, viewModel.startResult.value)
        assertEquals(1, location.startTrackingCallCount)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)
    }

    @Test
    fun entitledUserStartsTrackingBeyondQuota() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(
            location,
            StubPersistedWalkCount(10),
            SubscriptionState.entitled()
        )

        viewModel.startTracking()
        advanceUntilIdle()

        assertEquals(WalkStartResult.Started, viewModel.startResult.value)
        assertEquals(1, location.startTrackingCallCount)
    }

    @Test
    fun concurrentStartTapsTriggerExactlyOneGateInvocation() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(0))

        repeat(5) { viewModel.startTracking() }
        advanceUntilIdle()

        assertEquals(1, location.startTrackingCallCount)
        assertEquals(WalkStartResult.Started, viewModel.startResult.value)
    }

    @Test
    fun concurrentStartTapsOnDeniedQuotaStartNothing() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(3))

        repeat(5) { viewModel.startTracking() }
        advanceUntilIdle()

        assertEquals(0, location.startTrackingCallCount)
        assertIs<WalkStartResult.RequiresSubscription>(viewModel.startResult.value)
    }

    @Test
    fun isStartingWalkIsFalseBeforeAndAfterCompletion() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(0))

        assertFalse(viewModel.isStartingWalk.value)

        viewModel.startTracking()
        advanceUntilIdle()

        assertFalse(viewModel.isStartingWalk.value)
    }

    @Test
    fun isStartingWalkIsTrueWhileTheGateIsInFlight() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(0))

        viewModel.startTracking()

        assertTrue(viewModel.isStartingWalk.value)

        advanceUntilIdle()

        assertFalse(viewModel.isStartingWalk.value)
    }

    @Test
    fun consumeStartResultClearsOnlyTheEvent() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val countStub = StubPersistedWalkCount(3)
        val viewModel = createViewModel(location, countStub)

        viewModel.startTracking()
        advanceUntilIdle()
        assertIs<WalkStartResult.RequiresSubscription>(viewModel.startResult.value)

        viewModel.consumeStartResult()

        assertNull(viewModel.startResult.value)
        assertEquals(0, location.startTrackingCallCount)
    }

    @Test
    fun dismissingThePaywallDoesNotGrantAccessOnNextTap() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val countStub = StubPersistedWalkCount(3)
        val viewModel = createViewModel(location, countStub)

        viewModel.startTracking()
        advanceUntilIdle()
        viewModel.consumeStartResult()

        viewModel.startTracking()
        advanceUntilIdle()

        assertIs<WalkStartResult.RequiresSubscription>(viewModel.startResult.value)
        assertEquals(0, location.startTrackingCallCount)
        assertEquals(3, countStub.currentWalkCount())
    }

    @Test
    fun walkCreationAccessAndCountAreExposed() = runTest(testDispatcher) {
        val location = CountingLocationRepository()
        val viewModel = createViewModel(location, StubPersistedWalkCount(3))

        assertEquals(WalkCreationAccess.Allowed, viewModel.walkCreationAccess.value)
        assertEquals(0, viewModel.freeWalkCount.value)
    }
}
