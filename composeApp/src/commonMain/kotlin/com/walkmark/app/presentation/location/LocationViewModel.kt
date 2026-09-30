package com.walkmark.app.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.monetization.WalkCreationAccess
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.domain.walk.WalkStartResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocationTrackingUiState(
    val state: LocationTrackingState = LocationTrackingState.Idle,
    val points: List<LocationPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val lastLocation: LocationPoint? = null,
    val errorMessage: String? = null
)

class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val startWalk: StartWalkUseCase
) : ViewModel() {

    private val _startResult = MutableStateFlow<WalkStartResult?>(null)
    val startResult: StateFlow<WalkStartResult?> = _startResult.asStateFlow()

    private val _isStartingWalk = MutableStateFlow(false)
    val isStartingWalk: StateFlow<Boolean> = _isStartingWalk.asStateFlow()

    val walkCreationAccess: StateFlow<WalkCreationAccess> = startWalk.access

    val freeWalkCount: StateFlow<Int> = startWalk.freeWalkCount

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
        if (_isStartingWalk.value) return
        _isStartingWalk.value = true
        viewModelScope.launch {
            try {
                _startResult.value = startWalk.startWalk()
            } finally {
                _isStartingWalk.value = false
            }
        }
    }

    fun consumeStartResult() {
        _startResult.value = null
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
