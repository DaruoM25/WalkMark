# WalkMark — Navigation & Adaptive Layouts

## Foundation Screen - [IMPLEMENTED]
- `JournalScreen`: Smoke screen verifying Material 3 theming, UDF state binding, and semantic testTag / contentDescription selectors.

## Planned Screens - [PLANNED]
1. **Main Journal View** (History list of past walks)  - [PLANNED]
2. **Active Walk View** (Map + live GPS stats + photo/note capture action)  - [PLANNED]
3. **Walk Detail View** (Route polyline, memories timeline)  - [PLANNED]
4. **Paywall Modal** (`HardPaywallSheet`, dismissible to Main, no purchase enabled)  - [IMPLEMENTED (PHASE 1)]
   - Shown instead of starting a walk when the 3-free-walk quota is exhausted.
   - Dismissal returns to Main and grants nothing; the gate re-evaluates on the next Start Walk.
   - Real store offerings: [PLANNED] (requires an authorized purchase provider).
