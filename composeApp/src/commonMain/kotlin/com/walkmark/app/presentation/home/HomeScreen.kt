package com.walkmark.app.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.presentation.components.AccessPresentation
import com.walkmark.app.presentation.components.AccessStatusCard
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.rememberLocationPermissionLauncher
import com.walkmark.app.presentation.map.LiveMapUiState
import com.walkmark.app.presentation.map.MapRouteUiModel
import com.walkmark.app.presentation.map.toLiveMapUiState

@Composable
fun HomeScreen(
    viewModel: LocationViewModel,
    accessPresentation: AccessPresentation,
    mapContent: (@Composable (LiveMapUiState, Modifier) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isStartingWalk by viewModel.isStartingWalk.collectAsState()

    val permissionLauncher = rememberLocationPermissionLauncher(
        onPermissionGranted = { viewModel.startTracking() }
    )

    val isTracking = uiState.state is LocationTrackingState.Tracking || uiState.state is LocationTrackingState.Paused

    val statusText = when (uiState.state) {
        is LocationTrackingState.Idle -> "Idle"
        is LocationTrackingState.Tracking -> "Tracking"
        is LocationTrackingState.Paused -> "Paused"
        is LocationTrackingState.Error -> "Error"
    }

    var recenterRequestId by remember { mutableLongStateOf(0L) }
    val liveMapState = remember(uiState, recenterRequestId) {
        uiState.toLiveMapUiState(recenterRequestId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
            .semantics { contentDescription = "WalkMark Home Screen" }
    ) {
        // === Live Map (55-65% of portrait content) ===
        if (mapContent != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .testTag("home_map_container")
            ) {
                mapContent(liveMapState, Modifier.fillMaxSize())
            }
        } else {
            // Placeholder when no map renderer available
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .testTag("home_map_placeholder"),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Map unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // === Below-map content ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tracking summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tracking_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primary metric: Distance
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatDistance(uiState.distanceMeters),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .testTag("tracking_distance_text")
                                .semantics { contentDescription = "Total Distance Meters" }
                        )
                        Text(
                            text = "Distance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Secondary: Status badge
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
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("tracking_status_text")
                                .semantics { contentDescription = "Tracking Status: $statusText" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Access status card
            AccessStatusCard(
                access = accessPresentation,
                compact = true,
                modifier = Modifier.testTag("home_access_status")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Start / Stop CTA
            if (!isTracking) {
                Button(
                    onClick = { permissionLauncher() },
                    enabled = !isStartingWalk && !isTracking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_walk_button")
                        .semantics { contentDescription = "Start Walk" },
                    shape = MaterialTheme.shapes.medium
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
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("stop_walk_button")
                        .semantics { contentDescription = "Stop Walk" },
                    shape = MaterialTheme.shapes.medium
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
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        val km = meters / 1000.0
        "%.2f km".format(km)
    } else {
        "${meters.toInt()} m"
    }
}
