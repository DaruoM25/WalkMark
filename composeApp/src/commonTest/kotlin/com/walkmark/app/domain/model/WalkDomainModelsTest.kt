package com.walkmark.app.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WalkDomainModelsTest {

    @Test
    fun testWalkModelCreationAndProperties() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val later = Instant.fromEpochMilliseconds(1727451600000L)
        val walk = Walk(
            id = "walk_001",
            title = "Sunset Trail",
            summary = "Evening hike near lake",
            startTime = now,
            endTime = later,
            totalDistanceMeters = 3450.5,
            durationSeconds = 3600L
        )

        assertEquals("walk_001", walk.id)
        assertEquals("Sunset Trail", walk.title)
        assertEquals("Evening hike near lake", walk.summary)
        assertEquals(now, walk.startTime)
        assertEquals(later, walk.endTime)
        assertEquals(3450.5, walk.totalDistanceMeters)
        assertEquals(3600L, walk.durationSeconds)
    }

    @Test
    fun testWaypointModelCreationAndProperties() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val wp = Waypoint(
            id = "wp_001",
            walkId = "walk_001",
            latitude = 48.8566,
            longitude = 2.3522,
            altitude = 35.0,
            timestamp = now,
            accuracyMeters = 4.5f
        )

        assertEquals("wp_001", wp.id)
        assertEquals("walk_001", wp.walkId)
        assertEquals(48.8566, wp.latitude)
        assertEquals(2.3522, wp.longitude)
        assertEquals(35.0, wp.altitude)
        assertEquals(now, wp.timestamp)
        assertEquals(4.5f, wp.accuracyMeters)
    }

    @Test
    fun testNotePhotoModelCreationAndProperties() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val np = NotePhoto(
            id = "np_001",
            walkId = "walk_001",
            text = "Spotted a deer",
            photoUri = "file:///data/photos/photo_1.jpg",
            latitude = 48.8567,
            longitude = 2.3523,
            createdAt = now
        )

        assertEquals("np_001", np.id)
        assertEquals("walk_001", np.walkId)
        assertEquals("Spotted a deer", np.text)
        assertEquals("file:///data/photos/photo_1.jpg", np.photoUri)
        assertEquals(48.8567, np.latitude)
        assertEquals(2.3523, np.longitude)
        assertEquals(now, np.createdAt)
    }
}
