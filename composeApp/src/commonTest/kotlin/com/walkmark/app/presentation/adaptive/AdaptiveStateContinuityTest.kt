package com.walkmark.app.presentation.adaptive

import androidx.compose.ui.unit.dp
import com.walkmark.app.data.monetization.FakeSubscriptionManager
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.monetization.SubscriptionState
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.testing.StubPersistedWalkCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class AdaptiveStateContinuityTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeLocationRepository : LocationRepository {
        private val _trackingState = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Idle)
        override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

        private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
        override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

        private val _totalDistanceMeters = MutableStateFlow(0.0)
        override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

        private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
        override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

        override fun startTracking() {
            _trackingState.value = LocationTrackingState.Tracking(1000L)
        }

        fun emitPoint(point: LocationPoint, distanceDelta: Double) {
            _acceptedPoints.value = _acceptedPoints.value + point
            _lastLocation.value = point
            _totalDistanceMeters.value += distanceDelta
        }

        override fun pauseTracking() {}
        override fun resumeTracking() {}
        override fun stopTracking() {
            _trackingState.value = LocationTrackingState.Idle
        }
        override fun clearSession() {}
    }

    @Test
    fun testTrackingStateSurvivesLayoutModeTransitions() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(
            locationRepository = repository,
            startWalk = StartWalkUseCase(
                subscriptionManager = FakeSubscriptionManager(SubscriptionState.free()),
                walkCount = StubPersistedWalkCount(0),
                locationRepository = repository,
                scope = backgroundScope
            )
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        // 1. Start tracking on Compact phone screen
        viewModel.startTracking()
        advanceUntilIdle()

        val point1 = LocationPoint(48.8566, 2.3522, 35.0, 1000L, 5.0f)
        repository.emitPoint(point1, 10.0)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.points.size)
        assertEquals(10.0, viewModel.uiState.value.distanceMeters)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)

        // 2. Simulate fold unfolding to Expanded TwoPane (e.g. 1024.dp)
        val expandedWidthClass = AdaptiveLayoutResolver.classifyWidth(1024.dp)
        val expandedLayoutMode = AdaptiveLayoutResolver.resolveLayoutMode(expandedWidthClass)
        assertEquals(AdaptiveLayoutMode.ExpandedTwoPane, expandedLayoutMode)

        // Verify LocationViewModel tracking state is preserved
        assertEquals(1, viewModel.uiState.value.points.size)
        assertEquals(10.0, viewModel.uiState.value.distanceMeters)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)

        // 3. Emit second point on Expanded layout
        val point2 = LocationPoint(48.8567, 2.3522, 35.0, 5000L, 5.0f)
        repository.emitPoint(point2, 12.0)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.points.size)
        assertEquals(22.0, viewModel.uiState.value.distanceMeters)

        // 4. Simulate fold folding back to Compact (e.g. 400.dp)
        val compactWidthClass = AdaptiveLayoutResolver.classifyWidth(400.dp)
        val compactLayoutMode = AdaptiveLayoutResolver.resolveLayoutMode(compactWidthClass)
        assertEquals(AdaptiveLayoutMode.CompactSinglePane, compactLayoutMode)

        // Tracking state remains completely intact
        assertEquals(2, viewModel.uiState.value.points.size)
        assertEquals(22.0, viewModel.uiState.value.distanceMeters)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)
    }
}

