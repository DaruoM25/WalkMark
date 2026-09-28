# Android Runtime Truth Protocol

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

## Step Sequence for N3a Validation
1. Acquire ADB lease.
2. Check ADB daemon and list devices.
3. Query AVD name: `adb -s <serial> emu avd name`.
4. If UI visual acceptance is required, enforce `VISIBLE_EMULATOR_RUNNING == true`.
5. Wait for `sys.boot_completed == 1`.
6. Explicitly start target activity (never rely on monkey for UI acceptance).
7. Verify foreground window.
8. Run instrumentation / connected checks.
9. Release ADB lease.
