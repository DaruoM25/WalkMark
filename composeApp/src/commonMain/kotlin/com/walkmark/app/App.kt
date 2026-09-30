package com.walkmark.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.walkmark.app.core.database.createRoomDatabase
import com.walkmark.app.core.database.getDatabaseBuilder
import com.walkmark.app.data.location.DefaultLocationRepository
import com.walkmark.app.data.media.createLocalMediaStore
import com.walkmark.app.data.monetization.UnavailableSubscriptionManager
import com.walkmark.app.data.monetization.WalkRepositoryWalkCount
import com.walkmark.app.data.walk.RoomWalkRepository
import com.walkmark.app.data.walk.WalkSessionRecorder
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.rememberLocationTrackerManager
import com.walkmark.app.domain.monetization.SubscriptionManager
import com.walkmark.app.domain.monetization.WalkCreationAccessPolicy
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.domain.walk.WalkStartResult
import com.walkmark.app.presentation.adaptive.AdaptiveWalkScaffold
import com.walkmark.app.presentation.adaptive.DevicePosture
import com.walkmark.app.presentation.journal.JournalScreen
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.TrackingScreen
import com.walkmark.app.presentation.paywall.HardPaywallSheet
import com.walkmark.app.presentation.paywall.HardPaywallUiState
import com.walkmark.app.presentation.theme.WalkMarkTheme
import com.walkmark.app.presentation.walk.WalkViewModel

val LocalWalkViewModel = compositionLocalOf<WalkViewModel> {
    error("WalkViewModel not provided")
}

@Composable
fun App(
    locationRepository: LocationRepository? = null,
    walkRepository: WalkRepository? = null,
    mediaStore: LocalMediaStore? = null,
    posture: DevicePosture = DevicePosture.Normal,
    subscriptionManager: SubscriptionManager? = null
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

            val subscriptions = remember(subscriptionManager) {
                subscriptionManager ?: UnavailableSubscriptionManager()
            }

            val persistedWalkCount = remember(walks) { WalkRepositoryWalkCount(walks) }

            val startWalk = remember(subscriptions, persistedWalkCount, repository, scope) {
                StartWalkUseCase(
                    subscriptionManager = subscriptions,
                    walkCount = persistedWalkCount,
                    locationRepository = repository,
                    scope = scope
                )
            }

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

            val viewModel = remember(repository, startWalk) {
                LocationViewModel(
                    locationRepository = repository,
                    startWalk = startWalk
                )
            }

            val startResult by viewModel.startResult.collectAsState()
            val freeWalkCount by viewModel.freeWalkCount.collectAsState()
            val offerings by subscriptions.offerings.collectAsState()

            var paywallVisible by remember { mutableStateOf(false) }

            LaunchedEffect(startResult) {
                if (startResult is WalkStartResult.RequiresSubscription) {
                    paywallVisible = true
                    viewModel.consumeStartResult()
                }
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

            if (paywallVisible) {
                HardPaywallSheet(
                    state = HardPaywallUiState(
                        offerings = offerings,
                        freeWalkCount = freeWalkCount,
                        freeWalkLimit = WalkCreationAccessPolicy.FREE_WALK_LIMIT
                    ),
                    onDismiss = { paywallVisible = false }
                )
            }
        }
    }
}

