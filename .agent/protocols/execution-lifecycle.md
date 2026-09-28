# Execution Lifecycle Protocol

## Objective
Enforce a deterministic, auditable 10-step execution lifecycle across all changes.

## Mandatory Lifecycle
`OBSERVE` -> `ANALYZE` -> `PLAN` (Pass 1) -> `HUMAN APPROVAL` -> `PRE-FLIGHT` -> `IMPLEMENT` (Pass 2) -> `VERIFY` -> `COLLECT EVIDENCE` -> `REVIEW` -> `PR READY`.

## Pass 1 Stop Rule
Under Pass 1, only read-only commands (`git status`, `git log`, `git diff`, `cat`, `ls`, `docker ps`, `adb devices`, `gradlew tasks`) are permitted.
NO production code, configuration, or test mutation is allowed until explicit human approval is received.

## Pre-Flight Verification Gate
Before initiating Pass 2 mutations, verify:
- `BRANCH_CORRECT` = PASS
- `WORKTREE_CLEAN` = PASS
- `CONTAINER_CORRECT` = PASS
- `WORKSPACE_CORRECT` = PASS
- `INDEPENDENT_GIT` = PASS
- `REMOTE_AVAILABLE` = PASS
- `JAVA_READY` = PASS
- `ANDROID_SDK_READY` = PASS
- `ADB_LEASE_READY` = PASS
- `AVD_AVAILABLE` = PASS
- `CI_AVAILABLE` = PASS
If any check fails, do NOT proceed with implementation.
