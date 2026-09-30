# WalkMark — Module Architecture

## Gradle Module Structure
- `:composeApp` - [IMPLEMENTED]
  - `commonMain`: Domain models, Room KMP database & DAOs, Presentation UI & ViewModels.
  - `androidMain`: Android Activity, Application, and Platform Room builder.
  - `iosMain`: iOS UIViewController bridge, Platform Room builder.

## Future Feature Modularization - [PLANNED]
- GPS & Background Location Tracking  - [PLANNED]
- MapLibre Compose + OSM rendering  - [PLANNED]
- RevenueCat SDK & live billing  - [PLANNED]
- 3-free-walk quota gate & hard paywall (provider-neutral, no purchase provider)  - [IMPLEMENTED]
- Photo & Note Attachments  - [PLANNED]
