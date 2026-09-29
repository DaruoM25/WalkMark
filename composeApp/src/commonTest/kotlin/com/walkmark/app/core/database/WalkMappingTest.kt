package com.walkmark.app.core.database

import com.walkmark.app.core.database.entity.WalkEntity
import com.walkmark.app.core.database.entity.WalkPointEntity
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WalkMappingTest {

    @Test
    fun walkEntityRoundTripsAllFields() {
        val walk = WalkEntity(
            id = "walk-1",
            title = "Morning loop",
            summary = "Nice weather",
            startTimeEpochMs = 1_700_000_000_000L,
            endTimeEpochMs = 1_700_003_600_000L,
            totalDistanceMeters = 4321.5,
            durationSeconds = 3600L,
            status = WalkStatus.COMPLETED.name
        )

        val domain = walk.toDomain()

        assertEquals("walk-1", domain.id)
        assertEquals("Morning loop", domain.title)
        assertEquals("Nice weather", domain.summary)
        assertEquals(1_700_000_000_000L, domain.startTimeEpochMs)
        assertEquals(1_700_003_600_000L, domain.endTimeEpochMs)
        assertEquals(4321.5, domain.totalDistanceMeters)
        assertEquals(3600L, domain.durationSeconds)
        assertEquals(WalkStatus.COMPLETED, domain.status)
        assertEquals(walk, domain.toEntity())
    }

    @Test
    fun activeWalkWithNullsMapsCorrectly() {
        val walk = WalkEntity(
            id = "walk-2",
            title = "Walk",
            summary = null,
            startTimeEpochMs = 1L,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = WalkStatus.ACTIVE.name
        )

        val domain = walk.toDomain()

        assertNull(domain.summary)
        assertNull(domain.endTimeEpochMs)
        assertEquals(WalkStatus.ACTIVE, domain.status)
    }

    @Test
    fun unknownStatusFallsBackToActiveInsteadOfThrowing() {
        val walk = WalkEntity(
            id = "walk-3",
            title = "Walk",
            summary = null,
            startTimeEpochMs = 1L,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = "SOMETHING_ELSE"
        )

        assertEquals(WalkStatus.ACTIVE, walk.toDomain().status)
    }

    @Test
    fun locationPointMapsToEntityWithSeqAndWalkId() {
        val point = LocationPoint(
            latitude = 48.8584,
            longitude = 2.2945,
            altitude = 35.0,
            timestamp = 1_700_000_000_000L,
            accuracy = 4.5f
        )

        val entity = point.toEntity(walkId = "walk-1", seq = 7)

        assertEquals("walk-1-p-7", entity.id)
        assertEquals("walk-1", entity.walkId)
        assertEquals(7, entity.seq)
        assertEquals(48.8584, entity.latitude)
        assertEquals(2.2945, entity.longitude)
        assertEquals(35.0, entity.altitude)
        assertEquals(1_700_000_000_000L, entity.timestampEpochMs)
        assertEquals(4.5f, entity.accuracyMeters)
    }

    @Test
    fun locationPointWithoutAltitudeMapsNullAltitude() {
        val point = LocationPoint(
            latitude = 1.0,
            longitude = 2.0,
            altitude = null,
            timestamp = 99L,
            accuracy = 10f
        )

        val entity = point.toEntity(walkId = "w", seq = 0)

        assertNull(entity.altitude)
        assertNull(entity.toDomain().altitude)
    }

    @Test
    fun locationPointRoundTripsThroughEntity() {
        val point = LocationPoint(48.0, 2.0, 10.0, 1234L, 3f)

        assertEquals(point, point.toEntity("w", 0).toDomain())
    }

    @Test
    fun pointEntityWithNullAccuracyMapsToZero() {
        val entity = WalkPointEntity(
            id = "w-p-0",
            walkId = "w",
            seq = 0,
            latitude = 1.0,
            longitude = 2.0,
            altitude = null,
            timestampEpochMs = 5L,
            accuracyMeters = null
        )

        assertEquals(0f, entity.toDomain().accuracy)
    }

    @Test
    fun noteEntityRoundTripsAllFields() {
        val note = WalkNote(
            id = "note-1",
            walkId = "walk-1",
            text = "Bench near the fountain",
            latitude = 48.8584,
            longitude = 2.2945,
            createdAtEpochMs = 1_700_000_000_000L
        )

        val entity = note.toEntity()

        assertEquals("note-1", entity.id)
        assertEquals("walk-1", entity.walkId)
        assertEquals("Bench near the fountain", entity.text)
        assertEquals(note, entity.toDomain())
    }

    @Test
    fun noteExposesCoordinate() {
        val note = WalkNote("n", "w", "text", 10.5, 20.5, 1L)

        assertEquals(10.5, note.coordinate.latitude)
        assertEquals(20.5, note.coordinate.longitude)
    }

    @Test
    fun photoEntityRoundTripsAllFields() {
        val photo = WalkPhoto(
            id = "photo-1",
            walkId = "walk-1",
            latitude = 48.86,
            longitude = 2.30,
            relativePath = "walk-1/abc.jpg",
            mimeType = "image/jpeg",
            byteSize = 4096L,
            createdAtEpochMs = 1_700_000_001_000L
        )

        val entity = photo.toEntity()

        assertEquals("walk-1/abc.jpg", entity.relativePath)
        assertEquals("image/jpeg", entity.mimeType)
        assertEquals(4096L, entity.byteSize)
        assertEquals(photo, entity.toDomain())
    }

    @Test
    fun photoRelativePathIsRelativeNotAbsolute() {
        val photo = WalkPhoto("p", "w", 1.0, 2.0, "w/abc.jpg", "image/jpeg", 1L, 1L)

        val stored = photo.toEntity().relativePath

        assertTrue(!stored.startsWith("/"))
        assertTrue(!stored.contains("://"))
        assertEquals("w/abc.jpg", stored)
    }
}
