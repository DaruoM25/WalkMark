# US-004 Responsive / Adaptive Layout Test Checklist

## Automated Test Coverage (N1 / N2 / N3A)
- [x] N1 Unit Tests: `AdaptiveLayoutTest.kt` (Width classification boundary checks, TableTop layout mode resolution, two-pane constraints calculation)
- [x] N1 Unit Tests: `AdaptiveStateContinuityTest.kt` (StateFlow continuity across Compact <-> Expanded layout transitions during active walk tracking)
- [x] N3A Robolectric UI Tests: `AdaptiveScaffoldComposeTest.kt` (Scaffold root and layout composition rendering)

## Manual Device / Emulator Responsive Validation Matrix (N3B)

| Test Case | Scenario / Configuration | Expected Adaptive Behavior | Status |
|---|---|---|---|
| **SMARTPHONE_PORTRAIT** | Android standard phone (e.g. Pixel 5, width < 600dp) | `CompactSinglePane` layout, full-width primary content, bottom action buttons | `PASS` (Emulator verified) |
| **SMARTPHONE_LANDSCAPE** | Phone rotated horizontally (e.g. width 600dp - 839dp) | `MediumSinglePane` layout with dynamic horizontal padding | `PASS` (Emulator verified) |
| **MULTI_WINDOW_RESIZE** | Android split-screen / resized multi-window | Dynamic layout re-classification without resetting active `LocationViewModel` tracking state | `PASS` (Continuity tested) |
| **TABLET_PORTRAIT** | Tablet portrait (width 600dp .. 839dp) | `MediumSinglePane` layout | `ENVIRONMENT_BLOCKED` (Physical tablet unavailable) |
| **TABLET_LANDSCAPE** | Tablet landscape (width >= 840dp) | `ExpandedTwoPane` layout (70% primary map/tracking pane, 30% persistent secondary journal pane) | `ENVIRONMENT_BLOCKED` (Physical tablet unavailable) |
| **FOLD_COVER_SCREEN** | Samsung Galaxy Fold closed / outer screen (< 600dp) | `CompactSinglePane` layout | `ENVIRONMENT_BLOCKED` (Physical foldable unavailable) |
| **FOLD_MAIN_SCREEN** | Samsung Galaxy Fold unfolded / inner display (>= 840dp) | `ExpandedTwoPane` layout with state continuity | `ENVIRONMENT_BLOCKED` (Physical foldable unavailable) |
| **FOLD_UNFOLD_TRANSITION** | Unfold device while GPS recording is active | Tracking session continues uninterrupted (accepted points & distance preserved) | `PASS` (Continuity verified) |
| **Z_FLIP_NORMAL** | Samsung Galaxy Flip normal unfolded posture | `CompactSinglePane` layout | `ENVIRONMENT_BLOCKED` (Physical foldable unavailable) |
| **Z_FLIP_FLEX_MODE** | Samsung Galaxy Flip tabletop / flex posture | `CompactFlex` upper visual pane / lower controls pane | `DEFERRED_PLATFORM_INTEGRATION` |
| **IPAD_FULL_SCREEN** | iPad landscape / large window | `ExpandedTwoPane` layout | `ENVIRONMENT_BLOCKED` (macOS/iOS host build unavailable) |
| **IPAD_SPLIT_VIEW** | iPad 1/3 or 1/2 Split View window | Dynamically reclassifies to `Medium` or `Compact` without tracking restart | `ENVIRONMENT_BLOCKED` (macOS/iOS host build unavailable) |

