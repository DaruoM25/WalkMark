package com.walkmark.app.domain.export.structured

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class WalkExportDto(
    val schemaVersion: Int = 1,
    val id: String,
    val title: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val isAutoPaused: Boolean = false,
    val isClosedPrematurely: Boolean = false,
    val routePoints: List<RoutePointExportDto>,
    val notes: List<NoteExportDto>,
    val photos: List<PhotoExportDto>
)

@Serializable
internal data class RoutePointExportDto(
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestamp: Long,
    val accuracy: Float?
)

@Serializable
internal data class NoteExportDto(
    val id: String,
    val content: String,
    val createdAt: Long,
    val latitude: Double?,
    val longitude: Double?
)

@Serializable
internal data class PhotoExportDto(
    val id: String,
    val mimeType: String,
    val byteSize: Long,
    val createdAt: Long,
    val latitude: Double?,
    val longitude: Double?
)

object WalkStructuredExportGenerator {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun generateJson(
        walk: Walk,
        points: List<LocationPoint>,
        notes: List<WalkNote>,
        photos: List<WalkPhoto>
    ): String {
        val dto = WalkExportDto(
            schemaVersion = 1,
            id = walk.id,
            title = walk.title,
            status = walk.status.name,
            startTime = walk.startTimeEpochMs,
            endTime = walk.endTimeEpochMs,
            durationSeconds = walk.durationSeconds,
            distanceMeters = walk.totalDistanceMeters,
            isAutoPaused = false,
            isClosedPrematurely = false,
            routePoints = points.mapIndexed { index, point ->
                RoutePointExportDto(
                    sequence = index,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    altitude = point.altitude,
                    timestamp = point.timestamp,
                    accuracy = if (point.accuracy > 0f) point.accuracy else null
                )
            },
            notes = notes.map { note ->
                NoteExportDto(
                    id = note.id,
                    content = note.text,
                    createdAt = note.createdAtEpochMs,
                    latitude = note.latitude,
                    longitude = note.longitude
                )
            },
            photos = photos.map { photo ->
                PhotoExportDto(
                    id = photo.id,
                    mimeType = photo.mimeType,
                    byteSize = photo.byteSize,
                    createdAt = photo.createdAtEpochMs,
                    latitude = photo.latitude,
                    longitude = photo.longitude
                )
            }
        )
        return json.encodeToString(dto)
    }

    fun generateCsv(points: List<LocationPoint>): String {
        val sb = StringBuilder()
        sb.append("sequence,latitude,longitude,altitude,timestamp,accuracy\n")
        points.forEachIndexed { index, point ->
            val seq = index.toString()
            val lat = formatDouble(point.latitude)
            val lon = formatDouble(point.longitude)
            val alt = formatDouble(point.altitude)
            val ts = point.timestamp.toString()
            val acc = if (point.accuracy > 0f) formatFloat(point.accuracy) else ""

            sb.append(seq).append(',')
                .append(lat).append(',')
                .append(lon).append(',')
                .append(alt).append(',')
                .append(ts).append(',')
                .append(acc).append('\n')
        }
        return sb.toString()
    }

    private fun formatDouble(value: Double?): String {
        if (value == null || !value.isFinite()) return ""
        val s = value.toString()
        return if (s.contains(',')) s.replace(',', '.') else s
    }

    private fun formatFloat(value: Float?): String {
        if (value == null || !value.isFinite()) return ""
        val s = value.toString()
        return if (s.contains(',')) s.replace(',', '.') else s
    }
}
