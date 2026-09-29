# WalkMark — Local Persistence Architecture

Scope: US-003A (walk journal persistence, notes, photos, media storage).

Status markers used throughout this document:

| Marker | Meaning |
|---|---|
| `CONFIGURED` | Wiring/dependency declared in build files. Nothing proven to run. |
| `IMPLEMENTED` | Production code exists in the tree. |
| `EXECUTED` | Test actually ran and produced a result. |
| `PASS` | Executed and assertions held. |
| `FAIL` | Executed and assertions did not hold. |
| `ENVIRONMENT_BLOCKED` | Blocked by the host environment, not by product code. |
| `DEFERRED` | Deliberately not done in this story. |
| `NOT_VERIFIED` | No evidence exists. Must not be read as pass or fail. |

---

## 1. Layer boundaries

```
domain/           WalkRepository, LocalMediaStore, Walk, WalkNote, WalkPhoto, LocationPoint
   ^              (pure Kotlin, no Room, no Android, no platform APIs)
   |
data/             RoomWalkRepository, WalkSessionRecorder, createLocalMediaStore()
   ^
   |
core/database/    WalkMarkDatabase, entities, DAOs, MIGRATION_1_2, WalkMappers
```

The domain layer has no dependency on Room. `WalkRepository` and `LocalMediaStore` are
interfaces in `domain/repository`; `RoomWalkRepository` and the platform media stores are the
only implementations. This keeps the persistence detail swappable and testable in isolation.

All database and media I/O runs off the main thread. Room KMP is driven by injected coroutine
dispatchers, and `AndroidLocalMediaStore` wraps every file operation in `withContext(Dispatchers.IO)`.

## 2. Room database

`WalkMarkDatabase` — `commonMain`, `@ConstructedBy(WalkMarkDatabaseConstructor::class)`.

| Property | Value | Status |
|---|---|---|
| Entities | 5 (see below) | `IMPLEMENTED` |
| Schema version | `2` | `IMPLEMENTED` |
| `exportSchema` flag | `true` | `CONFIGURED` — but see §7, no JSON was produced |
| Driver | `BundledSQLiteDriver` | `CONFIGURED` / Android native load `NOT_VERIFIED` |
| Migration path | `MIGRATION_1_2` registered | `PASS` (behavioral proof, §6) |

The database is constructed through the KMP `expect/actual` `WalkMarkDatabaseConstructor`, with
`DatabaseBuilder.android.kt` supplying the platform builder and dispatcher. `SampleEntity` /
`SampleDao` remain as the Room foundation validation surface carried over from the earlier
database story; they are not part of the walk journal model.

## 3. Entities

| Entity | Table | Purpose |
|---|---|---|
| `WalkEntity` | `walks` | One recorded walk: title, summary, start/end, distance, duration, status. |
| `WalkPointEntity` | `walk_points` | Accepted GPS point. FK → `walks.id`, `ON DELETE CASCADE`. |
| `WalkNoteEntity` | `walk_notes` | Free-text note with its own geo-coordinate and creation time. FK → `walks.id`, `ON DELETE CASCADE`. |
| `WalkPhotoEntity` | `walk_photos` | Photo metadata incl. durable relative path. FK → `walks.id`, `ON DELETE CASCADE`. |
| `SampleEntity` | *(foundation)* | Room/DAO/builder validation. Not part of walk journal data. |

Conventions:

- **All timestamps are `Long` epoch milliseconds.** No date/time formatter, no string dates, and
  no platform date type is stored.
- **No absolute filesystem paths are stored.** Photos are referenced by a relative path plus
  metadata; see §5.
- `walk_points.seq` is a monotonically increasing per-walk sequence assigned by
  `RoomWalkRepository.appendPoints`, derived from `maxSeq(walkId)`. Route order is therefore
  explicit and independent of insertion timing.
- `ON UPDATE NO ACTION ON DELETE CASCADE` on every child table: deleting a walk removes its
  points, notes, and photo rows in one operation.

