---
name: gps-background-tracking
description: Manages battery-efficient background GPS walk tracking on Android (Foreground Service) and iOS (CLLocationManager).
---

### LOCATION TRACKING RULES
1. **Android**: Use `FusedLocationProviderClient` with a dedicated Foreground Service and persistent notification while a walk is active.
2. **iOS**: Use `CLLocationManager` with `allowsBackgroundLocationUpdates = true` and `pausesLocationUpdatesAutomatically = false`.
3. **Filtering**: Apply distance and accuracy filters to minimize battery drain.
4. **Safety**: Stop location updates immediately when the user stops or cancels the walk.
