# Validation Freeze & Automatic Freeze Protocol

## Objective
Preserve verified stability, prevent regression loops, and lock gates immediately upon valid execution.

## Automatic Freeze Rule
When current execution evidence satisfies:
`CLAIM` -> `COMMAND` -> `OUTPUT` -> `ASSERTION` -> `COMMIT` -> `ENVIRONMENT`
The gate status MUST transition immediately to `FROZEN_PASS` and be persisted to `artifacts/<US>/evidence.yaml`.

## Freeze Invalidation Triggers
A gate marked `FROZEN_PASS` can reopen ONLY if:
1. `RELEVANT_SOURCE_CHANGED` = YES
2. `RELEVANT_ENVIRONMENT_CHANGED` = YES
3. `REGRESSION_EVIDENCE` = YES
Otherwise, gate execution is strictly `SKIPPED_FROZEN_PASS`.

## No Ad-Hoc Runtime Mutation Rule
Validation must NEVER repair runtime prerequisites ad hoc (e.g. creating adb symlinks, installing runtime packages, editing PATH, altering SDK layout). If a runtime prerequisite is missing, classify as `RUNTIME_IMAGE_FIX` or `ENVIRONMENT_BLOCKED` and escalate.
