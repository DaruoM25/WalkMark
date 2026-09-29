package com.walkmark.app.domain.repository

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
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

    suspend fun addPhoto(photo: WalkPhoto)

    suspend fun getWalk(walkId: String): Walk?

    suspend fun getActiveWalk(): Walk?

    fun observeAllWalks(): Flow<List<Walk>>

    fun observeActiveWalk(): Flow<Walk?>

    fun observePoints(walkId: String): Flow<List<LocationPoint>>

    fun observeNotes(walkId: String): Flow<List<WalkNote>>

    fun observePhotos(walkId: String): Flow<List<WalkPhoto>>

    suspend fun deleteWalk(walkId: String)
}
