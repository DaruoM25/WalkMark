package com.walkmark.app.presentation.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import com.walkmark.app.presentation.map.toMapRouteUiModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface WalkDetailState {
    data object Loading : WalkDetailState
    data object NotFound : WalkDetailState
    data object Error : WalkDetailState
    data class Ready(
        val walk: Walk,
        val points: List<LocationPoint>,
        val notes: List<WalkNote>,
        val photos: List<WalkPhoto>
    ) : WalkDetailState
}

fun deletionNotice(result: WalkDeleteResult): String = when (result) {
    WalkDeleteResult.Success -> "Walk deleted."
    is WalkDeleteResult.SuccessWithMediaCleanupFailures ->
        "Walk deleted, but some photo files could not be removed."
}

@Composable
fun WalkDetailScreen(
    walkId: String,
    repository: WalkRepository,
    mediaStore: LocalMediaStore,
    onBack: () -> Unit,
    onDeleted: (WalkDeleteResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by produceState<WalkDetailState>(WalkDetailState.Loading, repository, walkId) {
        combine(
            repository.observeWalkById(walkId),
            repository.observePoints(walkId),
            repository.observeNotes(walkId),
            repository.observePhotos(walkId)
        ) { walk, points, notes, photos ->
            if (walk == null) WalkDetailState.NotFound
            else WalkDetailState.Ready(walk, points, notes, photos)
        }.catch { value = WalkDetailState.Error }
            .collect { value = it }
    }
    val scope = rememberCoroutineScope()
    var confirmDelete by remember(walkId) { mutableStateOf(false) }
    var deleting by remember(walkId) { mutableStateOf(false) }
    var deleteError by remember(walkId) { mutableStateOf(false) }

    WalkDetailContent(
        state = state,
        mediaStore = mediaStore,
        onBack = onBack,
        onDelete = { confirmDelete = true },
        deleteError = deleteError,
        deleting = deleting,
        modifier = modifier
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete walk?") },
        text = { Text("This removes the saved walk, notes, route and photos. This cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmDelete = false
                    deleting = true
                    scope.launch {
                        try {
                            onDeleted(repository.deleteWalk(walkId))
                        } catch (_: Exception) {
                            deleteError = true
                        } finally {
                            deleting = false
                        }
                    }
                },
                modifier = Modifier.testTag("detail_confirm_delete")
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = { confirmDelete = false }, modifier = Modifier.testTag("detail_cancel_delete")) { Text("Cancel") }
        }
    )
}

@Composable
fun WalkDetailContent(
    state: WalkDetailState,
    mediaStore: LocalMediaStore,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    deleteError: Boolean = false,
    deleting: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(16.dp).testTag("walk_detail_screen")) {
        TextButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) { Text("Back") }
        when (state) {
            WalkDetailState.Loading -> Text("Loading walk…", modifier = Modifier.testTag("detail_loading"))
            WalkDetailState.NotFound -> Text("Walk not found.", modifier = Modifier.testTag("detail_not_found"))
            WalkDetailState.Error -> Text("Walk details could not be loaded.", modifier = Modifier.testTag("detail_error"))
            is WalkDetailState.Ready -> Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.walk.title.takeIf { it.isNotBlank() } ?: "Walk", style = MaterialTheme.typography.headlineMedium)
                Text(formatWalkDateTime(state.walk.startTimeEpochMs))
                Text(formatWalkDistance(state.walk.totalDistanceMeters))
                if (state.walk.status == WalkStatus.COMPLETED) Text(formatWalkDuration(state.walk.durationSeconds))
                else Text("In progress")
                state.walk.summary?.takeIf { it.isNotBlank() }?.let { Text(it) }

                Text("Route", style = MaterialTheme.typography.titleMedium)
                if (state.points.isEmpty()) Text("No route points saved.")
                else WalkRoutePreview(state.points.toMapRouteUiModel())

                Text("Notes", style = MaterialTheme.typography.titleMedium)
                if (state.notes.isEmpty()) Text("No notes saved.")
                state.notes.forEach { note ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(note.text, Modifier.padding(12.dp).testTag("detail_note_${note.id}"))
                    }
                }

                Text("Photos", style = MaterialTheme.typography.titleMedium)
                if (state.photos.isEmpty()) Text("No photos saved.")
                state.photos.forEach { photo ->
                    SavedPhotoThumbnail(
                        absolutePath = mediaStore.absolutePath(photo.relativePath),
                        modifier = Modifier.fillMaxWidth().height(180.dp).testTag("detail_photo_${photo.id}")
                    )
                }

                if (deleteError) Text("Walk could not be deleted.", modifier = Modifier.testTag("detail_delete_error"))
                Button(onClick = onDelete, enabled = !deleting, modifier = Modifier.testTag("detail_delete_button")) {
                    Text(if (deleting) "Deleting…" else "Delete walk")
                }
            }
        }
    }
}
