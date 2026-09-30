package com.walkmark.app.presentation.location

import com.walkmark.app.data.monetization.FakeSubscriptionManager
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.monetization.SubscriptionState
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.testing.StubPersistedWalkCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingScreenTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeTestLocationRepository : LocationRepository {
        private val _trackingState = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Idle)
        override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

        private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
        override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

        private val _totalDistanceMeters = MutableStateFlow(0.0)
        override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

        private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
        override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

        var startCount = 0
        var stopCount = 0

        override fun startTracking() {
            startCount++
            _trackingState.value = LocationTrackingState.Tracking(startTime = 1000L)
        }

        override fun pauseTracking() {}
        override fun resumeTracking() {}

        override fun stopTracking() {
            stopCount++
            _trackingState.value = LocationTrackingState.Idle
        }

        override fun clearSession() {}
    }

    private fun TestScope.createViewModel(repo: LocationRepository): LocationViewModel =
        LocationViewModel(
            locationRepository = repo,
            startWalk = StartWalkUseCase(
                subscriptionManager = FakeSubscriptionManager(SubscriptionState.free()),
                walkCount = StubPersistedWalkCount(0),
                locationRepository = repo,
                scope = backgroundScope
            )
        )

    @Test
    fun testStartTrackingActionInvokesRepository() = runTest(testDispatcher) {
        val repo = FakeTestLocationRepository()
        val viewModel = createViewModel(repo)

        viewModel.startTracking()
        advanceUntilIdle()
        assertEquals(1, repo.startCount)
    }

    @Test
    fun testStopTrackingActionInvokesRepository() = runTest(testDispatcher) {
        val repo = FakeTestLocationRepository()
        val viewModel = createViewModel(repo)

        viewModel.startTracking()
        advanceUntilIdle()
        viewModel.stopTracking()
        advanceUntilIdle()
        assertEquals(1, repo.stopCount)
    }
}
