package com.walkmark.app.data.model

import com.walkmark.app.domain.model.NotePhoto
import com.walkmark.app.domain.model.Walk
import com.walkmark.app.domain.model.Waypoint
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationAndMappingTest {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    @Test
    fun testWalkSerializationAndMapping() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val later = Instant.fromEpochMilliseconds(1727451600000L)
        val original = Walk(
            id = "walk_ser_1",
            title = "Mountain Trail",
            summary = "Fresh air walk",
            startTime = now,
            endTime = later,
            totalDistanceMeters = 5200.0,
            durationSeconds = 3600L
        )

        val dto = WalkDto.fromDomain(original)
        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<WalkDto>(encoded)
        val reconstructed = decoded.toDomain()

        assertEquals(original, reconstructed)
        assertEquals(1727448000000L, dto.startTimeEpochMs)
        assertEquals(1727451600000L, dto.endTimeEpochMs)
    }

    @Test
    fun testWaypointSerializationAndMapping() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val original = Waypoint(
            id = "wp_ser_1",
            walkId = "walk_ser_1",
            latitude = 45.1885,
            longitude = 5.7245,
            altitude = 212.0,
            timestamp = now,
            accuracyMeters = 3.0f
        )

        val dto = WaypointDto.fromDomain(original)
        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<WaypointDto>(encoded)
        val reconstructed = decoded.toDomain()

        assertEquals(original, reconstructed)
    }

    @Test
    fun testNotePhotoSerializationAndMapping() {
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val original = NotePhoto(
            id = "np_ser_1",
            walkId = "walk_ser_1",
            text = "Nice scenic viewpoint",
            photoUri = "file:///storage/photos/view.jpg",
            latitude = 45.1890,
            longitude = 5.7250,
            createdAt = now
        )

        val dto = NotePhotoDto.fromDomain(original)
        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<NotePhotoDto>(encoded)
        val reconstructed = decoded.toDomain()

        assertEquals(original, reconstructed)
    }
}
