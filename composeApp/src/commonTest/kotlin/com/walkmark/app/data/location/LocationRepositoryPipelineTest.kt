package com.walkmark.app.data.location

import com.walkmark.app.domain.location.GpsFilterEngine
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationTrackingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryPipelineTest {

    @Test
    fun testPlatformStreamToRepositoryPipelineLifecycle() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val rawPlatformFlow = MutableSharedFlow<LocationPoint>(extraBufferCapacity = 64)
        var startPlatformTrackingCalls = 0
        var stopPlatformTrackingCalls = 0

        val repository = DefaultLocationRepository(
            rawLocationFlow = rawPlatformFlow,
            onStartPlatformTracking = { startPlatformTrackingCalls++ },
            onStopPlatformTracking = { stopPlatformTrackingCalls++ },
            filterEngine = GpsFilterEngine(maxAcceptedAccuracyMeters = 50.0f, maxReasonableSpeedMps = 15.0),
            scope = testScope,
            timeProvider = { 1000L }
        )

        // Initial State
        assertEquals(LocationTrackingState.Idle, repository.trackingState.value)
        assertEquals(0, repository.acceptedPoints.value.size)

        // 1. START TRACKING
        repository.startTracking()
        assertTrue(repository.trackingState.value is LocationTrackingState.Tracking)
        assertEquals(1, startPlatformTrackingCalls)

        // 2. Emit Point A
        val pointA = LocationPoint(
            latitude = 48.8566,
            longitude = 2.3522,
            altitude = 35.0,
            timestamp = 1000L,
            accuracy = 5.0f
        )
        rawPlatformFlow.emit(pointA)

        assertEquals(1, repository.acceptedPoints.value.size)
        assertEquals(pointA, repository.acceptedPoints.value.first())
        assertEquals(pointA, repository.lastLocation.value)
        assertEquals(0.0, repository.totalDistanceMeters.value)

        // 3. Emit Point B (10 meters away, 5 seconds later -> valid walking speed)
        val pointB = LocationPoint(
            latitude = 48.8567,
            longitude = 2.3522,
            altitude = 35.0,
            timestamp = 6000L,
            accuracy = 5.0f
        )
        rawPlatformFlow.emit(pointB)

        assertEquals(2, repository.acceptedPoints.value.size)
        assertEquals(pointB, repository.acceptedPoints.value.last())
        assertEquals(pointB, repository.lastLocation.value)
        assertTrue(repository.totalDistanceMeters.value > 0.0)

        // 4. STOP TRACKING
        repository.stopTracking()
        assertEquals(LocationTrackingState.Idle, repository.trackingState.value)
        assertEquals(1, stopPlatformTrackingCalls)

        // 5. Emit Point C after STOP -> must NOT reach accepted points
        val pointC = LocationPoint(
            latitude = 48.8568,
            longitude = 2.3522,
            altitude = 35.0,
            timestamp = 12000L,
            accuracy = 5.0f
        )
        rawPlatformFlow.emit(pointC)

        assertEquals(2, repository.acceptedPoints.value.size)
        assertEquals(pointB, repository.acceptedPoints.value.last())
    }
}
