# WalkMark — Development Governance V3

> **Version**: 3.0
> **Status**: ACTIVE
> **Canonical source for all governance rules.
> **Last updated**: 2026-09-30

---

## 1. Lifecycle

### 1.1 Phase Overview

Every feature story follows a mandatory, gated lifecycle:

```
PASS0 — REPO HEALTH
  ↓ (REPO_HEALTH = PASS)
PASS1 — OBSERVE / ANALYZE / PLAN     [READ-ONLY]
  ↓ (PLAN_APPROVED)
HUMAN APPROVAL — PLAN
  ↓
PASS2 — PRE-FLIGHT                    [LIMITED MUTATION ALLOWED]
  ↓ (PREFLIGHT_PASS)
HUMAN APPROVAL — IMPLEMENTATION AUTHORIZED
  ↓
PASS3 — IMPLEMENT                     [MUTATION PERMITTED]
  ↓ (IMPLEMENTED)
PASS4 — VERIFY                       [READ-ONLY for production code]
  ↓ (VERIFIED)
INDEPENDENT REVIEW                   [verification-gatekeeper + qa-reviewer]
  ↓ (REVIEW_PASS)
PR READY
  ↓
HUMAN APPROVAL — MERGE
  ↓
MERGED
  ↓
POST-MERGE MAIN GATE
  ↓ (MAIN_VERIFIED = PASS)
FROZEN_PASS
```

### 1.2 Status Taxonomy — Distinct, Non-Convergent

| Status | Meaning | Mutation Allowed |
|--------|---------|-----------------|
| `NOT_STARTED` | Story not yet begun | No |
| `PLAN_APPROVED` | PLAN read+approved by human; implementation NOT yet authorized | No |
| `IMPLEMENTATION_AUTHORIZED` | Human explicitly authorized Pass3 mutations | Yes (Pass3) |
| `IMPLEMENTED` | Code changes written; not yet verified | Yes (Pass3 complete) |
| `EXECUTING` | Gate currently running | Depends on phase |
| `VERIFIED` | Tests + evidence executed on real code; proofs recorded | No (Pass4) |
| `PR_READY` | Evidence collected, independent review passed, scope-clean | No |
| `MERGED` | PR merged to main | No |
| `MAIN_VERIFIED` | Post-merge gate passed (compile + tests) | No |
| `FROZEN_PASS` | Gate validated, evidence archived; reopens only on invalidation | No |
| `FAIL` | Gate failed with evidence | Depends |
| `ENVIRONMENT_BLOCKED` | Runtime/environment unavailable | No |
| `ENVIRONMENT_INCOMPATIBLE` | Environment does not match gate requirements | No |
| `UNVERIFIED` | Evidence missing or invalid | No |
| `NOT_APPLICABLE` | Gate does not apply to this story | No |

**Mandatory rules:**
- `CONFIGURED != EXECUTED != PASS`
- `IMPLEMENTED != VERIFIED`
- `COMPILED != runtime verified`
- Unknown or incomplete evidence → `UNVERIFIED`
- Current evidence overrides historical summary — never downgrade a proven PASS without explicit invalidation evidence

---

## 2. PASS0 — Repository Health Gate (MANDATORY before every feature story)

### 2.1 Purpose

Prove main is healthy before any feature work begins. Block feature work on a broken main.

### 2.2 Required Checks (ALL must be PASS)

| Check | Rule |
|-------|------|
| `ORIGIN_MAIN_HEAD` | `main` exists, reachable, HEAD = 6386460 (or descendant) |
| `MAIN_COMPILE_BASELINE` | Execute: `./gradlew :composeApp:compileDebugKotlinAndroid --no-daemon` — must PASS. Build outputs in ignored dirs (.gradle/, build/, .kotlin/) allowed. |
| `DIRTY_WORKTREE` | No uncommitted modifications in working tree (except ignored paths) |
| `STALE_BRANCH` | Current branch up-to-date vs main; no unexpected ahead/behind |
| `UNMERGED_HOTFIX` | No pending hotfix merge that touches high-conflict files of the story to come |
| `KNOWN_MAIN_REGRESSION` | No known regression on main (issue, evidence non-pass). If yes → BLOCKED, error → dedicated hotfix |
| `WORKSPACE_ISOLATION` | Worktree is a fresh clone or dedicated Git worktree for this story |
| `PARALLEL_STORY_COLLISION` | No other active story touches the same high-conflict files (per STORY_OWNERSHIP.yaml) |

