package com.walkmark.app.domain.cloud

/**
 * Provider-neutral persistence for cloud-stored walks.
 *
 * Every operation is explicitly scoped by [CloudOwnerId], so an unscoped read or
 * delete cannot be expressed. An implementation must never widen the caller scope.
 */
interface CloudWalkStore {

    suspend fun upsert(ownerId: CloudOwnerId, walk: CloudWalk): CloudResult<CloudWalk>

    suspend fun get(ownerId: CloudOwnerId, localWalkId: String): CloudResult<CloudWalk?>

    suspend fun listByOwner(ownerId: CloudOwnerId): CloudResult<List<CloudWalk>>

    suspend fun delete(ownerId: CloudOwnerId, localWalkId: String): CloudResult<Unit>
}