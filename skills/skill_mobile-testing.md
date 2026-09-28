---
name: mobile-testing
description: Defines testing standards for kotlin.test unit tests, Room DAO integration tests, and Compose UI tests.
---

### TESTING STANDARDS
1. **Unit Tests**: Test all UseCases and ViewModels with `kotlinx-coroutines-test` and `Turbine` for StateFlow assertions.
2. **Database Tests**: Test Room DAOs and migration paths on SQLite.
3. **UI Tests**: Test Compose screens using semantic `testTag` and `onNodeWithTag` assertions.
