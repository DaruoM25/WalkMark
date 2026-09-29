# iOS Platform - [IMPLEMENTED]

## Application Baseline
- Targets: iosArm64 & iosSimulatorArm64 - [IMPLEMENTED]
- MainViewController.kt ComposeUIViewController bridge - [IMPLEMENTED]
- Native SQLite database builder - [IMPLEMENTED]
- LocationTrackerManager.ios.kt wrapping CLLocationManager - [IMPLEMENTED]

## Platform Configuration & Permissions (Info.plist / Host App)
When configuring the host Xcode project / iOS bundle packaging, the following keys must be included in Info.plist:

### 1. Location Permissions
`xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>WalkMark a besoin d'accéder à votre position pour enregistrer le tracé de votre marche.</string>
`

### 2. Background Location Mode
Required by CLLocationManager.allowsBackgroundLocationUpdates = true with showsBackgroundLocationIndicator = true:
`xml
<key>UIBackgroundModes</key>
<array>
    <string>location</string>
</array>
`

> **Privacy Note**: NSLocationAlwaysAndWhenInUseUsageDescription is NOT required because WalkMark uses the active-session background tracking model via equestWhenInUseAuthorization() with the system blue location indicator bar while recording is in progress.