Foreign-key cascade behavior is exercised by
`WalkPersistenceIntegrationTest.deleteWalkCascadesToPointsNotesAndPhotos` — `PASS`.

## 4. DAO and repository layer

Five DAOs, one per entity, in `core/database/dao`:

- `WalkDao` — insert/update/get/delete by id, first-by-status, `observeAll`.
- `WalkPointDao` — `insertAll`, `maxSeq`, `observeByWalk`.
- `WalkNoteDao` — insert, `observeByWalk`.
- `WalkPhotoDao` — insert, `observeByWalk`.
- `SampleDao` — foundation validation.

`RoomWalkRepository` implements `WalkRepository`:

| Operation | Behavior |
|---|---|
| `startWalk` | Inserts a `Walk` with `status = ACTIVE`. |
| `completeWalk` | Sets end time, total distance, computed duration, `status = COMPLETED`. |
| `appendPoints` | Assigns `seq` from `maxSeq + 1`, batch-inserts. No-op on empty list. |
| `addNote` / `addPhoto` | Inserts the mapped entity. |
| `getWalk` / `getActiveWalk` | Point reads. |
| `observeAllWalks` / `observeActiveWalk` / `observePoints` / `observeNotes` / `observePhotos` | Cold `Flow`s driven by Room invalidation. |
| `deleteWalk` | Cascade delete via the parent row. |

`WalkMappers` (`toEntity` / `toDomain`) is the single conversion point between
`WalkNoteEntity` ↔ `WalkNote` and `WalkPhotoEntity` ↔ `WalkPhoto`, keeping Room types out of the
domain.

`WalkSessionRecorder` bridges GPS to persistence. It combines
`LocationRepository.trackingState` with `acceptedPoints` and reacts to each state:

- `Tracking` — creates the walk row on first emission, then persists only newly accepted points
  (tracked via `lastPersistedIndex`).
- `Paused` — flushes pending points.
- `Idle` — final flush, then `completeWalk`.
- `Error` — no persistence action.

The index-based flush is what prevents re-persisting the whole buffer on every GPS emission.

## 5. Media storage and durable photo references

`LocalMediaStore` is a domain interface with two operations, `importPhoto` and `deletePhoto`.
Platform binding is `expect fun createLocalMediaStore()` →
`LocalMediaStoreFactory.android.kt` → `AndroidLocalMediaStore`.

Android implementation:

- Photos are **copied out of the source `Uri` into app-private storage** under
  `filesDir/walk_photos/<walkId>/<uuid>.<ext>`. The source `Uri` is transient and is never
  persisted — picker-granted read access does not survive a reboot, so storing it would produce
  dangling references.
- `StoredPhoto.relativePath` (`<walkId>/<fileName>`) is the durable handle stored in
  `walk_photos.relativePath`. Resolution back to an absolute path is derived at read time via
  `absolutePath(relativePath)`, so the app's install path is not baked into the database.
- MIME type is resolved from the `ContentResolver` and drives the stored extension; `byteSize` is
  recorded from the copied file.
- Import failure deletes the partial file and throws rather than leaving a truncated entry.
- All file work is on `Dispatchers.IO`.

Photo picking is `PhotoPickerLauncher` (`expect`/`actual`), Android backed by the system photo
picker.

## 6. Migration v1 → v2 — behavioral proof

`WalkMarkMigrations.MIGRATION_1_2` (`commonMain`) creates `walks`, `walk_points`, `walk_notes`,
`walk_photos`, their foreign keys, and the three `walkId` indexes, using
`CREATE TABLE IF NOT EXISTS` / `CREATE INDEX IF NOT EXISTS` so it is idempotent.

Proof is **behavioral, not declarative**: `WalkMigration_1_2_Test.migratedWalkColumnsMatchRoomExpectations`
builds a real SQLite database at version 1, executes the migration against a live
`SQLiteConnection`, and asserts the resulting `walk_points` columns match the columns the
current `WalkPointEntity` expects.

