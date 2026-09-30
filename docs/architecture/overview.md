# WalkMark — Architecture Overview

## Summary
WalkMark is a private, local-first GPS wank journaling mobile application built with Kotlin Multiplatform (KMP), Compose Multiplatform, and Room KMP.

## Core Technical Stack
- **Language**: Kotlin 2.0.21 (JDK 17)  - [IMPLEMENTED]
- **Framework**: Compose Multiplatform 1.7.0 (Material 3, adaptive layouts, WindowSizeClass)  - [IMPLEMENTED]
- **Architecture**: Clean Architecture (domain, data, presentation, UDF/MVVM, StateFlow, Coroutines)  - [IMPLEMENTED]
- **Persistence**: Room KMP 2.7.0-alpha11 (local-first, BundledSQLiteDriver, off-UI thread)  - [IMPLEMENTED]
- **Testing**: kotlin.test, Turbine, Coroutines-Test, Compose UI semantic smoke  - [IMPLEMENTED]
- **Maps**: MapLibre Compose + OpenStreetMap  - [PLANNED]
- **Location**: FusedLocationProviderClient + Foreground Service (Android) / CLLocationManager (iOS)  - [PLANNED]
- **Networking**: Ktor Client + Kotlinx.serialization  - [PLANNED]
- **Monetization**: 3 free saved walks + hard paywall on the 4th, enforced by a provider-neutral domain gate  - [IMPLEMENTED (PHASE 1: NO PURCHASE PROVIDER)]
  - RevenueCat SDK and live billing: [PLANNED]. Planned pricing $2.99/week, $19.99/year. The runtime UI never displays these prices in Phase 1 because no provider supplies them.
