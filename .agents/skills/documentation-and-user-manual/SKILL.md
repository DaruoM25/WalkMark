---
name: documentation-and-user-manual
description: >-
  technical-documentation (v1.0.0) - Status: active, Verdict: ALLOW
---

# Skill: documentation-and-user-manual

> **Version**: `1.0.0`  
> **Lifecycle Status**: `active`  
> **Promotion State**: `active`  
> **Knowledge State**: `valid`  
> **Runtime Enforcement Verdict**: **`ALLOW`**  
> **Criticality**: `high`

## Dependencies & Triggers
- **Dependencies**: None
- **Triggers**: docs, user-manual, changelog, architecture-doc

## Operational Constraints
- If Verdict is **BLOCKED**, execution of this skill will be denied.
- If Verdict is **WARN**, agent must log a warning before executing.
- If Verdict is **ALLOW**, skill is validated and clear for execution.
