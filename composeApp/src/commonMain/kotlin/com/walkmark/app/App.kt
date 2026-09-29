package com.walkmark.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.walkmark.app.core.database.createRoomDatabase
import com.walkmark.app.core.database.getDatabaseBuilder
import com.walkmark.app.data.location.DefaultLocationRepository
import com.walkmark.app.data.media.createLocalMediaStore
import com.walkmark.app.data.walk.RoomWalkRepository
import com.walkmark.app.data.walk.WalkSessionRecorder
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.rememberLocationTrackerManager
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.presentation.adaptive.AdaptiveWalkScaffold
import com.walkmark.app.presentation.adaptive.DevicePosture
import com.walkmark.app.presentation.journal.JournalScreen
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.TrackingScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme
import com.walkmark.app.presentation.walk.WalkViewModel

val LocalWalkViewModel = compositionLocalOf<WalkViewModel> {
    error("WalkViewModel not provided")
}

@Composable
fun App(
    locationRepository: LocationRepository? = null,
    walkRepository: WalkRepository? = null,
    mediaStore: LocalMediaStore? = nul
    posture: DevicePosture = DevicePosture.Normal
) {
    WalkMarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("app_root_surface"),
            color = MaterialTheme.colorScheme.background
        ) {
            val scope = rememberCoroutineScope()
            val trackerManager = rememberLocationTrackerManager()

            val repository = remember(locationRepository, trackerManager) {
                locationRepository ?: DefaultLocationRepository(
                    rawLocationFlow = trackerManager.rawLocationUpdates,
                    onStartPlatformTracking = { trackerManager.start() },
                    onStopPlatformTracking = { trackerManager.stop() }
                )
            }

            val database = remember { createRoomDatabase(getDatabaseBuilder()) }
            val walks = remember(database, walkRepository) {
                walkRepository ?: RoomWalkRepository(database)
            }
            val media = remember { mediaStore ?: createLocalMediaStore() }

            val recorder = remember(repository, walks, scope) {
                WalkSessionRecorder(
                    locationRepository = repository,
                    walkRepository = walks,
                    scope = scope
                )
            }

            DisposableEffect(recorder) {
                recorder.start()
                onDispose { recorder.stop() }
            }

            val walkViewModel = remember(walks, media, repository, scope) {
                WalkViewModel(
                    walkRepository = walks,
                    mediaStore = media,
                    locationRepository = repository,
                    scope = scope
                )
            }

            val viewModel = remember(repository) {
                LocationViewModel(repository)
            }

CompositionLocalProvider(LocalWalkViewModel provides walkViewModel) {
    AdaptiveWalkScaffold(
        posture = posture,
        primaryContent = { _ ->
            TrackingScreen(viewModel = viewModel)
        },
        secondaryContent = { _ ->
            JournalScreen()
        }
    )
}
}

