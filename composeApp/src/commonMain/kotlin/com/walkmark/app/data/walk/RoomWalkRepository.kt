package com.walkmark.app.data.walk

import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.core.database.toDomain
import com.walkmark.app.core.database.toEntity
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWalkRepository(
    database: WalkMarkDatabase
) : WalkRepository {

    private val walkDao = database.walkDao()
    private val pointDao = database.walkPointDao()
    private val noteDao = database.walkNoteDao()
    private val photoDao = database.walkPhotoDao()

    override suspend fun startWalk(walkId: String, title: String, startTimeEpochMs: Long): Walk {
        val walk = Walk(
            id = walkId,
            title = title,
            summary = null,
            startTimeEpochMs = startTimeEpochMs,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = WalkStatus.ACTIVE
        )
        walkDao.insert(walk.toEntity())
        return walk
    }

    override suspend fun completeWalk(
        walkId: String,
        endTimeEpochMs: Long,
        totalDistanceMeters: Double
    ): Walk? {
        val existing = walkDao.getById(walkId) ?: return null
        val durationSeconds = ((endTimeEpochMs - existing.startTimeEpochMs).coerceAtLeast(0L)) / 1000L
        val updated = existing.copy(
            endTimeEpochMs = endTimeEpochMs,
            totalDistanceMeters = totalDistanceMeters,
            durationSeconds = durationSeconds,
            status = WalkStatus.COMPLETED.name
        )
        walkDao.update(updated)
        return updated.toDomain()
    }

    override suspend fun appendPoints(walkId: String, points: List<LocationPoint>) {
        if (points.isEmpty()) return
        val startSeq = (pointDao.maxSeq(walkId) ?: -1) + 1
        val entities = points.mapIndexed { index, point ->
            point.toEntity(walkId, startSeq + index)
        }
        pointDao.insertAll(entities)
    }

    override suspend fun addNote(note: WalkNote) {
        noteDao.insert(note.toEntity())
    }

    override suspend fun addPhoto(photo: WalkPhoto) {
        photoDao.insert(photo.toEntity())
    }

    override suspend fun getWalk(walkId: String): Walk? = walkDao.getById(walkId)?.toDomain()

    override suspend fun getActiveWalk(): Walk? =
        walkDao.getFirstByStatus(WalkStatus.ACTIVE.name)?.toDomain()

    override fun observeAllWalks(): Flow<List<Walk>> =
        walkDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActiveWalk(): Flow<Walk?> =
        walkDao.observeFirstByStatus(WalkStatus.ACTIVE.name).map { it?.toDomain() }

    override fun observePoints(walkId: String): Flow<List<LocationPoint>> =
        pointDao.observeByWalk(walkId).map { list -> list.map { it.toDomain() } }

    override fun observeNotes(walkId: String): Flow<List<WalkNote>> =
        noteDao.observeByWalk(walkId).map { list -> list.map { it.toDomain() } }

    override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> =
        photoDao.observeByWalk(walkId).map { list -> list.map { it.toDomain() } }

    override suspend fun deleteWalk(walkId: String) {
        walkDao.deleteById(walkId)
    }
}
