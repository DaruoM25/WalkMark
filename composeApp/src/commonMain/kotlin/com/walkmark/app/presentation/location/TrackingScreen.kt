package com.walkmark.app.presentation.location

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.location.LocationTrackingState

@Composable
fun TrackingScreen(
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("tracking_screen_container")
            .semantics { contentDescription = "WalkMark Tracking Screen" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WalkMark",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .testTag("tracking_title")
                    .semantics { contentDescription = "WalkMark Title" }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Status: $statusText",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .testTag("tracking_status_text")
                            .semantics { contentDescription = "Tracking Status: $statusText" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Points: ${uiState.points.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .testTag("tracking_points_count")
                                .semantics { contentDescription = "Accepted Points Count" }
                        )

                        Text(
                            text = "Distance: ${uiState.distanceMeters.toInt()} m",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .testTag("tracking_distance_text")
                                .semantics { contentDescription = "Total Distance Meters" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!isTracking) {
                Button(
                    onClick = { permissionLauncher() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_walk_button")
                        .semantics { contentDescription = "Start Walk" }
                ) {
                    Text(
                        text = "Start Walk",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("stop_walk_button")
                        .semantics { contentDescription = "Stop Walk" }
                ) {
                    Text(
                        text = "Stop Walk",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
