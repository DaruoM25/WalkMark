package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.domain.location.LocationPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapRouteUiModelTest {

    private fun point(latitude: Double, longitude: Double, timestamp: Long) =
        LocationPoint(
            latitude = latitude,
            longitude = longitude,
            altitude = 35.0,
            timestamp = timestamp,
            accuracy = 5f
        )

    @Test
    fun emptyLocationListProducesEmptyRoute() {
        val route = emptyList<LocationPoint>().toMapRouteUiModel()

        assertTrue(route.isEmpty)
        assertEquals(0, route.pointCount)
        assertEquals(null, route.bounds)
    }

    @Test
    fun pointOrderIsPreserved() {
        val points = listOf(
            point(48.0, 2.0, 1L),
            point(48.1, 2.1, 2L),
            point(48.2, 2.2, 3L)
        )

        val route = points.toMapRouteUiModel()

        assertEquals(3, route.pointCount)
        assertEquals(GeoCoordinate(48.0, 2.0), route.coordinates[0])
        assertEquals(GeoCoordinate(48.1, 2.1), route.coordinates[1])
        assertEquals(GeoCoordinate(48.2, 2.2), route.coordinates[2])
    }

    @Test
    fun routeDerivesBoundsFromCoordinates() {
        val route = listOf(
            point(10.0, 20.0, 1L),
            point(30.0, 40.0, 2L)
        ).toMapRouteUiModel()

        val bounds = route.bounds

        assertEquals(10.0, bounds?.south)
        assertEquals(30.0, bounds?.north)
        assertEquals(20.0, bounds?.west)
        assertEquals(40.0, bounds?.east)
    }

    @Test
    fun sourceListIsNotMutated() {
        val points = listOf(point(1.0, 1.0, 1L), point(2.0, 2.0, 2L))
        val snapshot = points.toList()

        points.toMapRouteUiModel()

        assertEquals(snapshot, points)
        assertFalse(points.isEmpty())
    }
}
