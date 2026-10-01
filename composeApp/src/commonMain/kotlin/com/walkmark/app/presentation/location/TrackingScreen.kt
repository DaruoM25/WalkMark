package com.walkmark.app.presentation.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.location.LocationTrackingState

@Composable
fun TrackingScreen(
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isStartingWalk by viewModel.isStartingWalk.collectAsState()

    val permissionLauncher = rememberLocationPermissionLauncher(
        onPermissionGranted = {
            viewModel.startTracking()
        }
    )

    val statusText = when (val state = uiState.state) {
        is LocationTrackingState.Idle -> "Idle"
        is LocationTrackingState.Tracking -> "Tracking"
        is LocationTrackingState.Paused -> "Paused"
        is LocationTrackingState.Error -> "Error: ${state.message}"
    }

    val isTracking = uiState.state is LocationTrackingState.Tracking || uiState.state is LocationTrackingState.Paused

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("tracking_screen_container")
            .semantics { contentDescription = "WalkMark Tracking Screen" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WalkMark",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .testTag("tracking_title")
                .semantics { contentDescription = "WalkMark Title" }
        )
        Text(
            text = if (isTracking) {
                "Your route is being recorded on this device."
            } else {
                "Start tracking when you're ready to head out."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tracking_status_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Surface(
                    color = if (isTracking) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (isTracking) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = "Status: $statusText",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("tracking_status_text")
                            .semantics { contentDescription = "Tracking Status: $statusText" }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Metric(
                        label = "Distance",
                        value = "${uiState.distanceMeters.toInt()} m",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tracking_distance_text")
                            .semantics { contentDescription = "Total Distance Meters" }
                    )
                    Metric(
                        label = "Route points",
                        value = uiState.points.size.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tracking_points_count")
                            .semantics { contentDescription = "Accepted Points Count" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isTracking) {
            Button(
                onClick = { permissionLauncher() },
                enabled = !isStartingWalk && !isTracking,
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 280.dp)
                    .height(48.dp)
                    .testTag("start_walk_button")
                    .semantics { contentDescription = "Start Walk" }
            ) {
                Text(
                    text = "Start Walk",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = { viewModel.stopTracking() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 280.dp)
                    .height(48.dp)
                    .testTag("stop_walk_button")
                    .semantics { contentDescription = "Stop Walk" }
            ) {
                Text(
                    text = "Stop Walk",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
