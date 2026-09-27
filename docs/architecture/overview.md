# WalkMark — Architecture Overview

## Summary
WalkMark is a private, local-first GPS walk journaling mobile application built with Kotlin Multiplatform (KMP), Compose Multiplatform, and Room KMP.

## Core Technical Stack
- **Language**: Kotlin (JDK 17)
- **Framework**: Compose Multiplatform (Material 3, adaptive layouts, WindowSizeClass)
- **Architecture**: Clean Architecture (domain, data, presentation, UDF/MVVM, StateFlow, Coroutines)
- **Maps**: MapLibre Compose + OpenStreetMap
- **Location**:
  - Android: FusedLocationProviderClient + Foreground Service
  - iOS: CLLocationManager + Background Location Updates
- **Persistence**: Room KMP (local-first, automated migrations, off-UI thread)
- **Networking**: Ktor Client + kotlinx.serialization
- **Monetization**: RevenueCat SDK (3 free saved walks, hard paywall on 4th save: .99/week, .99/year)
- **Testing**: kotlin.test, Compose UI tests, Robolectric
