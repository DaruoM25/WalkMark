# RETRY, RCA, BUDGETS & VALIDATION FREEZE: WalkMark

1. **Per-Gate Execution Budget**:
- `MAX_COMMANDS_PER_GATE` = 12
- `MAX_DURATION_PER_GATE_SECONDS` = 300
- `IDENTICAL_RETRY_MAX` = 2
- `REMEDIATION_ATTEMPTS_MAX` = 2
- Budget starts when gate execution begins.
- On budget exhaustion: STOP CURRENT GATE immediately, return structured budget escalation report (`GATE_STATUS`, `COMMAND_COUNT`, `ELAPSED_TIME`, `IDENTICAL_RETRIES`, `REMEDIATION_ATTEMPTS`, `ROOT_CAUSE`, `LAST_SUCCESSFUL_STATE`, `NEXT_ACTION`, `HUMAN_DECISION_REQUIRED = YES`), and perform no additional diagnostic exploration.

2. **Automatic Gate Freeze**:
- When current execution evidence satisfies `CLAIM` -> `COMMAND` -> `OUTPUT` -> `ASSERTION` -> `COMMIT` -> `ENVIRONMENT`, transition status immediately to `FROZEN_PASS` and persist before moving to next gate.
- A frozen gate reopens ONLY if:
  1. `RELEVANT_SOURCE_CHANGED` = YES
  2. `RELEVANT_ENVIRONMENT_CHANGED` = YES
  3. `REGRESSION_EVIDENCE` = YES
- Otherwise: `GATE_EXECUTION = SKIPPED_FROZEN_PASS`.
