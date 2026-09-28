---
name: mobile-ci-cd
description: >-
  build-ci-cd (v1.0.0) - Status: active, Verdict: ALLOW
---

# Skill: mobile-ci-cd

> **Version**: `1.0.0`  
> **Lifecycle Status**: `active`  
> **Promotion State**: `active`  
> **Knowledge State**: `valid`  
> **Runtime Enforcement Verdict**: **`ALLOW`**  
> **Criticality**: `medium`

## Dependencies & Triggers
- **Dependencies**: None
- **Triggers**: gradle, ci-cd, version-catalog, build

## Operational Constraints
- If Verdict is **BLOCKED**, execution of this skill will be denied.
- If Verdict is **WARN**, agent must log a warning before executing.
- If Verdict is **ALLOW**, skill is validated and clear for execution.
