package com.walkmark.app.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LocationTrackingUiState(
    val state: LocationTrackingState = LocationTrackingState.Idle,
    val points: List<LocationPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val lastLocation: LocationPoint? = null,
    val errorMessage: String? = null
)

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val uiState: StateFlow<LocationTrackingUiState> = combine(
        locationRepository.trackingState,
        locationRepository.acceptedPoints,
        locationRepository.totalDistanceMeters,
        locationRepository.lastLocation
    ) { state, points, distance, lastLocation ->
        val errorMsg = (state as? LocationTrackingState.Error)?.message
        LocationTrackingUiState(
            state = state,
            points = points,
            distanceMeters = distance,
            lastLocation = lastLocation,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocationTrackingUiState()
    )

    fun startTracking() {
        locationRepository.startTracking()
    }

    fun pauseTracking() {
        locationRepository.pauseTracking()
    }

    fun resumeTracking() {
        locationRepository.resumeTracking()
    }

    fun stopTracking() {
        locationRepository.stopTracking()
    }

    fun resetSession() {
        locationRepository.clearSession()
    }
}
