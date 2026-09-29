package com.walkmark.app.data.walk

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLocationRepository : LocationRepository {

    private val _trackingState = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Idle)
    override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

    private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0.0)
    override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
    override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

    fun emitTracking(startTime: Long) {
        _trackingState.value = LocationTrackingState.Tracking(startTime = startTime)
    }

    fun emitPaused(startTime: Long, pausedDurationMs: Long = 0L) {
        _trackingState.value =
            LocationTrackingState.Paused(startTime = startTime, pausedDurationMs = pausedDurationMs)
    }

    fun emitIdle() {
        _trackingState.value = LocationTrackingState.Idle
    }

    fun emitError(message: String) {
        _trackingState.value = LocationTrackingState.Error(message)
    }

    fun appendPoints(count: Int, fromIndex: Int = _acceptedPoints.value.size) {
        val existing = _acceptedPoints.value
        val fresh = (fromIndex until fromIndex + count).map { index ->
            LocationPoint(
                latitude = 48.0 + index / 1000.0,
                longitude = 2.0 + index / 1000.0,
                altitude = null,
                timestamp = 1_000L + index,
                accuracy = 4f
            )
        }
        _acceptedPoints.value = existing + fresh
    }

    fun setTotalDistance(meters: Double) {
        _totalDistanceMeters.value = meters
    }

    fun clearPoints() {
        _acceptedPoints.value = emptyList()
    }

    override fun startTracking() = Unit
    override fun pauseTracking() = Unit
    override fun resumeTracking() = Unit
    override fun stopTracking() = Unit
    override fun clearSession() = Unit
}
