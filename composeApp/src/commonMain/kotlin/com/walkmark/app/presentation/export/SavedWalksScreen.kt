package com.walkmark.app.presentation.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.export.GpxDocumentGenerator
import com.walkmark.app.domain.walk.Walk
import org.jetbrains.compose.resources.stringResource
import walkmark.composeapp.generated.resources.Res
import walkmark.composeapp.generated.resources.export_gpx_button
import walkmark.composeapp.generated.resources.export_gpx_error_no_app
import walkmark.composeapp.generated.resources.export_gpx_error_share_failed
import walkmark.composeapp.generated.resources.export_gpx_error_walk_not_found
import walkmark.composeapp.generated.resources.export_gpx_exporting
import walkmark.composeapp.generated.resources.export_gpx_no_walks
import walkmark.composeapp.generated.resources.export_gpx_success
import walkmark.composeapp.generated.resources.saved_walks_back
import walkmark.composeapp.generated.resources.saved_walks_entry
import walkmark.composeapp.generated.resources.saved_walks_title

/**
 * Lists locally persisted walks and offers a per-walk GPX export action.
 *
 * Offline, account-free, and read-only: exporting only serialises already-stored data.
 * The screen performs no navigation of its own beyond the injected [onBack].
 */
@Composable
fun SavedWalksScreen(
    viewModel: SavedWalksViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walks by viewModel.walks.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val exporting = exportState is WalkExportState.Exporting

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("saved_walks_screen")
            .semantics { contentDescription = "Saved Walks Screen" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("saved_walks_back_button")
                        .semantics { contentDescription = "Back from Saved Walks" }
                ) {
                    Text(stringResource(Res.string.saved_walks_back))
                }
                Text(
                    text = stringResource(Res.string.saved_walks_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.testTag("saved_walks_title")
                )
            }

            Spacer(Modifier.height(12.dp))

            when (val state = exportState) {
                is WalkExportState.Success -> Text(
                    text = stringResource(Res.string.export_gpx_success, state.fileName),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("export_gpx_success")
                )

                is WalkExportState.Error -> Text(
                    text = stringResource(state.reason.messageResource()),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("export_gpx_error")
                )

                else -> Unit
            }

            if (exporting) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.export_gpx_exporting),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("export_gpx_exporting")
                )
            }

            Spacer(Modifier.height(16.dp))

            if (walks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.export_gpx_no_walks),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("saved_walks_empty")
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    walks.forEach { walk ->
                        SavedWalkCard(
                            walk = walk,
                            exporting = exporting,
                            onExport = { viewModel.exportGpx(walk.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedWalkCard(
    walk: Walk,
    exporting: Boolean,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_walk_card_${walk.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = walk.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("saved_walk_title_${walk.id}")
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = walk.startDateLabel(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("saved_walk_started_at_${walk.id}")
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onExport,
                enabled = !exporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_gpx_button_${walk.id}")
                    .semantics { contentDescription = "Export GPX for ${walk.title}" }
            ) {
                Text(stringResource(Res.string.export_gpx_button))
            }
        }
    }
}

private fun WalkExportError.messageResource() = when (this) {
    WalkExportError.WalkNotFound -> Res.string.export_gpx_error_walk_not_found
    WalkExportError.NoCompatibleApp -> Res.string.export_gpx_error_no_app
    WalkExportError.ShareFailed -> Res.string.export_gpx_error_share_failed
}

/** Locale-independent `yyyy-MM-dd` UTC label; blank when the stored time is unusable. */
private fun Walk.startDateLabel(): String =
    GpxDocumentGenerator.toUtcTimestamp(startTimeEpochMs)?.substringBefore('T').orEmpty()
