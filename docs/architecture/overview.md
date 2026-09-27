# WalkMark — Architecture Overview

## Summary
WalkMark is a private, local-first GPS walk journaling mobile application built with Kotlin Multiplatform (KMP), Compose Multiplatform, and Room KMP.

## Core Technical Stack
- **Language**: Kotlin 2.0.21 (JDK 17) - [VERIFIED]
- **Framework**: Compose Multiplatform 1.7.0 (Material 3, adaptive layouts, WindowSizeClass) - [VERIFIED]
- **Architecture**: Clean Architecture (domain, data, ui, location, UDF/MVVM, StateFlow, Coroutines) - [VERIFIED]
- **Persistence**: Room KMP 2.7.0-alpha11 + Bundled SQLite 2.5.0-alpha11 (local-first, BundledSQLiteDriver, off-UI thread) - [VERIFIED]
- **Testing**: kotlin.test, Turbine, Coroutines-Test, Compose UI semantic smoke, Robolectric - [VERIFIED]
- **Maps**: MapLibre Compose + OpenStreetMap - [PLANNED]
- **Location**: LocationTracker abstraction + Android FusedLocationProviderClient / iOS CLLocationManager - [IMPLEMENTED]
- **Serialization & Time**: kotlinx-serialization 1.7.3, kotlinx-datetime 0.6.1 - [VERIFIED]
- **Monetization**: RevenueCat Purchases KMP (3-free-walk quota, hard paywall on 4th save: $2.99/week, $19.99/year) - [PLANNED]

