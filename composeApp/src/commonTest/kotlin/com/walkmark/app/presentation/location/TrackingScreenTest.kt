package com.walkmark.app.presentation.location

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackingScreenTest {

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

    @Test
    fun testStartTrackingActionInvokesRepository() {
        val repo = FakeTestLocationRepository()
        val viewModel = LocationViewModel(repo)

        viewModel.startTracking()
        assertEquals(1, repo.startCount)
    }

    @Test
    fun testStopTrackingActionInvokesRepository() {
        val repo = FakeTestLocationRepository()
        val viewModel = LocationViewModel(repo)

        viewModel.startTracking()
        viewModel.stopTracking()
        assertEquals(1, repo.stopCount)
    }
}
