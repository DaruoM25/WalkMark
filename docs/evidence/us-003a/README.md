# US-003A Verification Evidence

Verification record for US-003A — local walk persistence, notes, photos, and media storage.

```
FINAL_ACCEPTANCE = ACCEPTED_WITH_RUNTIME_WAIVER
```

US-003A is **not** fully runtime-verified. Four areas carry explicit waivers, listed in
[Waiver register](#waiver-register). None of them may be represented as pass.

## Verification vocabulary

| Marker | Meaning |
|---|---|
| `CONFIGURED` | Declared in build files. Nothing proven to run. |
| `IMPLEMENTED` | Production code exists. |
| `EXECUTED` | Test ran and produced a result. |
| `PASS` / `FAIL` | Executed; assertions held / did not hold. |
| `ENVIRONMENT_BLOCKED` | Blocked by host environment, not product code. |
| `DEFERRED` | Deliberately not done in this story. |
| `NOT_VERIFIED` | No evidence exists. Not pass, not fail. |

## Full JVM suite

Command (run once, final verification gate):

```
.\gradlew.bat :composeApp:testDebugUnitTest --console=plain --no-daemon
```

| Metric | Value |
|---|---|
| Result | `BUILD SUCCESSFUL in 6m 59s` (exit 0) |
| Suites | 19 |
| `TOTAL_TESTS` | 98 |
| `PASSED` | 98 |
| `FAILED` | 0 |
| `SKIPPED` | 0 |

Both previously-failing tests are inside this run and pass within it:

| Test | Time | Status |
|---|---|---|
| `WalkMigration_1_2_Test.migratedWalkColumnsMatchRoomExpectations` | 0.508s | `PASS` |
| `WalkPersistenceIntegrationTest.childRowsSurviveDatabaseReopen` | 0.639s | `PASS` |

No new functional regression. This run is the authoritative regression gate for US-003A.

## Targeted evidence

| Test | What it proves | Status |
|---|---|---|
| `migratedWalkColumnsMatchRoomExpectations` | `MIGRATION_1_2` executed against a real `SQLiteConnection` yields `walk_points` columns matching current `WalkPointEntity` expectations | `EXECUTED` / `PASS` |
| `childRowsSurviveDatabaseReopen` | Child rows (points, notes, photos) survive closing and reopening a uniquely-named file-backed database | `EXECUTED` / `PASS` |
| `deleteWalkCascadesToPointsNotesAndPhotos` | `ON DELETE CASCADE` removes all child rows | `EXECUTED` / `PASS` |
| `:composeApp:compileDebugAndroidTestKotlin` | Android instrumentation source compiles | `PASS`, 0 compiler errors |
| `BUNDLED_SQLITE_NATIVE_LOAD` on Android x86_64 | Bundled native SQLite loads and serves real DAO work | `NOT_VERIFIED` |

`SampleEntity` / `SampleDao` remain the Room foundation validation surface from the earlier
database story and are not walk journal data.

## Room schema export

`WalkMarkDatabase` declares `exportSchema = true`, but **no schema JSON exists** and
`composeApp/schemas` is empty.

| Attempt | Outcome |
|---|---|
| `androidx.room` plugin + `room { schemaDirectory("schemas") }` | KSP rejects the argument: `Processor arguments not in the format \S+=\S+: room.internal.schemaInput=C:\Projets Personnels\WalkMark\composeApp\schemas` |
| Path-space-free junction workaround | Fails identically — the junction canonicalizes back to the spaced path |
| Manual `room.schemaLocation=schemas` argument | KSP tasks complete but emit no JSON |
| Working supported export path | None found |

The Room plugin configuration was removed; manual Room-compiler KSP wiring is retained and the
build is green. `exportSchema = true` is kept as the intended end state but produces no
artifact.

Impact: no committed schema-version history is available for review, and Room's
migration-test auto-derivation cannot be used. The behavioral migration proof above is the
authoritative evidence for this story and is **not** a substitute for schema history going
forward.

## Waiver register

| # | Area | Classification | Basis |
|---|---|---|---|
| 1 | Room schema JSON export | `ENVIRONMENT_BLOCKED` / `DEFERRED` | KSP rejects a schema path containing a space; no validated export mechanism available in this environment |
| 2 | Android instrumentation runtime execution | `DEFERRED_WITH_WAIVER` — compile `PASS`, runtime `NOT_VERIFIED` | Runtime verification exceeded the authorized timebox and environment-attempt budget. No executed Android runtime result is claimed as verification evidence |
| 3 | `BundledSQLiteDriver` Android native runtime | `NOT_VERIFIED` | Follows directly from #2; must not be classified pass or fail |
| 4 | iOS actual compilation and runtime | `NOT_VERIFIED_ON_WINDOWS` | Host is Windows; no Xcode toolchain. `iosMain` actuals exist and are `CONFIGURED` but were never compiled |

### Runner configuration note (informational)

The original Android discovery failure was diagnosed and fixed in this story: AGP defaulted to
the deprecated `android.test.InstrumentationTestRunner`, which drives a JUnit 3
`junit.framework.TestSuite` and therefore reported `No tests found` for a JUnit 4
`@RunWith(AndroidJUnit4::class)` test. The build now declares
`testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"` and depends on
`androidx.test:runner:1.6.2` (addition only; `core:1.6.1` and `ext:junit:1.2.1` unchanged).

This is recorded as configuration progress only. It is **not** runtime verification, and it
does not lift waiver #2 or #3.

## Architecture reference

See `docs/architecture/persistence.md` for the full persistence architecture: entity and
schema design, the accepted-GPS-point model, `WalkNote` and `WalkPhoto`, the DAO/repository
layer, the media storage abstraction and durable relative-path photo references, the v1→v2
migration, and the iOS limitation.
