package com.walkmark.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String?,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long?,
    val totalDistanceMeters: Double,
    val durationSeconds: Long,
    val status: String
)
