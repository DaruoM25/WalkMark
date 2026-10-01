package com.walkmark.app.presentation.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("walk_history_screen")
            .semantics { contentDescription = "Walk History" }
    ) {
        if (onBack != null) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("history_back_button")
            ) {
                Text("Back")
            }
        }

        if (notice != null) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("history_notice")
                )
            }
        }

        when (state) {
            WalkHistoryState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("history_loading")
                    )
                }
            }
            WalkHistoryState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Walk history could not be loaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("history_error")
                    )
                }
            }
            is WalkHistoryState.Ready -> if (state.walks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No saved walks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("history_empty")
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Start your first walk from the Home tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.walks, key = { it.id }) { walk ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenWalk(walk.id) }
                                .testTag("history_walk_${walk.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = walk.title.takeIf { it.isNotBlank() } ?: "Walk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatWalkDateTime(walk.startTimeEpochMs),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatWalkDistance(walk.totalDistanceMeters),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (walk.status == WalkStatus.COMPLETED) {
                                        formatWalkDuration(walk.durationSeconds)
                                    } else {
                                        "In progress"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (walk.status == WalkStatus.COMPLETED) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                walk.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
