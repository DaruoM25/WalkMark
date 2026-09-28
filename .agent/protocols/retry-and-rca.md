# Retry & RCA Protocol

## Objective
Enforce disciplined root-cause analysis and stop execution before looping or making opportunistic changes.

## Hard Limits
- `IDENTICAL_RETRY_MAX` = 2
- `REMEDIATION_ATTEMPTS_MAX` = 3

## Escalation Protocol
When limits are reached, STOP immediately and return:
- `ROOT_CAUSE` = <Hypothesis with proof>
- `ATTEMPTS` = <Summary of attempted fixes>
- `OBSERVED_RESULT` = <Current failure state>
- `OPTIONS` = <List of possible next steps>
- `HUMAN_DECISION_REQUIRED` = YES
