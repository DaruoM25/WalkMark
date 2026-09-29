package com.walkmark.app.presentation.location

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

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

        var startTrackingCallCount = 0
        var pauseTrackingCallCount = 0
        var resumeTrackingCallCount = 0
        var stopTrackingCallCount = 0
        var clearSessionCallCount = 0

        override fun startTracking() {
            startTrackingCallCount++
            _trackingState.value = LocationTrackingState.Tracking(startTime = 1000L)
        }

        override fun pauseTracking() {
            pauseTrackingCallCount++
            _trackingState.value = LocationTrackingState.Paused(startTime = 1000L, pausedDurationMs = 0L)
        }

        override fun resumeTracking() {
            resumeTrackingCallCount++
            _trackingState.value = LocationTrackingState.Tracking(startTime = 1000L)
        }

        override fun stopTracking() {
            stopTrackingCallCount++
            _trackingState.value = LocationTrackingState.Idle
        }

        override fun clearSession() {
            clearSessionCallCount++
            _trackingState.value = LocationTrackingState.Idle
            _acceptedPoints.value = emptyList()
            _totalDistanceMeters.value = 0.0
            _lastLocation.value = null
        }

        fun emitAcceptedPoint(point: LocationPoint, distanceDelta: Double) {
            _acceptedPoints.value = _acceptedPoints.value + point
            _lastLocation.value = point
            _totalDistanceMeters.value = _totalDistanceMeters.value + distanceDelta
        }

        fun emitError(message: String) {
            _trackingState.value = LocationTrackingState.Error(message)
        }
    }

    @Test
    fun viewModel_initialState_isIdle() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(repository)

        assertEquals(LocationTrackingState.Idle, viewModel.uiState.value.state)
        assertEquals(0, viewModel.uiState.value.points.size)
        assertEquals(0.0, viewModel.uiState.value.distanceMeters)
        assertNull(viewModel.uiState.value.lastLocation)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun viewModel_startTracking_updatesStateAndDelegatesOnce() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.startTracking()
        advanceUntilIdle()

        assertEquals(1, repository.startTrackingCallCount)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)
    }

    @Test
    fun viewModel_receivesAcceptedPoints_updatesDistanceAndPoints() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.startTracking()
        advanceUntilIdle()

        val p1 = LocationPoint(48.8566, 2.3522, null, 1000L, 5f)
        repository.emitAcceptedPoint(p1, 0.0)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.points.size)
        assertEquals(48.8566, viewModel.uiState.value.lastLocation?.latitude)

        val p2 = LocationPoint(48.8570, 2.3522, null, 2000L, 5f)
        repository.emitAcceptedPoint(p2, 44.4)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.points.size)
        assertEquals(44.4, viewModel.uiState.value.distanceMeters)
    }

    @Test
    fun viewModel_errorState_exposesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        repository.emitError("GPS signal lost")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Error)
        assertEquals("GPS signal lost", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun viewModel_actions_delegateCorrectly() = runTest(testDispatcher) {
        val repository = FakeLocationRepository()
        val viewModel = LocationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        viewModel.startTracking()
        advanceUntilIdle()
        assertEquals(1, repository.startTrackingCallCount)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)

        viewModel.pauseTracking()
        advanceUntilIdle()
        assertEquals(1, repository.pauseTrackingCallCount)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Paused)

        viewModel.resumeTracking()
        advanceUntilIdle()
        assertEquals(1, repository.resumeTrackingCallCount)
        assertTrue(viewModel.uiState.value.state is LocationTrackingState.Tracking)

        viewModel.stopTracking()
        advanceUntilIdle()
        assertEquals(1, repository.stopTrackingCallCount)
        assertEquals(LocationTrackingState.Idle, viewModel.uiState.value.state)

        viewModel.resetSession()
        advanceUntilIdle()
        assertEquals(1, repository.clearSessionCallCount)
    }
}
