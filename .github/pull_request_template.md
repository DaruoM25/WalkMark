# WalkMark — Pull Request Template

> Fill every section. Incomplete PRs will be returned.

## Story Identification

| Field | Value |
|-------|-------|
| **Story ID** | |
| **Owner Agent** | |
| **Branch** | `agents/{runtime}/{us}` |
| **Base Commit** | (must be origin/main or descendant) |

## Scope

### Expected Files

List files this PR intends to modify or create:

```
- 
```

### Expected New Files

```
-
```

### Expected Diff Class

`feature` / `refactor` / `hotfix` / `schema` / `migration` / `toolchain`

### High-Conflict Files Touched

List any high-conflict files from `docs/developer/HIGH_CONFLICT_FILES.yaml` that this PR touches. For each, provide:

```
File:
WHY_REQUIRED:
MINIMAL_DIFF:
APPROVAL:
```

## Dependencies & Toolchain

- **Schema changed?** YES/NO — if YES, describe migration + tests
- **Dependencies changed?** YES/NO — if YES, list exact changes
- **Toolchain changed?** YES/NO — if YES, describe

## Tests

### Tests Removed or Weakened

If any existing test is deleted, commented out, disabled, or had its assertion weakened:

```
REMOVED_TEST:
WHY_REMOVED:
REPLACEMENT_TEST:
EQUIVALENT_COVERAGE: YES/NO + justification
HUMAN_APPROVAL: YES
```

If none, state: `NONE`

### Test Evidence Levels

For each verification claim, state:

```
TEST_TARGET:
IMPLEMENTATION_UNDER_TEST:
EVIDENCE_LEVEL: L1 | L2 | L3 | L4
COMMAND:
RESULT:
LIMITATION:
COMMIT_HASH:
```

### Targeted Tests

List tests written/updated for this story:

```
-
```

### Regression Tests

List tests that protect against regression of existing behavior:

```
-
```

## Scope Drift Check

During implementation, were any of these detected?

- Unexpected file: YES/NO
- Unexpected dependency: YES/NO
- Unexpected schema/toolchain: YES/NO

If YES to any, STOP_FOR_REVIEW = YES. Attach review evidence.

## Main Baseline

- **MAIN_COMPILE_BASELINE**: PASS/FAIL/UNKNOWN (PASS0 gate result)
- **Post-merge gate required:** YES

## Evidence

- Evidence manifest: `artifacts/<US>/evidence.yaml` attached? YES/NO
- All PASS claims follow CLAIM→COMMAND→OUTPUT→ASSERTION? YES/NO

## Notes

```
Any open questions, assumptions, or risks.
```

