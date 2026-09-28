---
name: room-kmp-persistence
description: Implements Room KMP entities, DAOs, and relations with strictly off-UI-thread execution.
---

### PERSISTENCE RULES
1. Entities: `WalkEntity`, `LocationPointEntity`, `PhotoAttachmentEntity`, `NoteAttachmentEntity`.
2. All database transactions and DAO queries must run on `Dispatchers.IO` (never main thread).
3. Expose reactive queries via `Flow`.
