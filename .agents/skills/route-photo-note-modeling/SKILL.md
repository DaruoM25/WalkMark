---
name: route-photo-note-modeling
description: >-
  domain-modeling (v1.0.0) - Status: active, Verdict: ALLOW
---

# Skill: route-photo-note-modeling

> **Version**: `1.0.0`  
> **Lifecycle Status**: `active`  
> **Promotion State**: `active`  
> **Knowledge State**: `valid`  
> **Runtime Enforcement Verdict**: **`ALLOW`**  
> **Criticality**: `high`

## Dependencies & Triggers
- **Dependencies**: None
- **Triggers**: walk-model, photo-attachment, note-attachment, route-point

## Operational Constraints
- If Verdict is **BLOCKED**, execution of this skill will be denied.
- If Verdict is **WARN**, agent must log a warning before executing.
- If Verdict is **ALLOW**, skill is validated and clear for execution.
