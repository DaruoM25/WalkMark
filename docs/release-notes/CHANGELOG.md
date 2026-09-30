# Changelog

All notable changes to WalkMark will be documented in this file.

# [Unreleased]
### Added - [IMPLEMENTED]
- **US-007: In-App User Support**:
  - Dedicated local Help & Support destination with approved FAQ and troubleshooting topics.
  - Privacy-preserving, user-initiated email contact through Android and iOS platform launchers.
  - No diagnostics, private walk content, telemetry, backend, support SDK, or new permission.

### Added - [VERIFIED]
- **US-006 Phase 1: Free-walk quota gate and hard paywall (no purchase provider)**:
  - Provider-neutral monetization domain: `SubscriptionState`/`SubscriptionStatus`,
    `SubscriptionManager` with `PurchaseResult`/`RestoreResult` (including `Unavailable`),
    `PaywallProduct`/`Offering`/`OfferingsState`, and the pure
    `WalkCreationAccessPolicy` decision function with `FREE_WALK_LIMIT = 3`.
  - Narrow `PersistedWalkCount` port (`walkCount: Flow<Int>`, `currentWalkCount(): Int`) plus
    the production adapter `WalkRepositoryWalkCount`, which only delegates to the existing
    `WalkRepository.observeAllWalks()`. **No second `WalkRepository` implementation exists.**
  - `StartWalkUseCase` as the authoritative *may-tracking-start* gate. It re-reads the walk
    count at call time so a stale retained `StateFlow` can never grant a walk, and on denial it
    performs **no side effect at all** - tracking never starts and nothing is persisted.
  - `UnavailableSubscriptionManager` in `commonMain`: a production-safe `NOT_CONFIGURED`
    fallback that never fabricates an entitlement or a loaded offering, and returns
    `PurchaseResult.Unavailable` / `RestoreResult.Unavailable`. Mutable
    `FakeSubscriptionManager` is `commonTest`-only.
  - `LocationViewModel` gate integration with a **synchronous** in-flight guard
    (`isStartingWalk` set before launching, reset in `finally`), so N concurrent Start taps
    produce exactly one gate invocation. `TrackingScreen` binds
    `enabled = !isStartingWalk && !isTracking` on the real Start control while preserving
    `testTag("start_walk_button")`, its contentDescription, and the existing
    permission-launcher sequence.
  - `HardPaywallSheet` + `HardPaywallUiState`: dismissible to Main via a visible Close control
    and system Back. Dismissal sets only presentation visibility - it never mutates quota,
    entitlement, walk count, or any persistent state, and creates no
    `alreadyOffered`/`paywallDismissed` latch. The next Start Walk tap re-runs the gate.
  - **No fabricated runtime prices.** When offerings are unavailable the paywall renders **no
    price row at all** (`showsStorePrices == false`, `products` empty). Instead it shows the
    title `Subscription required`, an explanation that purchases are not available in this
    build, the provider status `Provider not configured`, and disabled subscribe/restore
    controls. Planned pricing ($2.99/week, $19.99/year) is documentation-only and appears in
    no `.kt` / `.kts` / `.xml` / `.json` file in the repository.
  - No Material Icons dependency added; the close affordance is a dependency-free `TextButton`.

  Scope boundaries honoured:
  - `WalkRepository.kt`, `RoomWalkRepository.kt` and Room schema **unchanged**.
  - `WalkSessionRecorder` remains the `Walk` row creation owner; persistence is not moved into
    the gate.
  - No dependency change, no toolchain change, no schema change.

  Verification:
  - Full JVM unit-test suite **BUILD SUCCESSFUL** - 30 suites, 170 tests, 170 passed,
    0 failed, 0 skipped. Measured baseline on `origin/main` @ `6386460` was 19 suites /
    98 tests, so this change is strictly additive with no regression.
  - `:composeApp:compileDebugKotlinAndroid` **PASS**.
  - New suites: `WalkCreationAccessPolicyTest` (8), `StartWalkUseCaseTest` (8),
    `LocationViewModelStartWalkGateTest` (10), `UnavailableSubscriptionManagerTest` (9),
    `FakeSubscriptionManagerTest` (8), `HardPaywallUiStateTest` (6),
    `HardPaywallSheetComposeTest` (6), `MonetizationDomainArchitectureTest` (9).
  - Gate coverage: allow below limit, allow at 2nd free walk, deny at 4th with zero side
    effects, entitled bypass, stale-snapshot denial, count re-read per call, double-tap
    single invocation, dismissal-does-not-grant-access, paywall re-shown on next tap.

  Not verified / deferred:
  - Android instrumentation runtime: **NOT VERIFIED** (no emulator run in this pass).
  - iOS actual compilation and runtime: **NOT VERIFIED ON WINDOWS** (no Xcode toolchain).
  - Real billing: **NOT APPLICABLE** - no purchase provider exists by design in Phase 1.

  Docs: `docs/user-guide/subscriptions.md`,
  `docs/architecture/decisions/0001-initial-architecture.md` (Addendum),
  `docs/architecture/overview.md`, `docs/architecture/modules.md`,
  `docs/architecture/navigation.md`.

