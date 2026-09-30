package com.walkmark.app.domain.repository

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import kotlinx.coroutines.flow.Flow

interface WalkRepository {
    suspend fun startWalk(walkId: String, title: String, startTimeEpochMs: Long): Walk

    suspend fun completeWalk(
        walkId: String,
        endTimeEpochMs: Long,
        totalDistanceMeters: Double
    ): Walk?

    suspend fun appendPoints(walkId: String, points: List<LocationPoint>)

    suspend fun addNote(note: WalkNote)

    /** Inserts the photo DB record. On DB failure, attempts media cleanup then rethrows. */
    suspend fun addPhoto(photo: WalkPhoto)

    suspend fun getWalk(walkId: String): Walk?

    suspend fun getActiveWalk(): Walk?

    fun observeAllWalks(): Flow<List<Walk>>

    fun observeActiveWalk(): Flow<Walk?>

    /** Observe a single walk by id; emits null when the walk does not exist. */
    fun observeWalkById(walkId: String): Flow<Walk?>

    fun observePoints(walkId: String): Flow<List<LocationPoint>>

    fun observeNotes(walkId: String): Flow<List<WalkNote>>

    fun observePhotos(walkId: String): Flow<List<WalkPhoto>>

    /**
     * Deletes the walk and attempts to remove all associated media files.
     * The DB record is always removed. Media cleanup failures are collected and
     * returned as [WalkDeleteResult.SuccessWithMediaCleanupFailures].
     */
    suspend fun deleteWalk(walkId: String): WalkDeleteResult
}
