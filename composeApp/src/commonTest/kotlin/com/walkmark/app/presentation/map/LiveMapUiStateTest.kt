package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.presentation.location.LocationTrackingUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LiveMapUiStateTest {
    private fun point(latitude: Double, longitude: Double, timestamp: Long) =
        LocationPoint(latitude, longitude, timestamp = timestamp, accuracy = 5f)

    @Test
    fun emptyTrackingStateHasNoPositionOrStartPoint() {
        val map = LocationTrackingUiState().toLiveMapUiState(recenterRequestId = 0)

        assertNull(map.currentPosition)
        assertNull(map.startPoint)
        assertEquals(emptyList(), map.route.coordinates)
    }

    @Test
    fun acceptedPointsKeepOrderAndLastLocationIsCurrentPosition() {
        val first = point(48.0, 2.0, 1)
        val second = point(48.1, 2.1, 2)
        val map = LocationTrackingUiState(points = listOf(first, second), lastLocation = second)
            .toLiveMapUiState(recenterRequestId = 7)

        assertEquals(listOf(GeoCoordinate(48.0, 2.0), GeoCoordinate(48.1, 2.1)), map.route.coordinates)
        assertEquals(GeoCoordinate(48.0, 2.0), map.startPoint)
        assertEquals(GeoCoordinate(48.1, 2.1), map.currentPosition)
        assertEquals(7, map.recenterRequestId)
    }
}
