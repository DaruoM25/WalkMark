# Evidence Policy & Manifest Protocol

## Objective
Ensure all verdicts are grounded in verifiable execution evidence.

## Current-Evidence-First Evaluation Rule
Before executing a verification gate:
1. Verify canonical workspace, current branch, and current commit.
2. Inspect `artifacts/<US>/evidence.yaml` and relevant artifacts/logs.
3. Check if gate evidence matches current commit and is valid.
4. If valid: Declare `SKIP_EXECUTION` and transition to `FROZEN_PASS`.
5. If missing or invalid: Proceed with bounded execution.

## Current State Overrides Historical Summary
A retrospective or old execution log may NEVER downgrade a currently proven `PASS` to `UNVERIFIED` without explicit evidence of invalidation. Current valid evidence takes precedence.

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
