# Testing Strategy - [IMPLEMENTED]

## Foundation Tests - [VERIFIED]
1. **Common Domain Tests**: `SampleItemTest`, `SampleEntityMappingTest` via `kotlin.test`.
2. **ViewModel UTF Tests**: `JournalViewModelTest` via `app.cash.turbine:turbine` and `kotlinx.coroutines.test`.
3. **Compose UISmoke Tests**: `JournalComposeSmokeTest` asserting semantic selectors and state immutability.

## Planned Feature Tests - [PLANNED]
1. GPS Background Tracking & location filtering tests  - [PLANNED]
2. 3-free-walk quota and 4th-save paywall gate tests  - [PLANNED]
3. Room automated migration tests on SQLite  - [PLANNED]
