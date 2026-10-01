package com.walkmark.app.domain.export

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GpxDocumentGeneratorTest {

    private val walk = Walk(
        id = "walk-1",
        title = "Morning loop",
        summary = "Nice weather",
        startTimeEpochMs = 1_700_000_000_000L,
        endTimeEpochMs = 1_700_003_600_000L,
        totalDistanceMeters = 4321.5,
        durationSeconds = 3600L,
        status = WalkStatus.COMPLETED
    )

    private fun point(
        latitude: Double = 48.8584,
        longitude: Double = 2.2945,
        altitude: Double? = null,
        timestamp: Long = 1_700_000_000_000L
    ) = LocationPoint(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        timestamp = timestamp,
        accuracy = 4.5f
    )

    @Test
    fun documentDeclaresGpx11XmlDeclarationAndNamespace() {
        val gpx = GpxDocumentGenerator.generate(walk, listOf(point()))

        assertTrue(gpx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), gpx)
        assertTrue(gpx.contains("version=\"1.1\""), gpx)
        assertTrue(gpx.contains("creator=\"WalkMark\""), gpx)
        assertTrue(gpx.contains("xmlns=\"http://www.topografix.com/GPX/1/1\""), gpx)
        assertTrue(
            gpx.contains(
                "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                    "http://www.topografix.com/GPX/1/1/gpx.xsd\""
            ),
            gpx
        )
        assertTrue(gpx.trimEnd().endsWith("</gpx>"), gpx)
    }

    @Test
    fun metadataCarriesWalkmarkGeneratedFields() {
        val gpx = GpxDocumentGenerator.generate(walk, listOf(point()))

        assertTrue(gpx.contains("<name>Morning loop</name>"), gpx)
        assertTrue(gpx.contains("<desc>Nice weather</desc>"), gpx)
        assertTrue(gpx.contains("<time>2023-11-14T22:13:20.000Z</time>"), gpx)
    }

    @Test
    fun metadataOmitsDescWhenWalkHasNoSummary() {
        val gpx = GpxDocumentGenerator.generate(walk.copy(summary = null), listOf(point()))

        assertFalse(gpx.contains("<desc>"), gpx)
    }

    @Test
    fun trackPointsPreserveSuppliedOrder() {
        val points = listOf(
            point(latitude = 1.0, longitude = 1.0),
            point(latitude = 2.0, longitude = 2.0),
            point(latitude = 3.0, longitude = 3.0)
        )

        val gpx = GpxDocumentGenerator.generate(walk, points)

        val latOrder = Regex("lat=\"([^\"]+)\"").findAll(gpx).map { it.groupValues[1] }.toList()
        assertEquals(listOf("1.0", "2.0", "3.0"), latOrder)
        assertEquals(3, Regex("<trkpt ").findAll(gpx).count())
    }

    @Test
    fun latitudeAndLongitudeAreSerialisedWithoutEscapingOrLocalisation() {
        val gpx = GpxDocumentGenerator.generate(
            walk,
            listOf(point(latitude = -33.8688, longitude = 151.2093))
        )

        assertTrue(gpx.contains("<trkpt lat=\"-33.8688\" lon=\"151.2093\">"), gpx)
        assertFalse(gpx.contains(","), "coordinates must not use a locale decimal comma: $gpx")
    }

    @Test
    fun textValuesAreXmlEscaped() {
        val hostile = Walk(
            id = "walk-1",
            title = "Ben & Jerry <best> \"walk\" it's",
            summary = "5 > 3 & 2 < 1",
            startTimeEpochMs = 1_700_000_000_000L,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = WalkStatus.COMPLETED
        )

        val gpx = GpxDocumentGenerator.generate(hostile, listOf(point()))

        assertTrue(gpx.contains("Ben &amp; Jerry &lt;best&gt; &quot;walk&quot; it&apos;s"), gpx)
        assertTrue(gpx.contains("5 &gt; 3 &amp; 2 &lt; 1"), gpx)
        assertFalse(gpx.contains("<best>"), gpx)
    }

    @Test
    fun timestampIsSerialisedAsUtcWhenPresent() {
        val gpx = GpxDocumentGenerator.generate(
            walk,
            listOf(point(timestamp = 1_700_000_123_456L))
        )

        assertTrue(gpx.contains("<time>2023-11-14T22:13:23.456Z</time>"), gpx)
    }

    @Test
    fun altitudeIsSerialisedWhenPresent() {
        val gpx = GpxDocumentGenerator.generate(walk, listOf(point(altitude = 35.5)))

        assertTrue(gpx.contains("<ele>35.5</ele>"), gpx)
    }

    @Test
    fun missingAltitudeOmitsEleElement() {
        val gpx = GpxDocumentGenerator.generate(walk, listOf(point(altitude = null)))

        assertFalse(gpx.contains("<ele>"), gpx)
        assertTrue(gpx.contains("<trkpt "), gpx)
    }

    @Test
    fun missingTimestampOmitsTimeElementWithoutInventingOne() {
        val gpx = GpxDocumentGenerator.generate(walk, listOf(point(timestamp = 0L)))

        assertFalse(gpx.contains("<time>"), gpx)
        assertNull(GpxDocumentGenerator.toUtcTimestamp(0L))
        assertNull(GpxDocumentGenerator.toUtcTimestamp(-1L))
    }

    @Test
    fun walkWithNoPointsProducesValidEmptyTrackAndNoTrackPoint() {
        val gpx = GpxDocumentGenerator.generate(walk, emptyList())

        assertTrue(gpx.contains("<trkseg>"), gpx)
        assertTrue(gpx.contains("</trkseg>"), gpx)
        assertEquals(0, Regex("<trkpt ").findAll(gpx).count())
        assertFalse(gpx.contains("<ele>"), gpx)
    }

    @Test
    fun nonPositiveWalkStartTimeOmitsMetadataTime() {
        val gpx = GpxDocumentGenerator.generate(walk.copy(startTimeEpochMs = 0L), emptyList())

        assertFalse(gpx.contains("<time>"), gpx)
    }

    @Test
    fun utcTimestampConversionHandlesLeapDayAndEpochBoundaries() {
        assertEquals("1970-01-01T00:00:00.000Z", GpxDocumentGenerator.toUtcTimestamp(0L + 1L))
        assertEquals("2020-02-29T12:00:00.000Z", GpxDocumentGenerator.toUtcTimestamp(1_582_977_600_000L))
        assertEquals("2000-02-29T00:00:00.000Z", GpxDocumentGenerator.toUtcTimestamp(951_782_400_000L))
        assertEquals("2023-12-31T23:59:59.999Z", GpxDocumentGenerator.toUtcTimestamp(1_704_067_199_999L))
    }
}
