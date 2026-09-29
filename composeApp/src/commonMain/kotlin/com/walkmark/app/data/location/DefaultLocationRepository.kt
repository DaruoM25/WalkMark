package com.walkmark.app.data.location

import com.walkmark.app.domain.location.DistanceEngine
import com.walkmark.app.domain.location.GpsFilterEngine
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultLocationRepository(
    private val rawLocationFlow: Flow<LocationPoint>,
    private val onStartPlatformTracking: () -> Unit = {},
    private val onStopPlatformTracking: () -> Unit = {},
    private val filterEngine: GpsFilterEngine = GpsFilterEngine(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val timeProvider: () -> Long = { kotlin.time.TimeSource.Monotonic.markNow().hashCode().toLong() }
) : LocationRepository {

    private val _trackingState = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Idle)
    override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

    private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0.0)
    override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
    override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

    private var trackingJob: Job? = null
    private var pauseStartTime: Long = 0L
    private var accumulatedPauseDurationMs: Long = 0L

    override fun startTracking() {
        val now = timeProvider()
        _trackingState.value = LocationTrackingState.Tracking(startTime = now)
        accumulatedPauseDurationMs = 0L
        onStartPlatformTracking()

        trackingJob?.cancel()
        trackingJob = scope.launch {
            rawLocationFlow.collect { rawPoint ->
                processLocationUpdate(rawPoint)
            }
        }
    }

    override fun pauseTracking() {
        val current = _trackingState.value
        if (current is LocationTrackingState.Tracking) {
            pauseStartTime = timeProvider()
            _trackingState.value = LocationTrackingState.Paused(
                startTime = current.startTime,
                pausedDurationMs = accumulatedPauseDurationMs
            )
        }
    }

    override fun resumeTracking() {
        val current = _trackingState.value
        if (current is LocationTrackingState.Paused) {
            val pauseDuration = if (pauseStartTime > 0L) timeProvider() - pauseStartTime else 0L
            accumulatedPauseDurationMs += pauseDuration
            _trackingState.value = LocationTrackingState.Tracking(startTime = current.startTime)
            pauseStartTime = 0L
        }
    }

    override fun stopTracking() {
        _trackingState.value = LocationTrackingState.Idle
        trackingJob?.cancel()
        trackingJob = null
        onStopPlatformTracking()
    }

    override fun clearSession() {
        stopTracking()
        _acceptedPoints.value = emptyList()
        _totalDistanceMeters.value = 0.0
        _lastLocation.value = null
    }

    fun processLocationUpdate(candidate: LocationPoint) {
        val currentState = _trackingState.value
        if (currentState !is LocationTrackingState.Tracking) {
            // Drop points while paused or idle
            return
        }

        val lastPoint = _acceptedPoints.value.lastOrNull()
        if (filterEngine.shouldAccept(candidate, lastPoint)) {
            _acceptedPoints.update { it + candidate }
            _lastLocation.value = candidate

            if (lastPoint != null) {
                val delta = DistanceEngine.calculateDistance(lastPoint, candidate)
                _totalDistanceMeters.update { it + delta }
            }
        }
    }
}
