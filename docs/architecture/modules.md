# WalkMark — Module Architecture

## Module Structure (Planned)
- :core:model (Domain models, zero dependencies)
- :core:database (Room KMP entities, DAOs, migrations)
- :core:location (Platform GPS tracking abstraction)
- :core:map (MapLibre Compose integration)
- :core:monetization (RevenueCat purchase & entitlement management)
- :core:ui (Material 3 theme, components, adaptive layout utilities)
- :feature:tracking (Active walk recording UI & state machine)
- :feature:journal (Walk history, detail view, photo/note timeline)
- :feature:paywall (Subscription modal & billing flows)
- :composeApp (Application entry points for Android and iOS)
