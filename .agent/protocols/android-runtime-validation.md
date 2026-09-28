# Android Runtime Truth & Deterministic Recovery Protocol

## Independent Facts Model
The following are independent facts and must NEVER be inferred from one another:
1. `ADB_DAEMON_RUNNING` (`adb devices` responds)
2. `ADB_LEASE_ACQUIRED` (lease token held)
3. `DEVICE_PRESENT` (serial listed in `adb devices`)
4. `DEVICE_READY` (`sys.boot_completed == 1`)
5. `AVD_IDENTITY_VERIFIED` (`adb emu avd name` matches configuration)
6. `EMULATOR_PROCESS_RUNNING` (process active on host)
7. `HEADLESS_EMULATOR_RUNNING` (running with `-no-window`)
8. `VISIBLE_EMULATOR_RUNNING` (running with GUI/window enabled)
9. `APP_FOREGROUND` (`dumpsys window | grep mCurrentFocus` matches package)
10. `UI_HEALTHY` (no ANR or crash dialogs)

## Deterministic Recovery Sequence
1. Inspect state once (`adb devices`).
2. Acquire ADB lease.
3. Discover serial and verify AVD name.
4. If absent, launch visible AVD once (visible emulator mandatory for UI acceptance).
5. Wait maximum 120 seconds for `sys.boot_completed == 1`.
6. Explicitly start target activity.
7. Verify foreground window.
8. Execute requested Android gate.
9. Release ADB lease.

## Forbidden Actions During Android Validation
- Monkey / random screen taps
- Repeated keyevents / input loops
- Launcher force-stop
- Arbitrary ADB reconnect loops
- Runtime SDK / path mutations
If deterministic recovery fails: `ANDROID_GATE = ENVIRONMENT_BLOCKED`.
