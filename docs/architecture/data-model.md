# WalkMark — Data Model Specification

## Foundation Database - [VERIFIED]
- `SampleItem` / `SampleEntity`: Used for Room KMP database, DAO, and builder validation.
- `Walk` / `WalkEntity` / `WalkDao`: Room KMP database entity, DAO, and domain mapping verified with BundledSQLiteDriver.
- `WalkMarkDatabase`: Multi-entity Room database compiled and verified off-UI thread.

## Domain Models (kotlinx.serialization & kotlinx.datetime) - [VERIFIED]
1. **Walk**: id, startTime, endTime, totalDistanceMeters, durationSeconds, title, summary - [VERIFIED]
2. **Waypoint**: id, walkId, latitude, longitude, altitude, timestamp, accuracyMeters - [VERIFIED]
3. **NotePhoto**: id, walkId, text, photoUri, latitude, longitude, createdAt - [VERIFIED]