| Evidence | Status |
|---|---|
| Migration code | `IMPLEMENTED` |
| `migratedWalkColumnsMatchRoomExpectations` | `EXECUTED` / `PASS` (0.508s) |
| `childRowsSurviveDatabaseReopen` | `EXECUTED` / `PASS` (0.639s) |
| Migration execution on a real Android device/emulator | `DEFERRED` — not part of the executed evidence |
| Exported schema JSON | `ENVIRONMENT_BLOCKED` — see §7 |

## 7. Room schema export — `ENVIRONMENT_BLOCKED` / `DEFERRED`

`WalkMarkDatabase` declares `exportSchema = true`, but **no schema JSON exists in this
repository**, and `composeApp/schemas` is empty.

What was established during this story:

| Finding | Status |
|---|---|
| `androidx.room` Gradle plugin with `room { schemaDirectory("schemas") }` | `FAIL` — KSP rejects the argument: `Processor arguments not in the format \S+=\S+: room.internal.schemaInput=C:\Projets Personnels\WalkMark\composeApp\schemas` |
| Working around the space in the workspace path via a path-space-free junction | `FAIL` — the junction canonicalizes back to the spaced path; the error is unchanged |
| Manual `room.schemaLocation=schemas` processor argument | `FAIL` — KSP tasks completed but no JSON was emitted |
| A working, supported schema-export path in this environment | `NOT_VERIFIED` / none found |

The Room plugin configuration was therefore removed from `composeApp/build.gradle.kts`; manual
KSP wiring of the Room compiler is retained and the build is green. `exportSchema = true` is
retained as the intended end state, but **it currently produces no artifact**.

Consequence: the exported-JSON schema history is not available for review, and the Room
migration-testing toolchain cannot auto-derive migration tests. The behavioral proof in §6 is
the authoritative evidence for this story and should not be read as a substitute for committed
schema history in future stories.

**Waiver:** Room schema JSON export is `ENVIRONMENT_BLOCKED` / `DEFERRED`. It is not a pass.

## 8. Android instrumentation runtime — `DEFERRED_WITH_WAIVER`

`composeApp/src/androidInstrumentedTest/.../AndroidRoomInstrumentationTest.kt` is a real-device
test that constructs a `BundledSQLiteDriver` database and exercises DAO insert/query/observe/
count/delete plus sample and point counts.

| Stage | Status |
|---|---|
| `:composeApp:compileDebugAndroidTestKotlin` | `PASS` — 0 compiler errors |
| Runner configuration | `CONFIGURED` — `androidx.test.runner.AndroidJUnitRunner` + `androidx.test:runner:1.6.2` |
| Emulator execution | `DEFERRED_WITH_WAIVER` |
| `BundledSQLiteDriver` native load on the Android x86_64 ABI | `NOT_VERIFIED` |

Root cause of the deferral: runtime verification exceeded the authorized timebox and
environment-attempt budget. The runner misconfiguration that originally caused test discovery
to fail was identified and fixed, but no executed Android runtime result is being claimed as
verification evidence for this story. Per the acceptance decision, this is recorded as
`NOT_VERIFIED` and must not be represented as pass or fail.

**Waiver:** Android instrumentation runtime execution and `BundledSQLiteDriver` Android native
runtime verification.

## 9. iOS — `NOT_VERIFIED_ON_WINDOWS`

The `iosMain` actuals exist (`LocalMediaStoreFactory`, `LocationTrackerManager.ios.kt`, and the
KSP Room compiler wiring for `iosArm64` / `iosSimulatorArm64`) and are `CONFIGURED`. They were
**never compiled** — the host is Windows with no Xcode toolchain. Nothing about the iOS
persistence path is verified, in either direction.

**Waiver:** iOS actual compilation and runtime verification.

## 10. Test inventory

Executed JVM suite — `:composeApp:testDebugUnitTest`:

| Metric | Value |
|---|---|
| Suites | 19 |
| Total tests | 98 |
| Passed | 98 |
| Failed | 0 |
| Skipped | 0 |

Full results and the waiver register are in `docs/evidence/us-003a/README.md`.
