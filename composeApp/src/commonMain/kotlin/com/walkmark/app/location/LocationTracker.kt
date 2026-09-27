package com.walkmark.app.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracyMeters: Float? = null,
    val timestamp: Instant
)

enum class TrackingState {
    IDLE,
    RECORDING,
    PAUSED
}

interface LocationTracker {
    val trackingState: Flow<TrackingState>
    val currentLocation: Flow<LocationPoint?>
    fun startTracking()
    fun pauseTracking()
    fun stopTracking()
}

class DefaultLocationTracker : LocationTracker {
    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    override val trackingState: Flow<TrackingState> = _trackingState.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    override val currentLocation: Flow<LocationPoint?> = _currentLocation.asStateFlow()

    override fun startTracking() {
        _trackingState.value = TrackingState.RECORDING
    }

    override fun pauseTracking() {
        _trackingState.value = TrackingState.PAUSED
    }

    override fun stopTracking() {
        _trackingState.value = TrackingState.IDLE
    }

    fun emitLocation(point: LocationPoint) {
        _currentLocation.value = point
    }
}
