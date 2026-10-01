package com.walkmark.app.domain.cloud

/**
 * Provider-neutral persistence for cloud-stored walk notes.
 */
interface CloudNoteStore {

    suspend fun upsert(ownerId: CloudOwnerId, note: CloudWalkNote): CloudResult<CloudWalkNote>

    suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudWalkNote>>

    suspend fun delete(ownerId: CloudOwnerId, localId: String): CloudResult<Unit>
}