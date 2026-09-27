package com.walkmark.app.presentation.journal

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = JournalViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "WalkMark: Ready for Shipaton",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .testTag("app_ready_header")
                                .semantics { contentDescription = "WalkMark: Ready for Shipaton" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Private, local-first walk journal",
                            style = MaterialTheme.typography.bodyMedium,
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
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Walk Journal & Offline Maps",
                            style = MaterialTheme.typography.titleMedium,
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
                    .padding(16.dp)
                    .testTag("single_pane_layout")
            ) {
                Text(
                    text = "WalkMark: Ready for Shipaton",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .testTag("app_ready_header")
                        .semantics { contentDescription = "WalkMark: Ready for Shipaton" }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Private, local-first walk journal",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("journal_subtitle")
                )
            }
        }
    }
}
