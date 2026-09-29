package com.walkmark.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "walk_photos",
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
data class WalkPhotoEntity(
    @PrimaryKey val id: String,
    val walkId: String,
    val latitude: Double,
    val longitude: Double,
    val relativePath: String,
    val mimeType: String,
    val byteSize: Long,
    val createdAtEpochMs: Long
)
