package com.walkmark.app.data.walk

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RecordingWalkRepository : WalkRepository {

    data class StartCall(val walkId: String, val title: String, val startTimeEpochMs: Long)
    data class CompleteCall(
        val walkId: String,
        val endTimeEpochMs: Long,
        val totalDistanceMeters: Double
    )

    val startCalls = mutableListOf<StartCall>()
    val completeCalls = mutableListOf<CompleteCall>()
    val appendedPoints = mutableListOf<Pair<String, List<LocationPoint>>>()
    val notes = mutableListOf<WalkNote>()
    val photos = mutableListOf<WalkPhoto>()

    override suspend fun startWalk(walkId: String, title: String, startTimeEpochMs: Long): Walk {
        startCalls += StartCall(walkId, title, startTimeEpochMs)
        return Walk(
            id = walkId,
            title = title,
            summary = null,
            startTimeEpochMs = startTimeEpochMs,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = WalkStatus.ACTIVE
        )
    }

    override suspend fun completeWalk(
        walkId: String,
        endTimeEpochMs: Long,
        totalDistanceMeters: Double
    ): Walk? {
        completeCalls += CompleteCall(walkId, endTimeEpochMs, totalDistanceMeters)
        return null
    }

    override suspend fun appendPoints(walkId: String, points: List<LocationPoint>) {
        appendedPoints += walkId to points
    }

    override suspend fun addNote(note: WalkNote) {
        notes += note
    }

    override suspend fun addPhoto(photo: WalkPhoto) {
        photos += photo
    }

    override suspend fun getWalk(walkId: String): Walk? = null
    override suspend fun getActiveWalk(): Walk? = null
    override fun observeAllWalks(): Flow<List<Walk>> = flowOf(emptyList())
    override fun observeActiveWalk(): Flow<Walk?> = flowOf(null)
    override fun observePoints(walkId: String): Flow<List<LocationPoint>> = flowOf(emptyList())
    override fun observeNotes(walkId: String): Flow<List<WalkNote>> = flowOf(emptyList())
    override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> = flowOf(emptyList())
    override suspend fun deleteWalk(walkId: String) = Unit
}
