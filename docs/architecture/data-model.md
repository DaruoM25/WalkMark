# WalkMark — Data Model Specification

## Foundation Database - [IMPLEMENTED]
- `SampleItem` / `SampleEntity`: Used for Room KMP database, DAO, and builder validation. Not part of the walk journal model.
- `WalkMarkDatabase`: `commonMain`, 5 entities, schema version `2`, `@ConstructedBy` KMP constructor.
- Driver: `BundledSQLiteDriver`. Android native load is `NOT_VERIFIED` (see evidence waivers).

## Walk Journal Model - [IMPLEMENTED]
Schema version 2. Full architecture: `docs/architecture/persistence.md`.

1. **Walk** (`walks`) - [IMPLEMENTED]
   id, title, summary, startTimeEpochMs, endTimeEpochMs, totalDistanceMeters, durationSeconds, status.
2. **WalkPoint** (`walk_points`) - [IMPLEMENTED]
   id, walkId, seq, latitude, longitude, altitude, timestampEpochMs, accuracyMeters.
   `seq` is a monotonic per-walk sequence assigned by the repository. FK → `walks.id`, `ON DELETE CASCADE`.
3. **WalkPhoto** (`walk_photos`) - [IMPLEMENTED]
   id, walkId, relativePath, mimeType, byteSize, latitude, longitude, createdAtEpochMs.
   Stores a **relative** path, never an absolute filesystem path. FK → `walks.id`, `ON DELETE CASCADE`.
4. **WalkNote** (`walk_notes`) - [IMPLEMENTED]
   id, walkId, text, latitude, longitude, createdAtEpochMs.
   FK → `walks.id`, `ON DELETE CASCADE`.

### Conventions
- All timestamps are `Long` epoch milliseconds. No date/time formatter or string dates.
- Accepted GPS points only are persisted; the filter pipeline runs before the repository.
- Cascade delete is verified: `deleteWalkCascadesToPointsNotesAndPhotos` `PASS`.

## Verification Status
- Migration v1 → v2 behavioral proof (`migratedWalkColumnsMatchRoomExpectations`): `PASS`
- Reopen durability (`childRowsSurviveDatabaseReopen`): `PASS`
- Full JVM suite: 98 tests, 98 passed, 0 failed, 0 skipped
- Room schema JSON export: `ENVIRONMENT_BLOCKED` / `DEFERRED` (no JSON produced)
- Android instrumentation runtime: `DEFERRED_WITH_WAIVER`
- iOS compilation: `NOT_VERIFIED_ON_WINDOWS`

See `docs/evidence/us-003a/README.md` for the full evidence and waiver register.
