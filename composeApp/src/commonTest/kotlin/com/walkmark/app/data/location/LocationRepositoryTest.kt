package com.walkmark.app.data.location

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startTracking_initializesStateAndCollectsLocations() = runTest(testDispatcher) {
        val rawFlow = MutableSharedFlow<LocationPoint>(extraBufferCapacity = 64)
        var startPlatformCalled = false
        var stopPlatformCalled = false

        val repository = DefaultLocationRepository(
            rawLocationFlow = rawFlow,
            onStartPlatformTracking = { startPlatformCalled = true },
            onStopPlatformTracking = { stopPlatformCalled = true },
            scope = this,
            timeProvider = { 1000L }
        )

        repository.startTracking()
        advanceUntilIdle()
        assertTrue(startPlatformCalled)
        assertTrue(repository.trackingState.value is LocationTrackingState.Tracking)

        val p1 = LocationPoint(48.8566, 2.3522, null, 1000L, 5f)
        rawFlow.emit(p1)
        advanceUntilIdle()

        assertEquals(1, repository.acceptedPoints.value.size)
        assertEquals(p1, repository.lastLocation.value)
        assertEquals(0.0, repository.totalDistanceMeters.value)

        repository.stopTracking()
        advanceUntilIdle()
        assertTrue(stopPlatformCalled)
        assertEquals(LocationTrackingState.Idle, repository.trackingState.value)
    }

    @Test
    fun pauseAndResumeTracking_handlesStateProperly() = runTest(testDispatcher) {
        val rawFlow = MutableSharedFlow<LocationPoint>(extraBufferCapacity = 64)
        var currentTime = 1000L

        val repository = DefaultLocationRepository(
            rawLocationFlow = rawFlow,
            scope = this,
            timeProvider = { currentTime }
        )

        repository.startTracking()
        advanceUntilIdle()
        assertTrue(repository.trackingState.value is LocationTrackingState.Tracking)

        currentTime = 2000L
        repository.pauseTracking()
        assertTrue(repository.trackingState.value is LocationTrackingState.Paused)

        val pPaused = LocationPoint(48.8566, 2.3522, null, 2500L, 5f)
        rawFlow.emit(pPaused)
        advanceUntilIdle()
        assertEquals(0, repository.acceptedPoints.value.size)

        currentTime = 3000L
        repository.resumeTracking()
        assertTrue(repository.trackingState.value is LocationTrackingState.Tracking)

        val pResumed = LocationPoint(48.8566, 2.3522, null, 3500L, 5f)
        rawFlow.emit(pResumed)
        advanceUntilIdle()
        assertEquals(1, repository.acceptedPoints.value.size)

        repository.stopTracking()
        advanceUntilIdle()
    }
}