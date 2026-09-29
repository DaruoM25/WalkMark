package com.walkmark.app.domain.location

import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val trackingState: StateFlow<LocationTrackingState>
    val acceptedPoints: StateFlow<List<LocationPoint>>
    val totalDistanceMeters: StateFlow<Double>
    val lastLocation: StateFlow<LocationPoint?>

    fun startTracking()
    fun pauseTracking()
    fun resumeTracking()
    fun stopTracking()
    fun clearSession()
}
