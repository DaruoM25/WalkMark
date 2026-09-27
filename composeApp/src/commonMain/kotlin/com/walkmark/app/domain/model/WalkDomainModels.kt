package com.walkmark.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Walk(
    val id: String,
    val title: String,
    val summary: String? = null,
    val startTime: Instant,
    val endTime: Instant? = null,
    val totalDistanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L
)

@Serializable
data class Waypoint(
    val id: String,
    val walkId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Instant,
    val accuracyMeters: Float? = null
)

@Serializable
data class NotePhoto(
    val id: String,
    val walkId: String,
    val text: String? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Instant
)
