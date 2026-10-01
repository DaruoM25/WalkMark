package com.walkmark.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.walkmark.app.core.database.createRoomDatabase
import com.walkmark.app.core.database.getDatabaseBuilder
import com.walkmark.app.data.auth.UnavailableAuthRepository
import com.walkmark.app.data.location.DefaultLocationRepository
import com.walkmark.app.data.media.createLocalMediaStore
import com.walkmark.app.data.monetization.UnavailableSubscriptionManager
import com.walkmark.app.data.repository.RoomWalkRepository
import com.walkmark.app.data.walk.WalkRepositoryWalkCount
import com.walkmark.app.data.walk.WalkSessionRecorder
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.rememberLocationTrackerManager
import com.walkmark.app.domain.monetization.JuryPromoSubscriptionManager
import com.walkmark.app.domain.monetization.SubscriptionManager
import com.walkmark.app.domain.monetization.WalkCreationAccessPolicy
import com.walkmark.app.domain.promo.JuryPromoManager
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.StartWalkUseCase
import com.walkmark.app.domain.walk.WalkStartResult
import com.walkmark.app.presentation.adaptive.AdaptiveLayout
import com.walkmark.app.presentation.adaptive.AdaptiveWalkScaffold
import com.walkmark.app.presentation.adaptive.DevicePosture
import com.walkmark.app.presentation.auth.AuthScreen
import com.walkmark.app.presentation.auth.AuthViewModel
import com.walkmark.app.presentation.journal.WalkDetailScreen
import com.walkmark.app.presentation.journal.WalkHistoryScreen
import com.walkmark.app.presentation.journal.deletionNotice
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.TrackingScreen
import com.walkmark.app.presentation.paywall.HardPaywallSheet
import com.walkmark.app.presentation.paywall.HardPaywallUiState
import com.walkmark.app.presentation.settings.SettingsScreen
import com.walkmark.app.presentation.settings.SettingsViewModel
import com.walkmark.app.presentation.support.SupportContactConfig
import com.walkmark.app.presentation.support.SupportScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme
import com.walkmark.app.presentation.walk.LocalWalkViewModel
import com.walkmark.app.presentation.walk.WalkViewModel
import org.jetbrains.compose.resources.stringResource
import walkmark.composeapp.generated.resources.Res
import walkmark.composeapp.generated.resources.settings_entry
import walkmark.composeapp.generated.resources.support_entry

private enum class RootDestination {
    Main,
    Settings,
    Support,
    History,
    Detail,
    Account
}

