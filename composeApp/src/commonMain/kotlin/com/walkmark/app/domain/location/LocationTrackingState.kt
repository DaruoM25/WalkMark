package com.walkmark.app.domain.location

sealed interface LocationTrackingState {
    data object Idle : LocationTrackingState
    data class Tracking(val startTime: Long) : LocationTrackingState
    data class Paused(val startTime: Long, val pausedDurationMs: Long = 0L) : LocationTrackingState
    data class Error(val message: String) : LocationTrackingState
}
