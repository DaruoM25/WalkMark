package com.walkmark.app.presentation.journal

import com.walkmark.app.domain.walk.WalkDeleteResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WalkPresentationTest {
    @Test
    fun distanceAndDurationUsePersistedValuesWithoutInventingMetrics() {
        assertEquals("850 m", formatWalkDistance(850.9))
        assertEquals("1.2 km", formatWalkDistance(1234.0))
        assertEquals("2m", formatWalkDuration(125))
        assertEquals("1h 2m", formatWalkDuration(3725))
    }

    @Test
    fun partialMediaFailureIsNotReportedAsTotalCleanupSuccess() {
        val full = deletionNotice(WalkDeleteResult.Success)
        val partial = deletionNotice(WalkDeleteResult.SuccessWithMediaCleanupFailures(listOf("private/path.jpg")))

        assertEquals("Walk deleted.", full)
        assertEquals("Walk deleted, but some photo files could not be removed.", partial)
        assertFalse(partial.contains("private/path.jpg"))
    }
}
