# Retry & RCA Protocol

## Objective
Enforce disciplined root-cause analysis and stop execution before looping or making opportunistic changes.

## Per-Gate Execution Budget
- `MAX_COMMANDS_PER_GATE` = 12
- `MAX_DURATION_PER_GATE_SECONDS` = 300
- `IDENTICAL_RETRY_MAX` = 2
- `REMEDIATION_ATTEMPTS_MAX` = 2

Budget timer and counter start when gate execution begins.

## Escalation Protocol on Budget Exhaustion
When any limit is reached, STOP CURRENT GATE immediately and return:
- `GATE_STATUS` = BLOCKED/FAIL
- `COMMAND_COUNT` = <count>
- `ELAPSED_TIME` = <seconds>
- `IDENTICAL_RETRIES` = <count>
- `REMEDIATION_ATTEMPTS` = <count>
- `ROOT_CAUSE` = <Hypothesis with proof>
- `LAST_SUCCESSFUL_STATE` = <State summary>
- `NEXT_ACTION` = <Proposed options>
- `HUMAN_DECISION_REQUIRED` = YES
No additional diagnostic exploration is permitted after budget exhaustion.
