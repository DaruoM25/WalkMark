package com.walkmark.app.core.database

import com.walkmark.app.core.database.entity.WalkEntity
import com.walkmark.app.core.database.entity.WalkNoteEntity
import com.walkmark.app.core.database.entity.WalkPhotoEntity
import com.walkmark.app.core.database.entity.WalkPointEntity
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus

fun WalkEntity.toDomain(): Walk = Walk(
    id = id,
    title = title,
    summary = summary,
    startTimeEpochMs = startTimeEpochMs,
    endTimeEpochMs = endTimeEpochMs,
    totalDistanceMeters = totalDistanceMeters,
    durationSeconds = durationSeconds,
    status = runCatching { WalkStatus.valueOf(status) }.getOrDefault(WalkStatus.ACTIVE)
)

fun Walk.toEntity(): WalkEntity = WalkEntity(
    id = id,
    title = title,
    summary = summary,
    startTimeEpochMs = startTimeEpochMs,
    endTimeEpochMs = endTimeEpochMs,
    totalDistanceMeters = totalDistanceMeters,
    durationSeconds = durationSeconds,
    status = status.name
)

fun WalkPointEntity.toDomain(): LocationPoint = LocationPoint(
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    timestamp = timestampEpochMs,
    accuracy = accuracyMeters ?: 0f
)

fun LocationPoint.toEntity(walkId: String, seq: Int): WalkPointEntity = WalkPointEntity(
    id = "$walkId-p-$seq",
    walkId = walkId,
    seq = seq,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    timestampEpochMs = timestamp,
    accuracyMeters = accuracy
)

fun WalkNoteEntity.toDomain(): WalkNote = WalkNote(
    id = id,
    walkId = walkId,
    text = text,
    latitude = latitude,
    longitude = longitude,
    createdAtEpochMs = createdAtEpochMs
)

fun WalkNote.toEntity(): WalkNoteEntity = WalkNoteEntity(
    id = id,
    walkId = walkId,
    text = text,
    latitude = latitude,
    longitude = longitude,
    createdAtEpochMs = createdAtEpochMs
)

fun WalkPhotoEntity.toDomain(): WalkPhoto = WalkPhoto(
    id = id,
    walkId = walkId,
    latitude = latitude,
    longitude = longitude,
    relativePath = relativePath,
    mimeType = mimeType,
    byteSize = byteSize,
    createdAtEpochMs = createdAtEpochMs
)

fun WalkPhoto.toEntity(): WalkPhotoEntity = WalkPhotoEntity(
    id = id,
    walkId = walkId,
    latitude = latitude,
    longitude = longitude,
    relativePath = relativePath,
    mimeType = mimeType,
    byteSize = byteSize,
    createdAtEpochMs = createdAtEpochMs
)
