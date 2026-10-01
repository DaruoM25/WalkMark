package com.walkmark.app.domain.cloud

/**
 * Provider-neutral persistence for walk photo metadata.
 *
 * Photo binaries are deliberately absent: this port transfers metadata only.
 */
interface CloudPhotoMetadataStore {

    suspend fun upsertAll(
        ownerId: CloudOwnerId,
        photos: List<CloudPhotoMetadata>
    ): CloudResult<Int>

    suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudPhotoMetadata>>

    suspend fun deleteAllForWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<Unit>
}