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

2. **Deterministic Android Recovery Sequence**:
- Step 1: Inspect state once (`adb devices`).
- Step 2: Acquire ADB lease.
- Step 3: Discover serial and verify AVD identity (`adb -s <serial> emu avd name`).
- Step 4: If AVD is absent, launch visible AVD once (visible emulator is mandatory for UI acceptance).
- Step 5: Wait maximum 120 seconds for boot completion (`sys.boot_completed == 1`).
- Step 6: Explicitly launch target component.
- Step 7: Verify foreground window.
- Step 8: Execute requested Android gate.
- Step 9: Release ADB lease.
- Forbidden during acceptance recovery: monkey, random screen taps, repeated keyevents, launcher force-stop, arbitrary ADB reconnect loops, interactive verbose emulator launch, runtime SDK/path mutation.
- If deterministic recovery fails: `ANDROID_GATE = ENVIRONMENT_BLOCKED`.

3. **Visual Acceptance Policy**:
- Visible emulator is mandatory for visual acceptance testing.
- `SCREENSHOT_CAPTURE == PASS` does NOT imply `VISUAL_ACCEPTANCE == PASS`.
- A screenshot passes visual acceptance ONLY if:
  - PNG signature is valid and image renders correctly.
  - Expected app component is in foreground and target text/elements are visible.
  - No ANR, system popups, launcher crashes, or unhandled permission dialogs.
  - No clipping, truncation, status bar overlap, or unreadable contrast.
