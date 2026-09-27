package com.walkmark.app.data.model

import com.walkmark.app.domain.model.NotePhoto
import com.walkmark.app.domain.model.Walk
import com.walkmark.app.domain.model.Waypoint
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WalkDto(
    val id: String,
    val title: String,
    val summary: String? = null,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val totalDistanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L
) {
    fun toDomain(): Walk = Walk(
        id = id,
        title = title,
        summary = summary,
        startTime = Instant.fromEpochMilliseconds(startTimeEpochMs),
        endTime = endTimeEpochMs?.let { Instant.fromEpochMilliseconds(it) },
        totalDistanceMeters = totalDistanceMeters,
        durationSeconds = durationSeconds
    )

    companion object {
        fun fromDomain(walk: Walk): WalkDto = WalkDto(
            id = walk.id,
            title = walk.title,
            summary = walk.summary,
            startTimeEpochMs = walk.startTime.toEpochMilliseconds(),
            endTimeEpochMs = walk.endTime?.toEpochMilliseconds(),
            totalDistanceMeters = walk.totalDistanceMeters,
            durationSeconds = walk.durationSeconds
        )
    }
}

@Serializable
data class WaypointDto(
    val id: String,
    val walkId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestampEpochMs: Long,
    val accuracyMeters: Float? = null
) {
    fun toDomain(): Waypoint = Waypoint(
        id = id,
        walkId = walkId,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        timestamp = Instant.fromEpochMilliseconds(timestampEpochMs),
        accuracyMeters = accuracyMeters
    )

    companion object {
        fun fromDomain(wp: Waypoint): WaypointDto = WaypointDto(
            id = wp.id,
            walkId = wp.walkId,
            latitude = wp.latitude,
            longitude = wp.longitude,
            altitude = wp.altitude,
            timestampEpochMs = wp.timestamp.toEpochMilliseconds(),
            accuracyMeters = wp.accuracyMeters
        )
    }
}

@Serializable
data class NotePhotoDto(
    val id: String,
    val walkId: String,
    val text: String? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAtEpochMs: Long
) {
    fun toDomain(): NotePhoto = NotePhoto(
        id = id,
        walkId = walkId,
        text = text,
        photoUri = photoUri,
        latitude = latitude,
        longitude = longitude,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs)
    )

    companion object {
        fun fromDomain(np: NotePhoto): NotePhotoDto = NotePhotoDto(
            id = np.id,
            walkId = np.walkId,
            text = np.text,
            photoUri = np.photoUri,
            latitude = np.latitude,
            longitude = np.longitude,
            createdAtEpochMs = np.createdAt.toEpochMilliseconds()
        )
    }
}
