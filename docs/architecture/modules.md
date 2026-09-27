# WalkMark — Module Architecture

## Gradle Module Structure
- `:composeApp` (Canonical single-module project; `shared` is strictly forbidden) - [VERIFIED]
  - `src/commonMain/kotlin/com/walkmark/app/`:
    - `ui/`: Compose Multiplatform screens, adaptive layouts, and theming - [VERIFIED]
    - `domain/`: Pure domain models (`Walk`, `Waypoint`, `NotePhoto`) - [VERIFIED]
    - `data/`: Room KMP entities, DAOs, and serialization DTOs - [VERIFIED]
    - `location/`: Multiplatform GPS location tracking abstractions - [VERIFIED]
  - `src/androidMain/`: Android Activity, Application, and Android SQLite database builder - [VERIFIED]
  - `src/iosMain/`: iOS UIViewController bridge, iOS SQLite database builder - [VERIFIED]

## Future Feature Implementations - [PLANNED]
- GPS Background Service and Foreground Notification - [PLANNED]
- MapLibre Compose map rendering and OSM offline tiles - [PLANNED]
- RevenueCat 3-walk quota hard paywall modal - [PLANNED]

