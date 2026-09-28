---
name: room-kmp-migrations
description: Enforces mandatory Room KMP automated migrations with migration tests.
---

### MIGRATION RULES
1. Every schema change must increment `version` and supply an explicit `Migration` or `AutoMigration`.
2. Destructive fallback (`fallbackToDestructiveMigration`) is strictly forbidden in production.
3. Migration test must be written and pass before schema change sign-off.
