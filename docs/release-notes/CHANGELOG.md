# Changelog

All notable changes to WalkMark will be documented in this file.

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