### 2.3 Result

```
REPO_HEALTH = PASS   → feature work may proceed
REPO_HEALTH = BLOCKED → feature implementation MUST NOT START
```

### 2.4 External Defect Rule

If a defect is discovered in main during PASS0 or later:
- Current story = PAUSED
- Defect class = EXTERNAL_REGRESSION
- HOTFIX_REQUIRED = YES
- Feature branch MUST NOT mask the defect
- Dedicated hotfix lifecycle required (Section 11)

---

## 3. PASS1 — Read-Only Observe / Analyze / Plan

### 3.1 Purpose

Understand the codebase, analyze impact, produce a minimal verifiable plan. Zero production code mutation.

### 3.2 Allowed Commands

**READ_ONLY_ALLOWED:**
- `git fetch`, `git status`, `git log`, `git show`, `git diff`, `git branch`, `git remote`
- `cat`, `ls`, `find` (read-only)
- `grep`, text extraction (no file writes)
- Inspection of any existing source file, doc, config (read)
- `./gradlew tasks` — list available tasks
- `./gradlew help` — Gradle help
- `./gradlew --dry-run` — dry-run simulation
- `docker ps`, `adb devices` — environment inspection
- Reading existing evidence files (`artifacts/**/evidence.yaml`)

### 3.3 Forbidden Commands (STRICT)

