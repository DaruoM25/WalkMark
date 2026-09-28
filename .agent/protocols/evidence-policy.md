# Evidence Policy & Manifest Protocol

## Objective
Ensure all verdicts are grounded in verifiable execution evidence.

## Evidence Pattern
Every `PASS` must strictly follow:
`CLAIM` -> `COMMAND` -> `OUTPUT` -> `ASSERTION`

## Status Model Rules
Allowed statuses:
`NOT_STARTED`, `PLANNED`, `IMPLEMENTED`, `EXECUTING`, `PASS`, `FAIL`, `ENVIRONMENT_BLOCKED`, `ENVIRONMENT_INCOMPATIBLE`, `NOT_APPLICABLE`, `UNVERIFIED`, `FROZEN_PASS`.
- `CONFIGURED != EXECUTED != PASS`
- `IMPLEMENTED != VERIFIED`
- `COMPILED != runtime verified`
- Unknown or incomplete evidence must be reported as `UNVERIFIED`.

## Canonical Evidence Manifest
All per-US evidence must be recorded in `artifacts/<US>/evidence.yaml`:
```yaml
us: US-XXX
commit: <sha>
branch: agents/<runtime>/US-XXX
runtime: antigravity
n1:
  status: PASS|FAIL|UNVERIFIED
  evidence: <command + output>
n2:
  status: PASS|FAIL|UNVERIFIED
  evidence: <command + output>
n3a:
  emulator:
    avd: <avd_name>
    serial: <device_serial>
    visible: true|false
  instrumentation:
    status: PASS|FAIL|UNVERIFIED
    tests: <count>
    failures: 0
  visual:
    status: PASS|FAIL|UNVERIFIED
    screenshot: artifacts/<US>/screenshot.png
n3b:
  status: PASS|FAIL|UNVERIFIED
  ci_run: <url_or_id>
review:
  status: PASS|FAIL|UNVERIFIED
```
