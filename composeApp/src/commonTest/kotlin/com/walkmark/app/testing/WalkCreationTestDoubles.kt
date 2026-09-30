package com.walkmark.app.testing

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.monetization.PersistedWalkCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StubPersistedWalkCount(initialCount: Int = 0) : PersistedWalkCount {

    private val count = MutableStateFlow(initialCount)

    override val walkCount: Flow<Int> = count.asStateFlow()

    override suspend fun currentWalkCount(): Int = count.value

    fun setWalkCount(value: Int) {
        count.value = value
    }
}

class CountingLocationRepository : LocationRepository {

    private val _trackingState = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Idle)
    override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

    private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0.0)
    override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
    override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

    var startTrackingCallCount: Int = 0
        private set

    override fun startTracking() {
        startTrackingCallCount++
        _trackingState.value = LocationTrackingState.Tracking(startTime = 1_000L)
    }

    override fun pauseTracking() {
        _trackingState.value = LocationTrackingState.Paused(startTime = 1_000L, pausedDurationMs = 0L)
    }

    override fun resumeTracking() {
        _trackingState.value = LocationTrackingState.Tracking(startTime = 1_000L)
    }

    override fun stopTracking() {
        _trackingState.value = LocationTrackingState.Idle
    }

    override fun clearSession() {
        _trackingState.value = LocationTrackingState.Idle
        _acceptedPoints.value = emptyList()
        _totalDistanceMeters.value = 0.0
        _lastLocation.value = null
    }
}
