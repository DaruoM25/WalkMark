# WalkMark Project Context (v2)

## Overview
WalkMark is a privacy-first, local-first GPS walk journaling mobile application designed for Android and iOS using Kotlin Multiplatform (KMP), Compose Multiplatform, and Room KMP.

## Core Architectural Invariants
- **Canonical Module**: `:composeApp` (the name `shared` is strictly forbidden).
- **Clean Architecture Layers in `commonMain`**:
  - `ui/`: Compose Multiplatform screens, theme, and adaptive components.
  - `domain/`: Business entities (`Walk`, `Waypoint`, `NotePhoto`) and use cases.
  - `data/`: Room KMP entities, DAOs, DTOs, and serialization mappers.
  - `location/`: GPS abstractions (`LocationTracker`, `LocationPoint`, `TrackingState`).
- **Platform Separation**:
  - `androidMain`: Android Activity, application lifecycle, Android SQLite database builder.
  - `iosMain`: iOS MainViewController, Swift bridge, iOS SQLite database builder.

## Validated Stack Versions
- Kotlin: `2.0.21` (JDK 17) - [VERIFIED]
- Compose Multiplatform: `1.7.0` - [VERIFIED]
- Room KMP: `2.7.0-alpha11` - [VERIFIED]
- SQLite: `2.5.0-alpha11` - [VERIFIED]
- Coroutines: `1.9.0` - [VERIFIED]
- DateTime: `0.6.1` - [VERIFIED]
- Serialization: `1.7.3` - [VERIFIED]
- MapLibre Compose: `0.16.0` - [IMPLEMENTED]
- RevenueCat Purchases KMP: `3.9.0` - [IMPLEMENTED]

## Verification Status
- **N1**: Domain model creation, serialization round-trip, and architecture boundary tests - [VERIFIED]
- **N2**: Room in-memory database CRUD, Compose UI rendering, semantic assertions, adaptive layout - [VERIFIED]
- **N3a**: Android real device / emulator execution - [PLANNED]
- **N3b**: iOS simulator compilation and framework link verified via macOS CI workflow (`.github/workflows/ci.yml`) - [PLANNED]

