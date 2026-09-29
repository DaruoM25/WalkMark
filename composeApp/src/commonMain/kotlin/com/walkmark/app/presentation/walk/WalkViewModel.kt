package com.walkmark.app.presentation.walk

import com.walkmark.app.core.time.currentTimeEpochMillis
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.presentation.map.MapUiState
import com.walkmark.app.presentation.map.toMapMarkerUiModel
import com.walkmark.app.presentation.map.toMapRouteUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface WalkActionState {
    data object Idle : WalkActionState
    data object InProgress : WalkActionState
    data class Success(val message: String) : WalkActionState
    data class Error(val message: String) : WalkActionState
}

class WalkViewModel(
    private val walkRepository: WalkRepository,
    private val mediaStore: LocalMediaStore,
    private val locationRepository: LocationRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = { currentTimeEpochMillis() },
    private val idGenerator: () -> String = { "item-${clock()}-${Random.nextLong().toString(16)}" }
) {

    private val _actionState = MutableStateFlow<WalkActionState>(WalkActionState.Idle)
    val actionState: StateFlow<WalkActionState> = _actionState.asStateFlow()

    val uiState: StateFlow<MapUiState> = walkRepository.observeActiveWalk()
        .flatMapLatest { walk ->
            if (walk == null) {
                flowOf(MapUiState.Idle)
            } else {
                combine(
                    walkRepository.observePoints(walk.id),
                    walkRepository.observeNotes(walk.id),
                    walkRepository.observePhotos(walk.id),
                    locationRepository.totalDistanceMeters
                ) { points, notes, photos, distanceMeters ->
                    MapUiState.Active(
                        walkId = walk.id,
                        route = points.toMapRouteUiModel(),
                        markers = notes.map { it.toMapMarkerUiModel() } +
                            photos.map { it.toMapMarkerUiModel() },
                        totalDistanceMeters = distanceMeters
                    )
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), MapUiState.Loading)

    fun resetActionState() {
        _actionState.value = WalkActionState.Idle
    }

    fun addNote(text: String) {
        if (text.isBlank()) {
            _actionState.value = WalkActionState.Error("Note text cannot be empty")
            return
        }
        val location = locationRepository.lastLocation.value
        if (location == null) {
            _actionState.value = WalkActionState.Error("No location fix available yet")
            return
        }
        val trimmedText = text.trim()

        scope.launch {
            _actionState.value = try {
                val walkId = walkRepository.getActiveWalk()?.id
                if (walkId == null) {
                    WalkActionState.Error("No active walk")
                } else {
                    walkRepository.addNote(
                        WalkNote(
                            id = idGenerator(),
                            walkId = walkId,
                            text = trimmedText,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            createdAtEpochMs = clock()
                        )
                    )
                    WalkActionState.Success("Note saved")
                }
            } catch (error: Throwable) {
                WalkActionState.Error(error.message ?: "Unable to save note")
            }
        }
    }

    fun addPhoto(sourceUri: String) {
        _actionState.value = WalkActionState.InProgress
        scope.launch {
            _actionState.value = try {
                val walkId = walkRepository.getActiveWalk()?.id
                val location = locationRepository.lastLocation.value
                if (walkId == null || location == null) {
                    WalkActionState.Error("No active walk or location fix")
                } else {
                    val stored = mediaStore.importPhoto(sourceUri, walkId)
                    walkRepository.addPhoto(
                        WalkPhoto(
                            id = idGenerator(),
                            walkId = walkId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            relativePath = stored.relativePath,
                            mimeType = stored.mimeType,
                            byteSize = stored.byteSize,
                            createdAtEpochMs = clock()
                        )
                    )
                    WalkActionState.Success("Photo saved")
                }
            } catch (error: Throwable) {
                WalkActionState.Error(error.message ?: "Unable to save photo")
            }
        }
    }
}
