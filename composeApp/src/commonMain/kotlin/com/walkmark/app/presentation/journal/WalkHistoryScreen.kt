package com.walkmark.app.presentation.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

sealed interface WalkHistoryState {
    data object Loading : WalkHistoryState
    data class Ready(val walks: List<Walk>) : WalkHistoryState
    data object Error : WalkHistoryState
}

@Composable
fun WalkHistoryScreen(
    repository: WalkRepository,
    onOpenWalk: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    notice: String? = null,
    modifier: Modifier = Modifier
) {
    val state by produceState<WalkHistoryState>(WalkHistoryState.Loading, repository) {
        repository.observeAllWalks()
            .catch { value = WalkHistoryState.Error }
            .collect { value = WalkHistoryState.Ready(it) }
    }
    WalkHistoryContent(state, onOpenWalk, onBack, notice, modifier)
}

@Composable
fun WalkHistoryContent(
    state: WalkHistoryState,
    onOpenWalk: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    notice: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(16.dp).testTag("walk_history_screen")) {
        if (onBack != null) TextButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) { Text("Back") }
        Text("Walk history", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("history_title"))
        if (notice != null) Text(notice, modifier = Modifier.testTag("history_notice"))
        when (state) {
            WalkHistoryState.Loading -> Text("Loading walks…", modifier = Modifier.testTag("history_loading"))
            WalkHistoryState.Error -> Text("Walk history could not be loaded.", modifier = Modifier.testTag("history_error"))
            is WalkHistoryState.Ready -> if (state.walks.isEmpty()) {
                Text("No saved walks yet.", modifier = Modifier.testTag("history_empty"))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.walks, key = { it.id }) { walk ->
                        Card(Modifier.fillMaxWidth().clickable { onOpenWalk(walk.id) }.testTag("history_walk_${walk.id}")) {
                            Column(Modifier.padding(16.dp)) {
                                Text(walk.title.takeIf { it.isNotBlank() } ?: "Walk", style = MaterialTheme.typography.titleMedium)
                                Text(formatWalkDateTime(walk.startTimeEpochMs))
                                Text(formatWalkDistance(walk.totalDistanceMeters))
                                if (walk.status == WalkStatus.COMPLETED) Text(formatWalkDuration(walk.durationSeconds))
                                else Text("In progress")
                                walk.summary?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}
