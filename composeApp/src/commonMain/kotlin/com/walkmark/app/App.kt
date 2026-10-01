package com.walkmark.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.walkmark.app.core.database.createRoomDatabase
import com.walkmark.app.core.database.getDatabaseBuilder
import com.walkmark.app.data.auth.UnavailableAuthRepository
import com.walkmark.app.data.location.DefaultLocationRepository
import com.walkmark.app.data.media.createLocalMediaStore
import com.walkmark.app.data.monetization.UnavailableSubscriptionManager
import com.walkmark.app.data.monetization.WalkRepositoryWalkCount
import com.walkmark.app.data.walk.RoomWalkRepository
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
import com.walkmark.app.presentation.home.HomeScreen
import com.walkmark.app.presentation.auth.AuthScreen
import com.walkmark.app.presentation.auth.AuthViewModel
import com.walkmark.app.presentation.components.AccessPresentation
import com.walkmark.app.presentation.components.AccessTier
import com.walkmark.app.presentation.journal.WalkDetailScreen
import com.walkmark.app.presentation.journal.WalkHistoryScreen
import com.walkmark.app.presentation.journal.deletionNotice
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.map.LiveMapUiState
import com.walkmark.app.presentation.paywall.HardPaywallSheet
import com.walkmark.app.presentation.paywall.HardPaywallUiState
import com.walkmark.app.presentation.settings.SettingsScreen
import com.walkmark.app.presentation.settings.SettingsViewModel
import com.walkmark.app.presentation.support.SupportContactConfig
import com.walkmark.app.presentation.support.SupportScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme
import com.walkmark.app.presentation.walk.WalkViewModel

val LocalWalkViewModel = compositionLocalOf<WalkViewModel> {
    error("WalkViewModel not provided")
}

private enum class BottomTab {
    Home, History, Account
}

private enum class SubDestination {
    Settings, Support, Detail
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    locationRepository: LocationRepository? = null,
    walkRepository: WalkRepository? = null,
    mediaStore: LocalMediaStore? = null,
    supportContactConfig: SupportContactConfig = SupportContactConfig.NotConfigured,
    subscriptionManager: SubscriptionManager? = null,
    authRepository: AuthRepository? = null,
    juryPromoManager: JuryPromoManager? = null,
    mapContent: (@Composable (LiveMapUiState, Modifier) -> Unit)? = null
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

            val effectiveSubscriptions = remember(baseSubscriptions, promo) {
                JuryPromoSubscriptionManager(baseSubscriptions, promo)
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
            val subscriptionState by effectiveSubscriptions.subscriptionState.collectAsState()
            val isJuryActive by promo.isJuryAccessActive.collectAsState()
            val juryValidUntil by promo.validUntil.collectAsState()

            var paywallVisible by remember { mutableStateOf(false) }
            var selectedWalkId by remember { mutableStateOf<String?>(null) }
            var historyNotice by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(startResult) {
                if (startResult is WalkStartResult.RequiresSubscription) {
                    paywallVisible = true
                    viewModel.consumeStartResult()
                }
            }

            var selectedTab by remember { mutableStateOf(BottomTab.Home) }
            var subDestination by remember { mutableStateOf<SubDestination?>(null) }

            // Build access presentation from domain state (presentation-only mapping)
            val accessPresentation = remember(subscriptionState, isJuryActive, juryValidUntil, freeWalkCount) {
                when {
                    isJuryActive -> AccessPresentation(
                        tier = AccessTier.Jury,
                        title = "Jury Access",
                        description = "Active until ${juryValidUntil ?: JuryPromoManager.VALID_UNTIL}"
                    )
                    subscriptionState.isEntitled -> AccessPresentation(
                        tier = AccessTier.Premium,
                        title = "Premium",
                        description = "Unlimited walks"
                    )
                    else -> AccessPresentation(
                        tier = AccessTier.Free,
                        title = "Free",
                        description = "3 walks included",
                        detail = "$freeWalkCount of 3 walks used"
                    )
                }
            }

            CompositionLocalProvider(LocalWalkViewModel provides walkViewModel) {
                // Sub-destinations overlay the bottom tabs
                when (subDestination) {
                    SubDestination.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        juryPromoManager = promo,
                        onNavigateToAccount = {
                            subDestination = null
                            selectedTab = BottomTab.Account
                        },
                        onNavigateToSupport = { subDestination = SubDestination.Support },
                        onBack = { subDestination = null }
                    )
                    SubDestination.Support -> SupportScreen(
                        contactConfig = supportContactConfig,
                        onBack = { subDestination = SubDestination.Settings }
                    )
                    SubDestination.Detail -> selectedWalkId?.let { walkId ->
                        WalkDetailScreen(
                            walkId = walkId,
                            repository = walks,
                            mediaStore = media,
                            onBack = {
                                subDestination = null
                                selectedTab = BottomTab.History
                            },
                            onDeleted = { result ->
                                historyNotice = deletionNotice(result)
                                subDestination = null
                                selectedTab = BottomTab.History
                            }
                        )
                    }
                    null -> {
                        // Main app shell with bottom navigation
                        val topBarTitle = when (selectedTab) {
                            BottomTab.Home -> "WalkMark"
                            BottomTab.History -> "History"
                            BottomTab.Account -> "Account"
                        }

                        Scaffold(
                            modifier = Modifier.testTag("app_scaffold"),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = topBarTitle,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.testTag("top_bar_title")
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("bottom_navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == BottomTab.Home,
                                        onClick = { selectedTab = BottomTab.Home },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == BottomTab.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Home") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier
                                            .testTag("nav_home")
                                            .semantics { contentDescription = "Home tab" }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == BottomTab.History,
                                        onClick = { historyNotice = null; selectedTab = BottomTab.History },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == BottomTab.History) Icons.Filled.History else Icons.Outlined.History,
                                                contentDescription = "History"
                                            )
                                        },
                                        label = { Text("History") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier
                                            .testTag("nav_history")
                                            .semantics { contentDescription = "History tab" }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == BottomTab.Account,
                                        onClick = { selectedTab = BottomTab.Account },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == BottomTab.Account) Icons.Filled.Person else Icons.Outlined.Person,
                                                contentDescription = "Account"
                                            )
                                        },
                                        label = { Text("Account") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier
                                            .testTag("nav_account")
                                            .semantics { contentDescription = "Account tab" }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(Modifier.padding(innerPadding).fillMaxSize()) {
                                when (selectedTab) {
                                    BottomTab.Home -> HomeScreen(
                                        viewModel = viewModel,
                                        accessPresentation = accessPresentation,
                                        mapContent = mapContent
                                    )
                                    BottomTab.History -> WalkHistoryScreen(
                                        repository = walks,
                                        onOpenWalk = {
                                            selectedWalkId = it
                                            subDestination = SubDestination.Detail
                                        },
                                        notice = historyNotice
                                    )
                                    BottomTab.Account -> AuthScreen(
                                        viewModel = authViewModel,
                                        accessPresentation = accessPresentation,
                                        onNavigateToSettings = { subDestination = SubDestination.Settings },
                                        onNavigateToSupport = { subDestination = SubDestination.Support }
                                    )
                                }
                            }
                        }
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
