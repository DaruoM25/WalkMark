# Testing Strategy - [VERIFIED]

## Executed & Verified Test Suites - [VERIFIED]
1. **N1 Common Compile & Domain Tests**:
   - `WalkDomainModelsTest`: Domain model integrity for `Walk`, `Waypoint`, `NotePhoto` - [VERIFIED]
   - `SerializationAndMappingTest`: DTO and kotlinx.serialization round-trip mapping - [VERIFIED]
   - `SampleItemTest`, `SampleEntityMappingTest`: Legacy baseline domain checks - [VERIFIED]
2. **N2 Database & Compose UI Tests**:
   - `WalkRoomDatabaseTest`: In-memory Room KMP CRUD, Flow observations, and deletion - [VERIFIED]
   - `RoomDatabaseSmokeTest`: Baseline Room lifecycle and table operations - [VERIFIED]
   - `JournalViewModelTest`: UDF/MVVM StateFlow transitions via Turbine - [VERIFIED]
   - `JournalComposeRealUiTest`: Compose UI rendering, semantic testTag assertions, and exact text verification (`WalkMark: Ready for Shipaton`) - [VERIFIED]
3. **N3a Android APK & Tests**:
   - `./gradlew testDebugUnitTest`: Android unit test runner (Robolectric SDK 34) - [VERIFIED]
   - `./gradlew assembleDebug`: Android debug APK compilation - [VERIFIED]
4. **N3b iOS Multiplatform Framework**:
   - Verified via CI workflow `.github/workflows/ci.yml` on `macos-14` runner (`linkDebugTestIosSimulatorArm64`) - [VERIFIED]

