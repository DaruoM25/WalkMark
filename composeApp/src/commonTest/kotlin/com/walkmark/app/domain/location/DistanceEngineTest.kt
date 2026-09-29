package com.walkmark.app.domain.location

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistanceEngineTest {

    @Test
    fun calculateDistance_samePoint_returnsZero() {
        val p1 = LocationPoint(48.8566, 2.3522, null, 1000L, 5f)
        val p2 = LocationPoint(48.8566, 2.3522, null, 2000L, 5f)

        val distance = DistanceEngine.calculateDistance(p1, p2)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun calculateDistance_knownPoints_returnsAccurateDistance() {
        // Paris Eiffel Tower (48.8584, 2.2945) to Louvre (48.8606, 2.3376) ~ 3.16 km
        val eiffel = LocationPoint(48.8584, 2.2945, null, 1000L, 5f)
        val louvre = LocationPoint(48.8606, 2.3376, null, 2000L, 5f)

        val distance = DistanceEngine.calculateDistance(eiffel, louvre)
        assertTrue(distance > 3100 && distance < 3300, "Distance should be approx 3.2km, got $distance")
    }

    @Test
    fun calculateDistance_symmetry() {
        val p1 = LocationPoint(48.8584, 2.2945, null, 1000L, 5f)
        val p2 = LocationPoint(48.8606, 2.3376, null, 2000L, 5f)

        val d1 = DistanceEngine.calculateDistance(p1, p2)
        val d2 = DistanceEngine.calculateDistance(p2, p1)
        assertEquals(d1, d2, 0.0001)
    }
}