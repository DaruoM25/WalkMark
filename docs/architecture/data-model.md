# WalkMark — Data Model Specification

## Domain Entities & Database Schema
1. **Walk**: id, startTime, endTime, totalDistanceMeters, durationSeconds, title, summary.
2. **RoutePoint**: id, walkId, latitude, longitude, altitude, timestamp, accuracyMeters.
3. **PhotoAttachment**: id, walkId, localUri, latitude, longitude, capturedAt, caption.
4. **NoteAttachment**: id, walkId, text, latitude, longitude, createdAt.

## Persistence Rules
- Storage is 100% local-first via Room KMP on SQLite.
- Database access is strictly confined to Dispatchers.IO.
- Schema changes require version bump and automated migration tests.
