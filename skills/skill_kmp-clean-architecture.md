---
name: kmp-clean-architecture
description: Enforces Kotlin Multiplatform Clean Architecture, UDF/MVVM, StateFlow, Coroutines, and minimal expect/actual boundaries.
---

### ARCHITECTURE PRINCIPLES
1. **Layering**:
   - `domain`: Pure Kotlin entities, repositories interfaces, and use cases (no UI or platform dependencies).
   - `data`: Repository implementations, Room KMP database, DAOs, data sources, and Ktor client.
   - `presentation`: Compose Multiplatform UI, ViewModels (MVVM/UDF), and UI state models (`StateFlow`).
2. **State Management**:
   - Unidirectional Data Flow (UDF) with immutable data classes for UI states.
   - Events emitted from UI to ViewModel; state exposed via `StateFlow`.
3. **Platform Abstractions**:
   - Minimize `expect`/`actual`. Prefer interfaces defined in `commonMain` and implemented in platform sources.
