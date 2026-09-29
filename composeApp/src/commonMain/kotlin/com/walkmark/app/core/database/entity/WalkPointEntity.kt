package com.walkmark.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "walk_points",
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("walkId")]
)
data class WalkPointEntity(
    @PrimaryKey val id: String,
    val walkId: String,
    val seq: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestampEpochMs: Long,
    val accuracyMeters: Float?
)
