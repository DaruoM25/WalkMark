# ANDROID RUNTIME TRUTH & VISUAL ACCEPTANCE: WalkMark

1. **Independent Android Runtime Truth Model**:
Treat these facts as completely separate and independently verified:
- `ADB_DAEMON_RUNNING`
- `ADB_LEASE_ACQUIRED`
- `DEVICE_PRESENT`
- `DEVICE_READY`
- `AVD_IDENTITY_VERIFIED`
- `EMULATOR_PROCESS_RUNNING`
- `HEADLESS_EMULATOR_RUNNING`
- `VISIBLE_EMULATOR_RUNNING`
- `APP_FOREGROUND`
- `UI_HEALTHY`
Never infer one fact from another without explicit CLI verification.

2. **Visual Acceptance Policy**:
- Visible emulator is mandatory for visual acceptance testing.
- `SCREENSHOT_CAPTURE == PASS` does NOT imply `VISUAL_ACCEPTANCE == PASS`.
- A screenshot passes visual acceptance ONLY if:
  - PNG signature is valid and image renders correctly.
  - Expected app component is in foreground and target text/elements are visible.
  - No ANR, system popups, launcher crashes, or unhandled permission dialogs.
  - No clipping, truncation, status bar overlap, or unreadable contrast.
