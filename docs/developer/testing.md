# Testing Strategy - [IMPLEMENTED]

## Foundation Tests - [VERIFIED]
1. **Common Domain Tests**: `SampleItemTest`, `SampleEntityMappingTest` via `kotlin.test`.
2. **ViewModel UTF Tests**: `JournalViewModelTest` via `app.cash.turbine:turbine` and `kotlinx.coroutines.test`.
3. **Compose UISmoke Tests**: `JournalComposeSmokeTest` asserting semantic selectors and state immutability.

## Persistence Tests - [VERIFIED]
1. **Migration behavior**: `WalkMigration_1_2_Test` executes `MIGRATION_1_2` against a real
   `SQLiteConnection` and asserts resulting columns match current entity expectations - `PASS`.
2. **Persistence integration**: `WalkPersistenceIntegrationTest` covers CRUD, `ON DELETE CASCADE`,
   and child-row durability across a file-backed database close/reopen - `PASS`.
3. **Mapper tests** for entity ↔ domain conversion.

## Current Suite Result
`.\gradlew.bat :composeApp:testDebugUnitTest --console=plain --no-daemon`

19 suites, **98 tests, 98 passed, 0 failed, 0 skipped** (`BUILD SUCCESSFUL`). This is the
regression gate for US-003A.

## Android Instrumentation - [DEFERRED_WITH_WAIVER]
`AndroidRoomInstrumentationTest` compiles cleanly (`:composeApp:compileDebugAndroidTestKotlin`
`PASS`). Emulator **execution** is `DEFERRED_WITH_WAIVER` after exceeding the authorized
timebox/environment budget, so `BundledSQLiteDriver` native loading on Android is
`NOT_VERIFIED`. The build correctly configures `androidx.test.runner.AndroidJUnitRunner` with
`androidx.test:runner:1.6.2`.

Room schema JSON export is `ENVIRONMENT_BLOCKED`; migration proof is therefore behavioral
rather than derived from committed schema history.

Full detail: `docs/evidence/us-003a/README.md`.

## Planned Feature Tests - [PLANNED]
1. GPS Background Tracking & location filtering tests  - [PLANNED]
2. 3-free-walk quota and 4th-save paywall gate tests  - [PLANNED]
3. iOS actual compilation and runtime tests  - [PLANNED] (host is Windows; no Xcode toolchain)
