package com.walkmark.app.presentation.journal

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = JournalViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("journal_screen_container")
            .semantics { contentDescription = "Journal Screen Container" }
    ) {
        val isExpanded = maxWidth >= 600.dp

        if (isExpanded) {
            // Adaptive Two-Pane Layout for Tablet / Expanded
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .testTag("adaptive_two_pane_layout")
            ) {
                // Left pane: Walk Overview / Readiness banner
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("expanded_left_pane"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "WalkMark: Ready for Shipaton",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .testTag("app_ready_header")
                                .semantics { contentDescription = "WalkMark: Ready for Shipaton" }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Private, local-first walk journal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier.testTag("journal_subtitle")
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right pane: Secondary / Map / Details placeholder
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("expanded_right_pane"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Walk Journal and Offline Maps",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("expanded_right_pane_title")
                        )
                    }
                }
            }
        } else {
            // Single-Pane Mobile Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .testTag("single_pane_layout")
            ) {
                Text(
                    text = "WalkMark: Ready for Shipaton",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_ready_header")
                        .semantics { contentDescription = "WalkMark: Ready for Shipaton" }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Private, local-first walk journal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_subtitle")
                )
            }
        }
    }
}
