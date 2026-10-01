package com.walkmark.app.domain.cloud

/**
 * Provider-neutral persistence for recorded route points.
 *
 * Writes are bulk because one walk carries its whole recorded route. An
 * implementation must persist and return points ordered by [CloudRoutePoint.seq].
 */
interface CloudRouteStore {

    suspend fun upsertPoints(
        ownerId: CloudOwnerId,
        points: List<CloudRoutePoint>
    ): CloudResult<Int>

    suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudRoutePoint>>
}