package com.walkmark.app.data.walk

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [WalkRepository] implementation for unit tests.
 * Supports optional [LocalMediaStore] injection to verify media cleanup behaviour.
 */
class InMemoryWalkRepository(
    private val mediaStore: LocalMediaStore? = null
) : WalkRepository {

    private val walks = MutableStateFlow<Map<String, Walk>>(emptyMap())
    private val points = MutableStateFlow<Map<String, List<LocationPoint>>>(emptyMap())
    private val notes = MutableStateFlow<Map<String, List<WalkNote>>>(emptyMap())
    private val photos = MutableStateFlow<Map<String, List<WalkPhoto>>>(emptyMap())

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
        walks.update { it + (walkId to walk) }
        return walk
    }

    override suspend fun completeWalk(
        walkId: String,
        endTimeEpochMs: Long,
        totalDistanceMeters: Double
    ): Walk? {
        val existing = walks.value[walkId] ?: return null
        val durationSeconds = ((endTimeEpochMs - existing.startTimeEpochMs).coerceAtLeast(0L)) / 1000L
        val updated = existing.copy(
            endTimeEpochMs = endTimeEpochMs,
            totalDistanceMeters = totalDistanceMeters,
            durationSeconds = durationSeconds,
            status = WalkStatus.COMPLETED
        )
        walks.update { it + (walkId to updated) }
        return updated
    }

    override suspend fun appendPoints(walkId: String, points: List<LocationPoint>) {
        this.points.update { map ->
            map + (walkId to ((map[walkId] ?: emptyList()) + points))
        }
    }

    override suspend fun addNote(note: WalkNote) {
        notes.update { map ->
            map + (note.walkId to ((map[note.walkId] ?: emptyList()) + note))
        }
    }

    override suspend fun addPhoto(photo: WalkPhoto) {
        photos.update { map ->
            map + (photo.walkId to ((map[photo.walkId] ?: emptyList()) + photo))
        }
    }

    override suspend fun getWalk(walkId: String): Walk? = walks.value[walkId]

    override suspend fun getActiveWalk(): Walk? =
        walks.value.values.firstOrNull { it.status == WalkStatus.ACTIVE }

    override fun observeAllWalks(): Flow<List<Walk>> =
        walks.map { it.values.sortedByDescending { w -> w.startTimeEpochMs } }

    override fun observeActiveWalk(): Flow<Walk?> =
        walks.map { it.values.firstOrNull { w -> w.status == WalkStatus.ACTIVE } }

    override fun observeWalkById(walkId: String): Flow<Walk?> =
        walks.map { it[walkId] }

    override fun observePoints(walkId: String): Flow<List<LocationPoint>> =
        points.map { it[walkId] ?: emptyList() }

    override fun observeNotes(walkId: String): Flow<List<WalkNote>> =
        notes.map { it[walkId] ?: emptyList() }

    override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> =
        photos.map { it[walkId] ?: emptyList() }

    override suspend fun deleteWalk(walkId: String): WalkDeleteResult {
        val failedPaths = mutableListOf<String>()

        if (mediaStore != null) {
            val walkPhotos = photos.value[walkId] ?: emptyList()
            for (photo in walkPhotos) {
                try {
                    mediaStore.deletePhoto(photo.relativePath)
                } catch (_: Throwable) {
                    failedPaths += photo.relativePath
                }
            }
        }

        walks.update { it - walkId }
        photos.update { it - walkId }
        notes.update { it - walkId }
        points.update { it - walkId }

        return if (failedPaths.isEmpty()) {
            WalkDeleteResult.Success
        } else {
            WalkDeleteResult.SuccessWithMediaCleanupFailures(failedPaths)
        }
    }
}