# Testing Levels & Preservation Protocol

## Testing Levels
1. **N1 (Unit)**: Fast JVM/KMP domain & logic tests (`./gradlew test`).
2. **N2 (Component)**: Compose UI / Room repository unit/integration tests (`./gradlew jvmTest`).
3. **N3a (Device/Emulator)**: Room migrations, GPS tracking, and Compose UI on actual Android runtime (`./gradlew connectedCheck`).
4. **N3b (CI Pipeline)**: Complete continuous integration run on isolated remote runner.

## Test Preservation Policy
- Tests must NEVER be deleted, commented out, disabled, or made lenient to force a PASS.
- Test removals require explicit human approval with:
  - `TEST_REMOVAL_REASON` = ...
  - `REPLACEMENT_COVERAGE` = ...
  - `HUMAN_APPROVAL` = YES
