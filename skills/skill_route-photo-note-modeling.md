---
name: route-photo-note-modeling
description: Models Walk, RoutePoint, PhotoAttachment, and NoteAttachment domain structures.
---

### DOMAIN ENTITY DEFINITIONS
- `Walk`: id, startTime, endTime, totalDistanceMeters, durationSeconds, title, summary.
- `RoutePoint`: id, walkId, latitude, longitude, altitude, timestamp, accuracyMeters.
- `PhotoAttachment`: id, walkId, localUri, latitude, longitude, capturedAt, caption.
- `NoteAttachment`: id, walkId, text, latitude, longitude, createdAt.
