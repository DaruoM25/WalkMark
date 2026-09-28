---
name: compose-adaptive-ui
description: Implements adaptive Material 3 UI layouts for smartphones, tablets, iPads, and foldables with semantic testability.
---

### ADAPTIVE LAYOUT RULES
1. **WindowSizeClass**:
   - Compact: Single-pane mobile layout (bottom navigation or top app bar).
   - Medium / Expanded: Two-pane layout with map on left and journal/memories on right.
   - Foldable: Support folding posture and hinge separation.
2. **Material 3**:
   - Use dynamic color theming, standardized typography, and M3 components.
3. **Accessibility & Testability**:
   - Every interactive element must include semantic `testTag` and `contentDescription`.