@Composable
fun App(
    locationRepository: LocationRepository? = null,
    walkRepository: WalkRepository? = null,
    mediaStore: LocalMediaStore? = null,
    supportContactConfig: SupportContactConfig = SupportContactConfig.NotConfigured,
    posture: DevicePosture = DevicePosture.Normal,
    subscriptionManager: SubscriptionManager? = null,
    authRepository: AuthRepository? = null,
    juryPromoManager: JuryPromoManager? = null
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

            val media = remember { mediaStore ?: createLocalMediaStore() }
            val walks = remember(walkRepository, media) {
                walkRepository ?: RoomWalkRepository(createRoomDatabase(getDatabaseBuilder()), media)
            }

            val auth = remember(authRepository) {
                authRepository ?: UnavailableAuthRepository()
            }

            val promo = remember(juryPromoManager) {
                juryPromoManager ?: JuryPromoManager()
            }

            val baseSubscriptions = remember(subscriptionManager) {
                subscriptionManager ?: UnavailableSubscriptionManager()
            }

            val effectiveSubscriptions = remember(baseSubscriptions, promo, scope) {
                JuryPromoSubscriptionManager(
                    base = baseSubscriptions,
                    promoManager = promo,
                    scope = scope
                )
            }

            val persistedWalkCount = remember(walks) { WalkRepositoryWalkCount(walks) }

            val startWalk = remember(effectiveSubscriptions, persistedWalkCount, repository, scope) {
                StartWalkUseCase(
                    subscriptionManager = effectiveSubscriptions,
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

            val settingsViewModel = remember(walks, scope) {
                SettingsViewModel(
                    walkRepository = walks,
                    scope = scope
                )
            }

            val authViewModel = remember(auth, scope) {
                AuthViewModel(
                    authRepository = auth,
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
            val offerings by effectiveSubscriptions.offerings.collectAsState()

            var paywallVisible by remember { mutableStateOf(false) }
            var selectedWalkId by remember { mutableStateOf<String?>(null) }
            var historyNotice by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(startResult) {
                if (startResult is WalkStartResult.RequiresSubscription) {
                    paywallVisible = true
                    viewModel.consumeStartResult()
                }
            }
            var destination by remember { mutableStateOf(RootDestination.Main) }

            CompositionLocalProvider(LocalWalkViewModel provides walkViewModel) {
                when (destination) {
                    RootDestination.Main -> Box(Modifier.fillMaxSize()) {
                        AdaptiveWalkScaffold(
                            posture = posture,
                            primaryContent = { _ ->
                                TrackingScreen(viewModel = viewModel)
                            },
                            secondaryContent = { _ ->
                                WalkHistoryScreen(
                                    repository = walks,
                                    onOpenWalk = { selectedWalkId = it; destination = RootDestination.Detail }
                                )
                            }
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(8.dp)
                        ) {
                            TextButton(
                                onClick = { historyNotice = null; destination = RootDestination.History },
                                modifier = Modifier.testTag("history_entry_button")
                            ) {
                                Text("History")
                            }
                            TextButton(
                                onClick = { destination = RootDestination.Account },
                                modifier = Modifier
                                    .testTag("account_entry_button")
                                    .semantics { contentDescription = "Account" }
                            ) {
                                Text("Account")
                            }
                            TextButton(
                                onClick = { destination = RootDestination.Settings },
                                modifier = Modifier
                                    .testTag("settings_entry_button")
                                    .semantics { contentDescription = "Settings" }
                            ) {
                                Text(stringResource(Res.string.settings_entry))
                            }
                            TextButton(
                                onClick = { destination = RootDestination.Support },
                                modifier = Modifier
                                    .testTag("support_entry_button")
                                    .semantics { contentDescription = "Help and Support" }
                            ) {
                                Text(stringResource(Res.string.support_entry))
                            }
                        }
                    }
                    RootDestination.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        juryPromoManager = promo,
                        onNavigateToAccount = { destination = RootDestination.Account },
                        onNavigateToSupport = { destination = RootDestination.Support },
                        onBack = { destination = RootDestination.Main }
                    )
                    RootDestination.Account -> AuthScreen(
                        viewModel = authViewModel,
                        onBack = { destination = RootDestination.Main }
                    )
                    RootDestination.Support -> SupportScreen(
                        contactConfig = supportContactConfig,
                        onBack = { destination = RootDestination.Settings }
                    )
                    RootDestination.History -> WalkHistoryScreen(
                        repository = walks,
                        onOpenWalk = { selectedWalkId = it; destination = RootDestination.Detail },
                        onBack = { destination = RootDestination.Main },
                        notice = historyNotice
                    )
                    RootDestination.Detail -> selectedWalkId?.let { walkId ->
                        WalkDetailScreen(
                            walkId = walkId,
                            repository = walks,
                            mediaStore = media,
                            onBack = { destination = RootDestination.History },
                            onDeleted = { result ->
                                historyNotice = deletionNotice(result)
                                destination = RootDestination.History
                            }
                        )
                    }
                }
            }

            if (paywallVisible) {
                HardPaywallSheet(
                    state = HardPaywallUiState(
                        offerings = offerings,
                        freeWalkCount = freeWalkCount,
                        freeWalkLimit = WalkCreationAccessPolicy.FREE_WALK_LIMIT
                    ),
                    juryPromoManager = promo,
                    onDismiss = { paywallVisible = false }
                )
            }
        }
    }
}
