package com.walkmark.app.data.walk

import com.walkmark.app.core.time.currentTimeEpochMillis
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.repository.WalkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random

class WalkSessionRecorder(
    private val locationRepository: LocationRepository,
    private val walkRepository: WalkRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = { currentTimeEpochMillis() },
    private val idGenerator: () -> String = { "walk-${clock()}-${Random.nextLong().toString(16)}" },
    private val titleProvider: (Long) -> String = { "Walk" }
) {

    private var recordingJob: Job? = null

    fun start() {
        if (recordingJob != null) return
        recordingJob = scope.launch { observeSession() }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
    }

    private suspend fun observeSession() {
        var currentWalkId: String? = null
        var lastPersistedIndex = 0

        combine(
            locationRepository.trackingState,
            locationRepository.acceptedPoints
        ) { state, points -> state to points }
            .collect { (state, points) ->
                when (state) {
                    is LocationTrackingState.Tracking -> {
                        if (currentWalkId == null) {
                            val walkId = idGenerator()
                            val startedAtEpochMs = clock()
                            walkRepository.startWalk(walkId, titleProvider(startedAtEpochMs), startedAtEpochMs)
                            currentWalkId = walkId
                            lastPersistedIndex = points.size
                        }
                        currentWalkId?.let { walkId ->
                            lastPersistedIndex = persistNewPoints(walkId, points, lastPersistedIndex)
                        }
                    }

                    is LocationTrackingState.Paused -> {
                        currentWalkId?.let { walkId ->
                            lastPersistedIndex = persistNewPoints(walkId, points, lastPersistedIndex)
                        }
                    }

                    is LocationTrackingState.Idle -> {
                        currentWalkId?.let { walkId ->
                            lastPersistedIndex = persistNewPoints(walkId, points, lastPersistedIndex)
                            walkRepository.completeWalk(
                                walkId = walkId,
                                endTimeEpochMs = clock(),
                                totalDistanceMeters = locationRepository.totalDistanceMeters.value
                            )
                        }
                        currentWalkId = null
                        lastPersistedIndex = 0
                    }

                    is LocationTrackingState.Error -> Unit
                }
            }
    }

    private suspend fun persistNewPoints(
        walkId: String,
        points: List<LocationPoint>,
        lastPersistedIndex: Int
    ): Int {
        if (points.size <= lastPersistedIndex) return lastPersistedIndex
        val fresh = points.subList(lastPersistedIndex, points.size).toList()
        walkRepository.appendPoints(walkId, fresh)
        return points.size
    }
}