**MUTATING_FORBIDDEN:**
- `git checkout` that modifies working-tree files (inspection-only `git show` permitted)
- `git restore`, `git stash`, `git merge`, `git rebase`, `git reset`, `git commit`, `git push`
- Any file write: `cat > file`, `>>`, Python write, Node fs.write, `patch`, `sed -i`, `apply`
- Creation of files in `src/**`, `app/**`, `core/**`, `feature/**`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/**`, `libs.versions.toml`
- **Gradle tasks that produce build outputs** (compile, test, assemble) — BUILD_OUTPUT_MUTATION = FORBIDDEN in PASS1

### 3.4 Build Outputs Decision

PASS0 explicitly permits `MAIN_COMPILE_BASELINE` execution (the purpose of PASS0 is proving main health). PASS1 does NOT. If a specific analysis requires a compile in PASS1, it requires separate human authorization for that specific need; this is not the default.

### 3.5 Required Outputs

Each PASS1 must produce:
- `PLAN.md` per story with: objective, scope, expected files, tests planned, risk assessment, open questions, assumptions
- RECOMMENDED_APPROACH (recommended + alternatives)
- Pre-flight readiness list (pre-conditions for Pass2)
- **Zero files written in repo source**

### 3.6 Stop Rule

DO NOT write production code during PASS1. Wait for explicit human validation. Do NOT proceed to PASS2 without PLAN_APPROVED.

---

## 4. PASS2 — Pre-Flight (Limited Mutation)

### 4.1 Purpose

Verify environment readiness before allowing implementation mutations. Some limited mutations permitted (e.g., initial branch setup), but production code implementation NOT yet.

### 4.2 Required Checks (ALL must be PASS)

| Check | Rule |
|-------|------|
| `BRANCH_CORRECT` | On branch `agents/{runtime}/{us}`, based on current main |
| `WORKTREE_CLEAN` | No untracked residue that would be inherited |
| `CONTAINER_CORRECT` | Isolated container runtime active, configured |
| `WORKSPACE_CORRECT` | Target workspace = isolated container, not host, not shared repo |
| `INDEPENDENT_GIT` | Independent clone, not referencing another story's worktree |
| `REMOTE_AVAILABLE` | Remote reachable, fetch ok, push policy verified |
| `JAVA_READY` | JDK 17 available |
| `ANDROID_SDK_READY` | Android SDK present, env configured |
| `ADB_LEASE_READY` | ADB lease acquired if Android runtime required |
| `AVD_AVAILABLE` | AVD target available (version, name matches config) |
| `CI_AVAILABLE` | CI endpoint reachable or `CI_NOT_CONFIGURED` (info-only, not blocking) |

### 4.3 Result

If any check fails → STOP, `PREFLIGHT_BLOCKED`, human decision required (except `CI_NOT_CONFIGURED` which is informational).

---

## 5. PASS3 — Implement

### 5.1 Purpose

Implement the approved plan in verified increments.

### 5.2 Allowed

- Write source code and tests within story scope (files declared in PLAN.md)
- Execute Gradle compile/test for incremental validation
- Update documentation within scope
- Record evidence in `artifacts/<US>/evidence.yaml`

### 5.3 Forbidden

- Modify files OUT_OF_SCOPE without separate human approval
- Modify high-conflict files without WHY_REQUIRED + OWNER_APPROVAL documented
- Introduce fakes/stubs in commonMain without architecture owner judgment (Section 13)
- Solve a pre-existing main bug alongside the story → pause story → dedicated hotfix
- Delete/weaken existing tests without full TEST_DELETION_GUARD (Section 14)

### 5.4 Loop Guard (BINDING — Section 8)

MAX_ATTEMPTS = 2. No third attempt without new human authorization.

### 5.5 Diff Budget / Scope Drift (Section 16)

---

## 6. PASS4 — Verify

### 6.1 Purpose

Execute planned tests, collect evidence, render auditable verdicts.

### 6.2 Allowed

- Execute existing and new tests
- Record evidence (logs, outputs, screenshots for L4)
- Write to `artifacts/**/evidence.yaml`, `reports/**/*`

### 6.3 Forbidden

- Write to `src/**`, `app/**`, `core/**`, `feature/**`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/**`, `libs.versions.toml`
- Modify existing tests to pass (except via TEST_DELETION_GUARD)
- Repair source during VERIFY — if test fails due to source defect: `VERIFY = FAIL` → RCA → STOP → new plan

### 6.4 Evidence Levels — Test Evidence Model (Section 12)

Every verification report MUST include:

```
TEST_TARGET =
IMPLEMENTATION_UNDER_TEST =
EVIDENCE_LEVEL = L1 | L2 | L3 | L4
COMMAND =
RESULT =
LIMITATION =
COMMIT_HASH =
```

---

## 7. Human Approval Boundaries

### 7.1 Mandatory Approval Gates

| Transition | Approval Required |
|------------|-------------------|
| PASS1 → PASS2 | PLAN_APPROVED by human |
| PASS2 → PASS3 | IMPLEMENTATION_AUTHORIZED by human |
| PASS4 → PR | Independent review (verification-gatekeeper + qa-reviewer) |
| PR → MERGE | Human merge approval |
| MERGED → FROZEN_PASS | MAIN_VERIFIED = PASS (automated gate) |

### 7.2 No Coding Before Authorization

Implementation MUST NOT start before PLAN_APPROVED + IMPLEMENTATION_AUTHORIZED.

### 7.3 No External-Regression Masking

No story may silently carry a main defect repair. Section 11.

---

## 8. Loop Guard (BINDING)

### 8.1 Core Rule

The loop counter tracks the OBJECTIVE, not the command.

```
ATTEMPT_OBJECTIVE = description of what the agent is trying to achieve (e.g., "make WalkRepositoryTest pass")
ATTEMPT_COUNT = number of attempts on the same objective
RCA_REQUIRED_AFTER = 1 (RCA mandatory after each failure before next attempt)
MAX_ATTEMPTS = 2
BLOCKED_AFTER = 2 FAILED ATTEMPTS
```

### 8.2 Sequence

```
Attempt 1 → failure → RCA mandatory → Attempt 2 → failure → BLOCKED → human decision required
```

### 8.3 Tool Switching Does NOT Reset Counter

Switching shell → Python → Node → base64 → ... does NOT reset the attempt count. 2 failed attempts, whichever tools were used, and the objective is BLOCKED.

### 8.4 RCA Requirement

After each failed attempt, a documented RCA is required before the next attempt:
- Root cause hypothesis with evidence
- What was tried
- Why it failed (proven, not presumed)

No new attempt without RCA.

### 8.5 Budget Exhaustion

On BLOCKED:
- STOP current gate immediately
- Return structured report: GATE_STATUS, ATTEMPT_COUNT, RCA, LAST_SUCCESSFUL_STATE, NEXT_ACTION, HUMAN_DECISION_REQUIRED = YES
- No further diagnostic exploration permitted

---

## 9. Workspace Isolation

### 9.1 Rule

- Each active story has its own isolated workspace (fresh clone or dedicated Git worktree).
- No agent reads/modifies another story's worktree.
- Host (outside container) is READ-ONLY for source code — host may do Docker, ADB, emulator, artifact transport.
- All source writes happen inside the isolated container workspace.

### 9.2 Pre-Flight Check

Pre-flight verifies workspace identity (path, origin, branch) before Pass3.

### 9.3 Violation

Detected violation → STOP, `WORKSPACE_ISOLATION_VIOLATION`, human review.

---

## 10. Story Ownership Model

### 10.1 Canonical Source

`docs/developer/STORY_OWNERSHIP.yaml` — machine-readable + human-readable.

### 10.2 Each Active Story Defines

- `story_id`
- `owner_agent`
- `owned_domains`
- `owned_files` (where known)
- `shared_high_conflict_files`
- `forbidden_domains`
- `dependencies` on other stories
- `merge_order`
- `authoritative_seam`
- `scope`: OWNED / SHARED / OUT_OF_SCOPE

### 10.3 Out-of-Scope Modification

Any OUT_OF_SCOPE modification requires separate human approval before being integrated.

---

## 11. External Regression / Hotfix Protocol

### 11.1 Detection

At any point, if a story discovers a pre-existing main defect:

```
CURRENT_STORY = PAUSED
DEFECT_CLASS = EXTERNAL_REGRESSION
HOTFIX_REQUIRED = YES
FEATURE_BRANCH_MUST_NOT_MASK_DEFECT = YES
```

### 11.2 Dedicated Hotfix Lifecycle

1. Diagnose with evidence
2. Human approval for the hotfix
3. Fresh branch from main (`hotfix/{description}`)
4. Minimal fix, targeted, with tests if applicable
5. Dedicated PR
6. Merge
7. POST-MERGE MAIN GATE (Section 17)
8. Resume paused stories ONLY after `MAIN_VERIFIED = PASS`

### 11.3 Rule

No unrelated story may silently carry the repair.

---

## 12. Test Evidence Model — L1 to L4

### 12.1 Evidence Levels

| Level | Description | Example Command |
|-------|-------------|-----------------|
| **L1** | Unit / pure logic — domain models, use cases, calculators | `./gradlew :composeApp:testDebugUnitTest` |
| **L2** | Component / controlled fake — ViewModel with fake Repository, DAO with in-memory DB | `./gradlew :composeApp:jvmTest` or `./gradlew :composeApp:testDebugUnitTest` |
| **L3** | Real implementation integration — real Room, real file store, real Compose UI components | `./gradlew :composeApp:connectedCheck` or `./gradlew :composeApp:compileDebugAndroidTestKotlin` + instrumented |
| **L4** | Platform/runtime — AVD, installed app, real behavior, visual capture | AVD launch + instrumentation + screenshot |

### 12.2 Normative Rule

```
FAKE_TEST_PASS != PRODUCTION_IMPLEMENTATION_PASS
```

An L2 test with a fake does NOT validate L3/L4 behavior. Evidence level must match what is being claimed.

### 12.3 When Each Level Is Mandatory

- **L1**: All new logic code (use cases, domain, calculations) — mandatory
- **L2**: All ViewModel / Repository / component with dependencies — mandatory if external interface simulated
- **L3**: Any code interacting with Room, real file storage, GPS, real UI — mandatory
- **L4**: Real UI behavior, visual, navigation, critical user path — strongly recommended; mandatory for visual acceptance criteria

---

## 13. Fake / Stub / Provider Policy

### 13.1 Placement by Source Set

| Source Set | Rule |
|------------|------|
| **commonMain** | Production-valid implementations ONLY. No fakes. No stubs. |
| **commonTest** | Mutable fakes / spies / stubs allowed. Narrow ports preferred over full repository fakes. |
| **androidTest / iosTest** | Platform fakes, instrumentation fakes, platform mocks. |

### 13.2 Provider-Neutral Production Fallback

A provider-neutral production fallback (e.g., `UnavailableSubscriptionManager` returning deterministic "unavailable" without network) is a VALID production implementation in commonMain — it represents real production behavior when a feature is unavailable. This is NOT a fake.

### 13.3 In-Memory Fake

An in-memory fake that simulates external behavior (memory store, fake GPS, fake network) belongs ONLY in test source sets (commonTest / platformTest).

### 13.4 Preference

Narrow ports (minimal interfaces) > full repository fakes.

---

## 14. Test Deletion Guard

### 14.1 Rule

Deleting, commenting out, disabling, or weakening an existing test (assertion made weaker to get green) REQUIRES:

```
REMOVED_TEST = path + description
WHY_REMOVED = reason
REPLACEMENT_TEST = path of new test or alternative proof
EQUIVALENT_COVERAGE = YES/NO + justification
HUMAN_APPROVAL = YES
```

### 14.2 Without This Record → BLOCKED

No test removal or weakening without the full record.

### 14.3 Weakening Assertions Equals Removal

Making an assertion weaker to pass = same as deletion. BLOCKED.

### 14.4 Implementation

Template `artifacts/<US>/evidence.yaml` with field `test_removals` mandatory if tests are modified in Pass3.

---

## 15. High-Conflict File Protection

### 15.1 Canonical Source

`docs/developer/HIGH_CONFLICT_FILES.yaml` — machine-readable registry.

### 15.2 For Each Planned Modification to a High-Conflict File

```
WHY_REQUIRED =
MINIMAL_DIFF =
ACTIVE_STORIES_TOUCHING_FILE =
MERGE_ORDER =
OWNER_APPROVAL =
```

### 15.3 CI Detection

CI can detect unauthorized changes to high-conflict files and annotate the PR. (AUTOMATE_LATER — Phase C)

---

## 16. Diff Budget / Scope Drift

### 16.1 Before Implementation

Declare in PLAN.md:

```
EXPECTED_FILES = list
EXPECTED_NEW_FILES = list
EXPECTED_DIFF_CLASS = feature | refactor | hotfix | schema | migration | toolchain
EXPECTED_HIGH_CONFLICT_FILES = list (may be empty)
```

### 16.2 During Implementation — Detect and Flag

If detected:
- Unexpected file → `SCOPE_DRIFT = YES`
- Unexpected dependency → `SCOPE_DRIFT = YES`
- Unexpected schema/toolchain change → `SCOPE_DRIFT = YES`

### 16.3 Review Trigger

LOC increase ≈ +30% over plan → `REVIEW_TRIGGER = YES` (review signal, not automatic rejection).

### 16.4 Stopping Rule

If `SCOPE_DRIFT = YES` → STOP_FOR_REVIEW = YES. No further implementation without human review.

---

## 17. Post-Merge Main Gate

### 17.1 Mandatory Sequence

```
MERGED
  → POST_MERGE_MAIN_GATE
    → MAIN_COMPILE (mandatory fast)
    → SMOKE_TESTS (mandatory fast)
    → ANDROID_TEST_COMPILE (mandatory for stories touching Android)
  → MAIN_VERIFIED = PASS
  → FROZEN_PASS
```

### 17.2 Known WalkMark Commands

| Gate | Command |
|------|---------|
| `MAIN_COMPILE` | `./gradlew :composeApp:compileDebugKotlinAndroid --no-daemon` |
| `SMOKE_TESTS` | `./gradlew :composeApp:testDebugUnitTest --no-daemon` |
| `ANDROID_TEST_COMPILE` | `./gradlew :composeApp:compileDebugAndroidTestKotlin --no-daemon` |

Exact tasks must be verified with `./gradlew tasks` during a permitted mutable/preflight phase.

### 17.3 Nightly / Deeper

- iOS build where toolchain/runner exists
- Instrumented tests on AVD
- Visual acceptance on visible emulator

These are deeper/optional gates, not the mandatory fast gate.

### 17.4 Normative Rule

```
MERGED != FROZEN_PASS
```

If main fails after merge:
- `MAIN_HEALTH = BLOCKED`
- New feature starts paused
- Dedicated RCA/hotfix required

---

## 18. Integration Owner

### 18.1 Model: CI + HUMAN HYBRID

| Responsibility | Owner |
|----------------|-------|
| Compile, smoke/unit checks, automated guards | CI |
| Merge approval | Human |
| Conflict-resolution judgment | Human |
| Scope/ownership decisions | Human |

### 18.2 Integration Agent

An integration agent may assist with automated checks and coordination, but does NOT independently hold merge authority.

### 18.3 What Integration Owner Owns

- Merge sequencing
- Conflict resolution review (human judgment)
- Main health
- Post-merge verification
- Shared-file inspection after merge
- NO feature invention

---

## 19. Frozen Pass (FROZEN_PASS)

### 19.1 When a Story Is Frozen

A frozen/accepted story may ONLY be reopened for:

- Concrete regression (new evidence shows broken behavior)
- Explicit shared-integration requirement (later story needs compatible change)
- Approved schema/API compatibility change

### 19.2 Reopen Requires

```
REOPEN_REASON =
AFFECTED_ACCEPTED_EVIDENCE = list of impacted frozen gates
NEW_RISK = description
NEW_VERIFICATION = re-validation plan
```

### 19.3 Prohibited

No opportunistic cleanup. No refactoring without functional reason. No "improvements" in a frozen story.

---

## 20. Active Story Migration Rules

### 20.1 US-005 (Antigravity — Walk History / Detail / delete semantics)

- Apply loop guard / evidence discipline / workspace isolation NOW
- Full V3 on next lifecycle boundary
- Do NOT rewrite current branch

### 20.2 US-006 (OpenCode — Monetization / free-walk gate / paywall foundation)

- Apply V3 from current preflight/implementation boundary
- Do NOT rewrite current branch

### 20.3 US-007 (Codex — In-app Help & Support)

- Apply V3 from current implementation/verification boundary
- Do NOT rewrite current branch

### 20.4 General Rule

Do not force valid current work to restart solely because V3 is introduced.

---

## 21. Source Transfer Boundary

Git/GitHub is the ONLY normal inter-agent source transfer mechanism. No agent may assume unpublished work from another agent. Agent workspace isolation violations are BLOCKED.

---

## 22. Git / Branch Conventions

- Branch naming: `agents/{runtime}/{us}`
- Direct push to `main`: PROHIBITED
- Independent clones: ENFORCED
- Docker socket inside containers: PROHIBITED
- Run as non-root: YES

---

## 23. Evidence Requirement

Every `PASS` verdict requires:

```
CLAIM → COMMAND → OUTPUT → ASSERTION → COMMIT → ENVIRONMENT
```

Unsupported claims default to `UNVERIFIED`.

---

## 24. Cross-References

- Canonical governance: `docs/developer/DEVELOPMENT_GOVERNANCE_V3.md` (this file)
- Story ownership: `docs/developer/STORY_OWNERSHIP.yaml`
- High-conflict registry: `docs/developer/HIGH_CONFLICT_FILES.yaml`
- PR template: `.github/pull_request_template.md`
- CI workflows: `.github/workflows/` (Phase C — not yet created)

