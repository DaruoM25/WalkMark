package com.walkmark.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.walkmark.app.data.location.DefaultLocationRepository
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.rememberLocationTrackerManager
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.TrackingScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme

@Composable
fun App(
    locationRepository: LocationRepository? = null
) {
    WalkMarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("app_root_surface"),
            color = MaterialTheme.colorScheme.background
        ) {
            val trackerManager = rememberLocationTrackerManager()
            val repository = remember(locationRepository, trackerManager) {
                locationRepository ?: DefaultLocationRepository(
                    rawLocationFlow = trackerManager.rawLocationUpdates,
                    onStartPlatformTracking = { trackerManager.start() },
                    onStopPlatformTracking = { trackerManager.stop() }
                )
            }
            val viewModel = remember(repository) {
                LocationViewModel(repository)
            }

            TrackingScreen(viewModel = viewModel)
        }
    }
}
