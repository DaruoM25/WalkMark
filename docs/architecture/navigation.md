# WalkMark — Navigation & Adaptive Layouts

## Foundation Screen - [IMPLEMENTED]
- `JournalScreen`: Smoke screen verifying Material 3 theming, UDF state binding, and semantic testTag / contentDescription selectors.

## Root destinations - [IMPLEMENTED]
- `Main`: preserves the existing adaptive tracking and journal composition.
- `Support`: dedicated Help & Support destination with an explicit Back action.
- The root uses local `Main` / `Support` state without a navigation dependency. Repositories, the active recorder, and ViewModels are remembered above destination switching so opening support does not reset an active recording.
- A safe-area top-end action exposes Help & Support in compact and expanded layouts without changing `AdaptiveWalkScaffold`.

## Planned Screens - [PLANNED]
1. **Main Journal View** (History list of past walks)  - [PLANNED]
2. **Active Walk View** (Map + live GPS stats + photo/note capture action)  - [PLANNED]
3. **Walk Detail View** (Route polyline, memories timeline)  - [PLANNED]
4. **Paywall Modal** (`HardPaywallSheet`, dismissible to Main, no purchase enabled)  - [IMPLEMENTED (PHASE 1)]
   - Shown instead of starting a walk when the 3-free-walk quota is exhausted.
   - Dismissal returns to Main and grants nothing; the gate re-evaluates on the next Start Walk.
   - Real store offerings: [PLANNED] (requires an authorized purchase provider).
