# Changelog

All notable changes to WalkMark will be documented in this file.

## [Unreleased]
### Added - [VERIFIED]
- **US-001: KMP Stack Foundation & Architecture Setup**:
  - Canonical `:composeApp` module configured with Kotlin Multiplatform `2.0.21` and Compose Multiplatform `1.7.0`.
  - Canonical packages structured under `commonMain`: `ui/`, `domain/`, `data/`, `location/`.
  - Core domain models implemented with `kotlinx.serialization` and `kotlinx.datetime`: `Walk`, `Waypoint`, `NotePhoto`.
  - Room KMP `2.7.0-alpha11` database entities (`WalkEntity`, `SampleEntity`) and DAOs (`WalkDao`, `SampleDao`) with `BundledSQLiteDriver`.
  - Root `App()` and `JournalScreen` rendering exact text `WalkMark: Ready for Shipaton` with semantic `testTag` selectors and adaptive two-pane layout support.
  - Complete N1, N2, and N3a test suites validated.
  - macOS CI workflow configured for N3b iOS framework verification.

