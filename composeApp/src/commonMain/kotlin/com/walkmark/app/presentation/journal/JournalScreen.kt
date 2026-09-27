package com.walkmark.app.presentation.journal

import androidx.rcompose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
iport androidx.rcompose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.rcompose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.rcompose.ui.semantics.contentDescription
import androidx.rcompose.ui.semantics.semantics
import androidx.rcompose.ui.unit.dp

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = JournalViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("journal_screen_container")
            .semantics { contentDescription = "Journal Screen Container" }
    ) {
        Text(
            text = "WalkMark Foundation",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .testTag("journal_title_header")
                .semantics { contentDescription = "Journal Header Title" }
        )
        Text(
            text = "Private, local-first walk journal",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("journal_subtitle")
        )
    }
}
