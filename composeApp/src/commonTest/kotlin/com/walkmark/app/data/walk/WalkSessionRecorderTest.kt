package com.walkmark.app.data.walk

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WalkSessionRecorderTest {

    private val monotonicHashCode = 1_234_567_890L

    private fun recorder(
        location: FakeLocationRepository,
        walks: RecordingWalkRepository,
        scope: TestScope,
        clockValues: List<Long> = listOf(1_000L, 2_000L, 3_000L, 4_000L, 5_000L)
    ): WalkSessionRecorder {
        val clock = { var index = 0; { clockValues[index++ % clockValues.size] } }()
        var idCounter = 0
        return WalkSessionRecorder(
            locationRepository = location,
            walkRepository = walks,
            scope = scope,
            clock = clock,
            idGenerator = { "walk-${++idCounter}" }
        )
    }

    @Test
    fun startsNoWalkWhileIdle() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        assertTrue(walks.startCalls.isEmpty())
        assertTrue(walks.completeCalls.isEmpty())
    }

    @Test
    fun startsWalkOnTrackingTransition() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(startTime = monotonicHashCode)
        advanceUntilIdle()

        assertEquals(1, walks.startCalls.size)
        assertEquals("walk-1", walks.startCalls[0].walkId)
    }

    @Test
    fun usesInjectedClockNotMonotonicTrackingStartTime() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope, clockValues = listOf(1_700_000_000_000L)).start()
        advanceUntilIdle()

        location.emitTracking(startTime = monotonicHashCode)
        advanceUntilIdle()

        val start = walks.startCalls.single()
        assertEquals(1_700_000_000_000L, start.startTimeEpochMs)
        assertTrue(
            start.startTimeEpochMs != monotonicHashCode,
            "walk start time must not be US-002's monotonic mark hashCode"
        )
    }

    @Test
    fun appendsOnlyNewPointsAndNeverDuplicates() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()

        location.appendPoints(count = 2)
        advanceUntilIdle()
        location.appendPoints(count = 3)
        advanceUntilIdle()

        val allAppended = walks.appendedPoints.flatMap { it.second }
        assertEquals(5, allAppended.size)
        assertEquals(5, allAppended.map { it.timestamp }.toSet().size)
        assertTrue(walks.appendedPoints.all { it.first == "walk-1" })
    }

    @Test
    fun appendsPendingPointsEmittedBeforeTrackingStarted() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.appendPoints(count = 2)
        advanceUntilIdle()
        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()

        assertTrue(walks.appendedPoints.isEmpty(), "points before a walk must not be attributed to it")
    }

    @Test
    fun doesNotReappendEarlierSessionPointsToASecondWalk() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.appendPoints(count = 3)
        advanceUntilIdle()
        location.emitIdle()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()

        val secondWalkAppends = walks.appendedPoints.filter { it.first == "walk-2" }
        assertTrue(
            secondWalkAppends.isEmpty(),
            "a new walk must not inherit the previous walk's accepted points"
        )
    }

    @Test
    fun completesWalkOnIdleWithTotalDistance() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.appendPoints(count = 2)
        advanceUntilIdle()
        location.setTotalDistance(1234.5)
        advanceUntilIdle()
        location.emitIdle()
        advanceUntilIdle()

        val complete = walks.completeCalls.single()
        assertEquals("walk-1", complete.walkId)
        assertEquals(1234.5, complete.totalDistanceMeters)
    }

    @Test
    fun pauseDoesNotCompleteTheWalk() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.emitPaused(monotonicHashCode, pausedDurationMs = 5_000L)
        advanceUntilIdle()

        assertTrue(walks.completeCalls.isEmpty())
        assertEquals(1, walks.startCalls.size)
    }

    @Test
    fun errorStateKeepsWalkOpenUntilIdle() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.emitError("GPS lost")
        advanceUntilIdle()

        assertTrue(walks.completeCalls.isEmpty())

        location.emitIdle()
        advanceUntilIdle()

        assertEquals(1, walks.completeCalls.size)
    }

    @Test
    fun idleAfterPointsArePendingFlushesThemBeforeCompleting() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.appendPoints(count = 1)
        location.emitIdle()
        advanceUntilIdle()

        assertEquals(1, walks.appendedPoints.sumOf { it.second.size })
        assertEquals(1, walks.completeCalls.size)
    }

    @Test
    fun twoFullWalksInOneSessionProduceTwoCompleteCalls() = runTest {
        val location = FakeLocationRepository()
        val walks = RecordingWalkRepository()
        val scope = TestScope(StandardTestDispatcher(testScheduler))

        recorder(location, walks, scope).start()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.appendPoints(count = 2)
        location.emitIdle()
        advanceUntilIdle()

        location.emitTracking(monotonicHashCode)
        advanceUntilIdle()
        location.appendPoints(count = 2, fromIndex = 2)
        location.emitIdle()
        advanceUntilIdle()

        assertEquals(2, walks.startCalls.size)
        assertEquals(2, walks.completeCalls.size)
        assertEquals(listOf("walk-1", "walk-2"), walks.completeCalls.map { it.walkId })
    }
}
