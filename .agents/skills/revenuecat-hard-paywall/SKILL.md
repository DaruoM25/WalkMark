---
name: revenuecat-hard-paywall
description: >-
  monetization-quota (v1.0.0) - Status: active, Verdict: ALLOW
---

# Skill: revenuecat-hard-paywall

> **Version**: `1.0.0`  
> **Lifecycle Status**: `active`  
> **Promotion State**: `active`  
> **Knowledge State**: `valid`  
> **Runtime Enforcement Verdict**: **`ALLOW`**  
> **Criticality**: `critical`

## Dependencies & Triggers
- **Dependencies**: None
- **Triggers**: revenuecat, paywall, subscription, walk-quota

## Operational Constraints
- If Verdict is **BLOCKED**, execution of this skill will be denied.
- If Verdict is **WARN**, agent must log a warning before executing.
- If Verdict is **ALLOW**, skill is validated and clear for execution.
