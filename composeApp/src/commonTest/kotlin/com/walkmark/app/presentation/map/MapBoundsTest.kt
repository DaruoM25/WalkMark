package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapBoundsTest {

    private fun coord(latitude: Double, longitude: Double) = GeoCoordinate(latitude, longitude)

    @Test
    fun emptyListReturnsNull() {
        assertNull(MapBoundsCalculator.calculate(emptyList()))
    }

    @Test
    fun singlePointProducesZeroSpan() {
        val bounds = MapBoundsCalculator.calculate(listOf(coord(48.8584, 2.2945)))

        assertEquals(48.8584, bounds?.south)
        assertEquals(48.8584, bounds?.north)
        assertEquals(2.2945, bounds?.west)
        assertEquals(2.2945, bounds?.east)
        assertEquals(0.0, bounds?.latitudeSpan)
        assertEquals(0.0, bounds?.longitudeSpan)
        assertTrue(bounds?.isSinglePoint == true)
    }

    @Test
    fun twoPointsProduceMinMaxInBothAxes() {
        val bounds = MapBoundsCalculator.calculate(
            listOf(coord(10.0, -20.0), coord(40.0, 60.0))
        )

        assertEquals(10.0, bounds?.south)
        assertEquals(40.0, bounds?.north)
        assertEquals(-20.0, bounds?.west)
        assertEquals(60.0, bounds?.east)
        assertEquals(30.0, bounds?.latitudeSpan)
        assertEquals(80.0, bounds?.longitudeSpan)
        assertEquals(25.0, bounds?.centerLatitude)
        assertEquals(20.0, bounds?.centerLongitude)
        assertFalse(bounds?.isSinglePoint == true)
    }

    @Test
    fun manyPointsProduceCorrectExtremes() {
        val coordinates = listOf(
            coord(-33.8688, 151.2093),
            coord(51.5074, -0.1278),
            coord(0.0, 0.0),
            coord(-33.8688, -70.6692),
            coord(35.6762, 139.6503)
        )

        val bounds = MapBoundsCalculator.calculate(coordinates)

        assertEquals(-33.8688, bounds?.south)
        assertEquals(51.5074, bounds?.north)
        assertEquals(-70.6692, bounds?.west)
        assertEquals(151.2093, bounds?.east)
    }

    @Test
    fun negativeLatitudesOnly() {
        val bounds = MapBoundsCalculator.calculate(
            listOf(coord(-10.0, -5.0), coord(-20.0, 5.0))
        )

        assertEquals(-20.0, bounds?.south)
        assertEquals(-10.0, bounds?.north)
    }

    @Test
    fun equatorAndPrimeMeridianCrossings() {
        val bounds = MapBoundsCalculator.calculate(
            listOf(coord(-1.0, -1.0), coord(1.0, 1.0))
        )

        assertEquals(0.0, bounds?.centerLatitude)
        assertEquals(0.0, bounds?.centerLongitude)
        assertEquals(2.0, bounds?.latitudeSpan)
        assertEquals(2.0, bounds?.longitudeSpan)
    }

    @Test
    fun inputListIsNotMutatedOrReordered() {
        val coordinates = listOf(coord(3.0, 3.0), coord(1.0, 1.0), coord(2.0, 2.0))
        val original = coordinates.toList()

        MapBoundsCalculator.calculate(coordinates)

        assertEquals(original, coordinates)
    }
}
