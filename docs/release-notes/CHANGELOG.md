# Changelog

All notable changes to WalkMark will be documented in this file.

# [Unreleased]
### Added - [IMPLEMENTED]
- **US-011: Local Walk GPX Export**:
  - Provider-neutral `GpxDocumentGenerator` in `commonMain` producing valid UTF-8 **GPX 1.1**
    (`version="1.1"`, `creator="WalkMark"`, `http://www.topografix.com/GPX/1/1` namespace plus
    `xsi:schemaLocation`) with `<metadata>`, one `<trk>`/`<trkseg>`, and one `<trkpt>` per
    persisted point.
  - Read-only `ExportWalkUseCase` built on the existing `WalkRepository.getWalk()` and
    `observePoints().first()`. Point order is the persisted `ORDER BY seq ASC` sequence and is
    never re-sorted.
  - **No manufactured data.** `<ele>` is emitted only when the persisted point carries altitude;
    a missing altitude is never converted to `0`. `<time>` is emitted only for a timestamp
    greater than zero, formatted as locale-independent UTC ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`).
    Unavailable source data causes the element to be omitted.
  - Text values are XML-escaped for `& < > " '`. Numeric latitude/longitude are serialised with
    locale-independent `Double.toString`, never XML-escaped, so no decimal-comma defect can occur.
  - Deterministic filename `walkmark-<sanitized-title>-<walk-id>.gpx`, degrading to
    `walkmark-<walk-id>.gpx`. Both segments use an `[A-Za-z0-9-_]` allow-list with bounded length,
    so a walk title can never produce a path separator or a `..` traversal segment.
  - `GpxExportViewModel` is platform-neutral: it depends only on `ExportWalkUseCase` and the
    provider-neutral `GpxShareLauncher` interface, and holds no `Context`, `Intent`, `Uri`,
    `NSURL`, or filesystem path. `WalkExportState` is
    `Idle / Exporting / Success / Error`, with an `InProgress` re-entrancy guard that suppresses
    duplicate export requests. `Error` carries a closed enum reason, never an exception message.
    It owns **no walk list, no history, and no navigation**; a caller supplies the id of the walk
    the user chose to export.
  - `GpxShareLauncher` expect/actual following the existing `SupportContactLauncher` pattern.
    Android writes to the **app-private cache only** (`walkmark-gpx/`), shares via
    `androidx.core.content.FileProvider` + `ACTION_SEND` + `application/gpx+xml` +
    `FLAG_GRANT_READ_URI_PERMISSION`, and adds **no storage permission**.
    iOS writes to `NSTemporaryDirectory()` and presents `UIActivityViewController`.
  - `ACCOUNT_REQUIRED = NO`, `NETWORK_REQUIRED = NO`: export reads only already-persisted local data.

  Scope boundaries honoured:
  - Room schema **unchanged** (still version 2); no migration, no entity, DAO, or mapper change.
  - `WalkRepository.kt` **unchanged** (high-conflict registry file).
  - Walk data is **read only**: `Walk`, `walk_points`, identifiers, and media paths are never mutated.
  - No new dependency. `androidx.core.content.FileProvider` resolves transitively through the
    existing `androidx.activity:activity-compose`; `libs.versions.toml` is unchanged.
  - Out of scope and not implemented: GPX import, cloud backup, sync, bulk export, ZIP archive,
    photo export, note export, background export, scheduled backup.
  - **`App.kt` is NOT changed.** `strings.xml` is NOT changed. No root destination, no entry
    button, no navigation wiring, and no History/Detail UI is introduced by US-011.

  Ownership:
  - US-005B is the sole owner of the History list, Walk Detail, and navigation to persisted walks.
    A prior US-011 commit (`d8bd662`) introduced a competing `Saved Walks` destination and
    `SavedWalksScreen` in `App.kt`; a corrective follow-up commit removed all of it, so US-011
    ships as **infrastructure only**. The future user-facing action is an *Export GPX* button
    added to the canonical Walk Detail UI by that story - deliberately not implemented here.
    Because there is no entry point yet, `docs/user-guide/export-gpx.md` documents the file
    format and platform behaviour rather than a user task.

  Verification status (partial - verification debt OPEN):
  - `:composeApp:compileDebugKotlinAndroid` **PASS** on the `c53c8d8` baseline and **PASS** after
    the initial implementation. This confirms `androidx.core.content.FileProvider` resolves
    through the existing `androidx.activity:activity-compose` graph with **no new dependency**,
    and that the Android actual and new resources compile.
  - **That PASS was produced before the isolation correction.** The corrective commit renames
    `SavedWalksViewModel` to `GpxExportViewModel` and drops the walk-list projection, and it is
    therefore **NOT VERIFIED ON THE FINAL TIP**.
  - `:composeApp:testDebugUnitTest` **NOT PASSING / NOT VERIFIED**. The first run failed at
    `:composeApp:compileDebugUnitTestKotlinAndroid` because a test helper returned the
    `CoroutineScope` interface instead of `TestScope`, so `advanceUntilIdle()` was unresolved.
    That defect was corrected **statically**; the suite has never passed, so no test pass is claimed.
  - `:composeApp:assembleDebug` **NOT RUN**. The `FileProvider` provider declaration and
    `res/xml/file_paths.xml` linkage are statically audited but **not** proven by a successful build.
  - Test suites awaiting first successful execution: `GpxDocumentGeneratorTest` (13),
    `ExportWalkUseCaseTest` (8), `GpxExportViewModelTest` (7), `GpxShareLauncherTest` (1).

  Not verified / deferred:
  - iOS actual compilation and runtime: **NOT VERIFIED ON WINDOWS** (`ENVIRONMENT_BLOCKED` /
    `MACOS_XCODE_REQUIRED`). No iOS PASS is claimed.
  - Android instrumentation runtime: **NOT VERIFIED** (no emulator run in this pass).
  - Branch is based on `c53c8d8`. `origin/main` advanced to `023d1cd` after the branch was
    created; that commit touches only two `androidUnitTest` support files and does not overlap
    US-011, but the branch is one commit behind `main` and has not been rebased or merged.

  Docs: `docs/user-guide/export-gpx.md`.

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

