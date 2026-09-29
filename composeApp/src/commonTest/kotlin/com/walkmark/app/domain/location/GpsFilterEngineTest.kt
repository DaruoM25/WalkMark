package com.walkmark.app.domain.location

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GpsFilterEngineTest {

    private val filterEngine = GpsFilterEngine()

    @Test
    fun shouldAccept_firstValidPoint_returnsTrue() {
        val point = LocationPoint(48.8566, 2.3522, null, 1000L, 10f)
        assertTrue(filterEngine.shouldAccept(point, null))
    }

    @Test
    fun shouldAccept_invalidLatitude_returnsFalse() {
        val point = LocationPoint(95.0, 2.3522, null, 1000L, 10f)
        assertFalse(filterEngine.shouldAccept(point, null))

        val pointNeg = LocationPoint(-91.0, 2.3522, null, 1000L, 10f)
        assertFalse(filterEngine.shouldAccept(pointNeg, null))
    }

    @Test
    fun shouldAccept_invalidLongitude_returnsFalse() {
        val point = LocationPoint(48.8566, 185.0, null, 1000L, 10f)
        assertFalse(filterEngine.shouldAccept(point, null))

        val pointNeg = LocationPoint(48.8566, -185.0, null, 1000L, 10f)
        assertFalse(filterEngine.shouldAccept(pointNeg, null))
    }

    @Test
    fun shouldAccept_accuracyGreaterThan20m_returnsFalse() {
        val point = LocationPoint(48.8566, 2.3522, null, 1000L, 20.1f)
        assertFalse(filterEngine.shouldAccept(point, null))

        val validPoint = LocationPoint(48.8566, 2.3522, null, 1000L, 20.0f)
        assertTrue(filterEngine.shouldAccept(validPoint, null))
    }

    @Test
    fun shouldAccept_timestampRegressionOrDuplicate_returnsFalse() {
        val p1 = LocationPoint(48.8566, 2.3522, null, 2000L, 10f)
        val duplicateTime = LocationPoint(48.8567, 2.3523, null, 2000L, 10f)
        val regressionTime = LocationPoint(48.8567, 2.3523, null, 1999L, 10f)

        assertFalse(filterEngine.shouldAccept(duplicateTime, p1))
        assertFalse(filterEngine.shouldAccept(regressionTime, p1))
    }

    @Test
    fun shouldAccept_speedGreaterThanLimit_returnsFalse() {
        // Distance ~ 100 meters in 1 second -> speed = 100 m/s > 8.33 m/s
        val p1 = LocationPoint(48.8566, 2.3522, null, 1000L, 5f)
        val p2Teleport = LocationPoint(48.8575, 2.3522, null, 2000L, 5f)

        assertFalse(filterEngine.shouldAccept(p2Teleport, p1))
    }

    @Test
    fun shouldAccept_normalWalkingSpeed_returnsTrue() {
        // Distance ~ 1.5 meters in 1 second -> speed = 1.5 m/s <= 8.33 m/s
        val p1 = LocationPoint(48.856600, 2.352200, null, 1000L, 5f)
        val p2Walk = LocationPoint(48.856613, 2.352200, null, 2000L, 5f)

        assertTrue(filterEngine.shouldAccept(p2Walk, p1))
    }
}