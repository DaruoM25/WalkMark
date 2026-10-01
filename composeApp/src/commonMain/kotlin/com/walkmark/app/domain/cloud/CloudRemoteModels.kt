package com.walkmark.app.domain.cloud

import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.serialization.Serializable

/**
 * Remote representation of a locally recorded walk.
 *
 * [localId] is the authoritative local identity. [cloudId] is optional provider
 * metadata assigned on first successful upsert. No local filesystem path and no
 * deletion or tombstone state is carried.
 */
@Serializable
data class CloudWalk(
    val localId: String,
    val cloudId: CloudEntityId? = null,
    val title: String,
    val summary: String? = null,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val totalDistanceMeters: Double,
    val durationSeconds: Long,
    val status: WalkStatus
)

/**
 * Remote representation of one recorded GPS sample.
 *
 * [seq] carries the persisted route order explicitly. The local point row id is
 * derived from the walk id and the sequence number, so it is not repeated here.
 */
@Serializable
data class CloudRoutePoint(
    val localWalkId: String,
    val seq: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestampEpochMs: Long,
    val accuracyMeters: Float
)

/**
 * Remote representation of a walk note.
 */
@Serializable
data class CloudWalkNote(
    val localId: String,
    val localWalkId: String,
    val text: String,
    val latitude: Double,
    val longitude: Double,
    val createdAtEpochMs: Long
)

/**
 * Remote representation of a walk photo's metadata.
 *
 * Photo binaries are out of scope. No local filesystem path and no file URI is
 * carried, because neither is resolvable outside the device that owns the file.
 */
@Serializable
data class CloudPhotoMetadata(
    val localId: String,
    val localWalkId: String,
    val latitude: Double,
    val longitude: Double,
    val mimeType: String,
    val byteSize: Long,
    val createdAtEpochMs: Long
)