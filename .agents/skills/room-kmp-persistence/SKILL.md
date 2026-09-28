---
name: room-kmp-persistence
description: >-
  local-persistence (v1.0.0) - Status: active, Verdict: ALLOW
---

# Skill: room-kmp-persistence

> **Version**: `1.0.0`  
> **Lifecycle Status**: `active`  
> **Promotion State**: `active`  
> **Knowledge State**: `valid`  
> **Runtime Enforcement Verdict**: **`ALLOW`**  
> **Criticality**: `critical`

## Dependencies & Triggers
- **Dependencies**: None
- **Triggers**: room, database, dao, entity

## Operational Constraints
- If Verdict is **BLOCKED**, execution of this skill will be denied.
- If Verdict is **WARN**, agent must log a warning before executing.
- If Verdict is **ALLOW**, skill is validated and clear for execution.
