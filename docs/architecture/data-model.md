# WalkMark — Data Model Specification

## Foundation Database - [IMPLEMENTED]
- `SampleItem` / `SampleEntity`: Used for Room KMP database, DAO, and builder validation.
- `WalkMarkDatabase`: Compiled and verified with bundled SQLite driver and coroutine dispatcher injection.

## Future Walk Journal Entities - [PLANNED]
1. **Walk**: id, startTime, endTime, totalDistanceMeters, durationSeconds, title, summary  - [PLANNED]
2. **RoutePoint**: id, wankId, latitude, longitude, altitude, timestamp, accuracyMeters  - [PLANNED]
3. **PhotoAttachment**: id, walkId, localUri, latitude, longitude, capturedAt, caption  - [PLANNED]
4. **NoteAttachment**: id, walkId, text, latitude, longitude, createdAt  - [PLANNED]
