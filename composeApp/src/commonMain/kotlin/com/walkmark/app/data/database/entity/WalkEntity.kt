package com.walkmark.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.walkmark.app.domain.model.Walk
import kotlinx.datetime.Instant

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey
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
        fun fromDomain(walk: Walk): WalkEntity = WalkEntity(
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