### Added - [ACCEPTED_WITH_RUNTIME_WAIVER]
- **US-003A: Local Walk Persistence, Notes, and Photos**:
  - Room KMP database at schema version 2 with five entities: `Walk`, accepted GPS `WalkPoint`,
    `WalkNote`, and `WalkPhoto` (plus the pre-existing `Sample` foundation entity).
  - All timestamps stored as `Long` epoch milliseconds; no date/time formatter or string dates.
  - `WalkPoint.seq` monotonic per-walk sequence assigned by the repository, making route order
    independent of insertion timing.
  - Foreign keys on all child tables use `ON DELETE CASCADE`, so deleting a walk removes its
    points, notes, and photo rows in one operation.
  - `MIGRATION_1_2` v1 → v2 creating all four walk tables, their foreign keys, and `walkId` indexes.
  - `RoomWalkRepository` implementing `WalkRepository`: walk start/complete, batched point append,
    note and photo insert, and `Flow`-based observation of walks, points, notes, and photos.
  - `WalkMappers` as the single entity ↔ domain conversion point, keeping Room types out of the domain.
  - `WalkSessionRecorder` bridging GPS to persistence, flushing only newly accepted points on
    `Tracking`/`Paused` and completing the walk on `Idle`.
  - `LocalMediaStore` abstraction: photos are copied out of the transient picker `Uri` into
    app-private storage and referenced by a durable **relative** path, so app install paths and
    expiring content permissions are never persisted. All media I/O on `Dispatchers.IO`.
  - Migration proof is **behavioral**: `WalkMigration_1_2_Test` runs the migration against a real
    `SQLiteConnection` and asserts columns match entity expectations.

  Verification:
  - Full JVM suite `BUILD SUCCESSFUL` — 19 suites, 98 tests, 98 passed, 0 failed, 0 skipped.
  - `migratedWalkColumnsMatchRoomExpectations` `PASS`; `childRowsSurviveDatabaseReopen` `PASS`;
    cascade delete `PASS`.
  - `:composeApp:compileDebugAndroidTestKotlin` `PASS`.

  Waivers (none of these are passes):
  - Room schema JSON export `ENVIRONMENT_BLOCKED` / `DEFERRED` — KSP rejects a schema path
    containing a space in the workspace path; no JSON was produced and `composeApp/schemas` is empty.
  - Android instrumentation runtime `DEFERRED_WITH_WAIVER` — compile `PASS`, runtime `NOT_VERIFIED`
    after exceeding the authorized timebox/environment budget.
  - `BundledSQLiteDriver` Android native loading `NOT_VERIFIED` (follows the runtime waiver).
  - iOS actual compilation and runtime `NOT_VERIFIED_ON_WINDOWS` — no Xcode toolchain on host.

  Build fix: `androidx.test.runner.AndroidJUnitRunner` is now configured with
  `androidx.test:runner:1.6.2`, replacing the deprecated framework runner that reported
  `No tests found` for the JUnit 4 instrumentation test.

  Docs: `docs/architecture/persistence.md`, `docs/evidence/us-003a/README.md`.

### Added - [VERIFIED]
=======
## [Unreleased]
### Added - [VERIFIED]
- **US-004A: Adaptive / Responsive Layout Foundation**:
  - Presentation width classification (`AdaptiveWidthClass.Compact` < 600dp, `AdaptiveWidthClass.Medium` 600dp..839dp, `AdaptiveWidthClass.Expanded` >= 840dp).
  - `AdaptiveWalkScaffold` supporting `CompactSinglePane`, `MediumSinglePane`, `ExpandedTwoPane` (~70/30 distribution with minimum constraints), and `CompactFlex` layout modes.
  - Platform posture abstraction (`DevicePosture.Normal`, `DevicePosture.TableTop`, `DevicePosture.Book`, `DevicePosture.SeparatingHinge`) with production posture defaulting to Normal.
  - Content slots decoupling presentation shell from future MapLibre renderer and current tracking/journal components.
  - State continuity verified across layout transitions during active GPS recording sessions.
  - Flex-compatible adaptive layout implemented; real fold posture detection deferred.
  - Automated tests: unit (`AdaptiveLayoutTest`), state continuity (`AdaptiveStateContinuityTest`), and Compose Robolectric UI (`AdaptiveScaffoldComposeTest`).
  - Runtime validation PASS on Android emulator (Compact layout, Expanded TwoPane layout, rotation continuity, and active tracking preservation).
  - 
- **US-002: Walk Recording Core + Native Background GPS Engine**:
  - Common domain models and repository: `LocationPoint`, `LocationTrackingState`, and `LocationRepository` interface.
  - `DistanceEngine` implementing Haversine geodesic calculation.
  - `GpsFilterEngine` implementing accuracy threshold (< 20m), temporal monotonicity, and unrealistic speed jump rejection.
  - Android native tracking: `WalkLocationService` Android Foreground Service (`foregroundServiceType="location"`, notification channel, persistent notification) using `FusedLocationProviderClient`.
  - Foreground location permission request (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) initiated from visible Start Walk user action.
  - Clean Stop Walk lifecycle stopping FusedLocationProviderClient callbacks, cancelling coroutine collection, and removing foreground notification.
  - Background tracking support verified with GPS point delivery while app is backgrounded.
  - Minimal `TrackingScreen` UI integration displaying live tracking status, accepted point counts, and distance meters.
  - iOS `CLLocationManager` implementation in `LocationTrackerManager.ios.kt` (host Xcode project, configuration, and runtime verification deferred because host project is absent).
  - Automated tests: unit and pipeline test coverage (`DistanceEngineTest`, `GpsFilterEngineTest`, `LocationRepositoryTest`, `LocationRepositoryPipelineTest`, `LocationViewModelTest`, `TrackingScreenTest`, `JournalComposeRealUiTest`).
  - Android N3a runtime validation PASS on emulator (foreground service, location stream, speed jump filter, background GPS reception, and stop lifecycle).

### Added - [IMPLEMENTED]
- KMP + Compose Multiplatform :1composeApp module foundation with Material 3.
- Room KMP database, bundled SQLite driver, and dispatcher injection foundation.
- UBF/MVWM JournalViewModel + StateFlow state machine.
- Common domain, database mapping, ViewModel, semantic Compose smoke tests, and CI workflow.

