# WalkMark — Agent Governance & System Roster

> **Project Type**: mobile-kmp
> **Base Branch**: `main`
> **Repository**: `DaruoM25/WalkMark`
> **Governance Version**: V3 (canonical: `docs/developer/DEVELOPMENT_GOVERNANCE_V3.md`)
> **Status**: ACTIVE

---

## 1. Governance

### 1.1 Canonical Sources

| Artifact | Location |
|----------|---------|
| **Governance (canonical)** | `docs/developer/DEVELOPMENT_GOVERNANCE_V3.md` |
| **Story ownership** | `docs/developer/STORY_OWNERSHIP.yaml` |
| **High-conflict registry** | `docs/developer/HIGH_CONFLICT_FILES.yaml` |
| **PR template** | `.github/pull_request_template.md` |
| **Agent roster** | This file (AGENTS.md) |
| **Project map** | `PROJECT_MAP.md` |

> Full lifecycle, PASS gates, loop guard, evidence model, test deletion guard, fake/provider policy, frozen pass, post-merge gate, migration rules are specified in **DEVELOPMENT_GOVERNANCE_V3.md**. This file contains only the entry rules.

### 1.2 Lifecycle Summary

```
PASS0 — REPO HEALTH
  ↓ (REPO_HEALTH = PASS)
PASS1 — OBSERVE / ANALYZE / PLAN     [READ-ONLY]
  ↓ (PLAN_APPROVED)
HUMAN APPROVAL — PLAN
  ↓
PASS2 — PRE-FLIGHT
  ↓ (PREFLIGHT_PASS)
HUMAN APPROVAL — IMPLEMENTATION AUTHORIZED
  ↓
PASS3 — IMPLEMENT
  ↓ (IMPLEMENTED)
PASS4 — VERIFY
  ↓ (VERIFIED)
INDEPENDENT REVIEW
  ↓ (REVIEW_PASS)
PR READY
  ↓
HUMAN APPROVAL — MERGE
  ↓
MERGED → POST-MERGE MAIN GATE → MAIN_VERIFIED → FROZEN_PASS
```

### 1.3 Mandatory Rules (Condensed)

1. **PASS0 is mandatory before every feature story.** Main compile baseline must be proven (`./gradlew :composeApp:compileDebugKotlinAndroid --no-daemon`). If main is broken, feature work pauses; dedicated hotfix required.
2. **PASS1 is strictly read-only.** No code mutation, no build output generation, no file writes. `./gradlew tasks` / `help` / `--dry-run` only.
3. **No coding before human authorization.** PLAN_APPROVED + IMPLEMENTATION_AUTHORIZED required before Pass3.
4. **Loop guard: MAX_ATTEMPTS = 2.** One objective, two attempts max. RCA required after each failure. Tool switching does NOT reset the counter. BLOCKED after 2 failed attempts — human decision required.
5. **Workspace isolation.** Each story gets a fresh clone or dedicated Git worktree. Host is read-only for source. All writes in isolated container.
6. **No external-regression masking.** If a story discovers a pre-existing main defect, the story pauses. Dedicated hotfix required. No story silently carries the fix.
7. **Git/GitHub is the only normal inter-agent source transfer.** No agent assumes unpublished work from another agent.
8. **Evidence requirement.** Every PASS requires CLAIM → COMMAND → OUTPUT → ASSERTION → COMMIT → ENVIRONMENT. Unsupported claims = UNVERIFIED.
9. **Frozen pass.** A frozen/accepted story reopens ONLY for concrete regression, explicit integration requirement, or approved schema/API compatibility change. No opportunistic cleanup.
10. **Test deletion guard.** Deleting, disabling, or weakening existing tests requires REMOVED_TEST / WHY_REMOVED / REPLACEMENT_TEST / EQUIVALENT_COVERAGE / HUMAN_APPROVAL.

---

## 2. Agent Roster

| ID | Display Name | Type | Runtime | Criticality | Approval Authority |
|---|---|---|---|---|---|
| `walkmark-po-orchestrator` | **WalkMark Product Owner & Orchestrator** | `orchestrator` | `antigravity` | `critical` | `Yes` |
| `kmp-architecture-engineer` | **KMP Architecture Engineer** | `specialist` | `antigravity` | `critical` | `No` |
| `map-location-engineer` | **Map & Location Engineer** | `specialist` | `antigravity` | `high` | `No` |
| `compose-ui-ux-engineer` | **Compose UI/UX Engineer** | `specialist` | `antigravity` | `high` | `No` |
| `local-data-engineer` | **Local Data & Persistence Engineer** | `specialist` | `antigravity` | `critical` | `No` |
| `revenuecat-monetization-engineer` | **RevenueCat Monetization Engineer** | `specialist` | `antigravity` | `high` | `No` |
| `qa-automation-engineer` | **QA Automation Engineer** | `specialist` | `antigravity` | `high` | `No` |
| `mobile-security-privacy-engineer` | **Mobile Security & Privacy Engineer** | `specialist` | `antigravity` | `critical` | `No` |
| `mobile-devops-engineer` | **Mobile DevOps & Build Engineer** | `specialist` | `antigravity` | `medium` | `No` |
| `walkmark-documentation-engineer` | **WalkMark Documentation Engineer** | `specialist` | `antigravity` | `medium` | `No` |
| `qa-reviewer` | **QA Reviewer & Quality Gate** | `reviewer` | `antigravity` | `critical` | `Yes` |

## 3. Detailed Responsibilities & Scopes

### Agent: `walkmark-po-orchestrator` (WalkMark Product Owner & Orchestrator)

- **Type**: `orchestrator`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `YES`
- **Delegates To**: `kmp-architecture-engineer`, `map-location-engineer`, `compose-ui-ux-engineer`, `local-data-engineer`, `revenuecat-monetization-engineer`, `qa-automation-engineer`, `mobile-security-privacy-engineer`, `mobile-devops-engineer`, `walkmark-documentation-engineer`, `qa-reviewer`
- **Required Protocols**: `00-request-normalization`, `10-planning`, `50-verification`
- **Required Skills**: `kmp-clean-architecture`, `documentation-and-user-manual`

#### Responsibilities:
- Decompose product requirements into verifiable user stories.
- Coordinate specialist agent investigation during Pass 1.
- Consolidate implementation plans, test matrices, and risk assessments.
- Enforce strict human approval gate before Pass 2 implementation begins.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `AGENTS.md`, `PROJECT_MAP.md`, `docs/**/*`
- **Deny Scopes**: *None*

### Agent: `kmp-architecture-engineer` (KMP Architecture Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `10-planning`, `20-execution`, `30-surgical-edit`
- **Required Skills**: `kmp-clean-architecture`, `offline-first-local-storage`

#### Responsibilities:
- Define and maintain KMP Clean Architecture module boundaries.
- Enforce commonMain and platform-specific code separation.
- Implement UDF/MVVM patterns with StateFlow and Coroutines.
- Govern expect/actual usage strictly only when justified.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonMain/**/*`, `androidMain/**/*`, `iosMain/**/*`, `docs/architecture/**/*`
- **Deny Scopes**: *None*

### Agent: `map-location-engineer` (Map & Location Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `high`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`, `40-rca`
- **Required Skills**: `maplibre-mapping`, `gps-background-tracking`, `route-photo-note-modeling`

#### Responsibilities:
- Integrate MapLibre Compose and OpenStreetMap tiles.
- Implement GPS route recording with accuracy and battery trade-offs.
- Implement Android FusedLocationProviderClient and Foreground Service.
- Implement iOS CLLocationManager background location updates.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonMain/**/*`, `androidMain/**/*`, `iosMain/**/*`
- **Deny Scopes**: *None*

### Agent: `compose-ui-ux-engineer` (Compose UI/UX Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `high`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`
- **Required Skills**: `compose-adaptive-ui`, `route-photo-note-modeling`

#### Responsibilities:
- Build Compose Multiplatform Material 3 responsive UI.
- Implement adaptive layouts for smartphones, tablets, iPads, and foldables.
- Implement two-pane large-screen map and journal layout.
- Ensure accessibility and semantic testTag / contentDescription testability.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonMain/src/commonMain/compose/**/*`, `commonMain/**/*`
- **Deny Scopes**: *None*

### Agent: `local-data-engineer` (Local Data & Persistence Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`
- **Required Skills**: `room-kmp-persistence`, `room-kmp-migrations`, `offline-first-local-storage`

#### Responsibilities:
- Design and maintain Room KMP entities, DAOs, and relations.
- Enforce mandatory automated database migrations.
- Guarantee local-first persistence without network dependencies.
- Ensure all database operations run strictly off the main/UI thread.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonMain/src/commonMain/db/**/*`, `commonMain/**/*`, `androidMain/**/*`, `iosMain/**/*`
- **Deny Scopes**: *None*

### Agent: `revenuecat-monetization-engineer` (RevenueCat Monetization Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `high`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`
- **Required Skills**: `revenuecat-hard-paywall`

#### Responsibilities:
- Integrate RevenueCat SDK and entitlement checking model.
- Enforce 3-free-walk quota strictly at local save boundary.
- Trigger hard paywall on 4th walk save attempt.
- Manage weekly ($2.99) and annual ($19.99) subscription offerings.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonMain/**/*`, `androidMain/**/*`, `iosMain/**/*`
- **Deny Scopes**: *None*

### Agent: `qa-automation-engineer` (QA Automation Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `high`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `40-rca`, `50-verification`
- **Required Skills**: `mobile-testing`

#### Responsibilities:
- Author kotlin.test unit and integration test suites.
- Develop Compose UI tests and Robolectric tests where appropriate.
- Target UI elements with semantic testTag and contentDescription selectors.
- Maintain regression test coverage for GPS, Room, and quota gates.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `commonTest/**/*`, `androidTest/**/*`, `iosTest/**/*`
- **Deny Scopes**: *None*

### Agent: `mobile-security-privacy-engineer` (Mobile Security & Privacy Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `10-planning`, `30-surgical-edit`, `50-verification`
- **Required Skills**: `mobile-privacy-local-first`

#### Responsibilities:
- Enforce local data privacy and zero-tracking baseline.
- Audit minimum required Android and iOS runtime permissions.
- Ensure GPS location and attached photos remain exclusively local.
- Verify secrets and API keys are not exposed in client code.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `docs/user-guide/privacy.md`, `docs/architecture/**/*`
- **Deny Scopes**: *None*

### Agent: `mobile-devops-engineer` (Mobile DevOps & Build Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `medium`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`
- **Required Skills**: `mobile-ci-cd`

#### Responsibilities:
- Maintain Gradle build scripts and libs.versions.toml version catalog.
- Configure Android and iOS CI workflows on JDK 17.
- Guarantee build reproducibility and compiler dependency isolation.
- Enforce Git branch hygiene and PR requirements.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `gradle/**/*`, `.github/workflows/**/*`, `build.gradle.kts`, `settings.gradle.kts`
- **Deny Scopes**: *None*

### Agent: `walkmark-documentation-engineer` (WalkMark Documentation Engineer)

- **Type**: `specialist`
- **Runtime**: `antigravity`
- **Criticality**: `medium`
- **Approval Authority**: `NO`
- **Delegates To**: *None*
- **Required Protocols**: `20-execution`, `30-surgical-edit`, `50-verification`
- **Required Skills**: `documentation-and-user-manual`

#### Responsibilities:
- Maintain technical documentation continuously under docs/architecture and docs/developer.
- Maintain user manual continuously under docs/user-guide with every user-visible feature.
- Maintain setup, build, and testing developer guides.
- Document permission and privacy behavior in user and architecture docs.
- Maintain release notes and changelog continuously under docs/release-notes.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `docs/**/*`, `README.md`
- **Deny Scopes**: *None*

### Agent: `qa-reviewer` (QA Reviewer & Quality Gate)

- **Type**: `reviewer`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `YES`
- **Delegates To**: *None*
- **Required Protocols**: `50-verification`, `60-skill-lifecycle`
- **Required Skills**: `mobile-testing`, `documentation-and-user-manual`

#### Responsibilities:
- Perform independent final review of implementations and tests.
- Verify all acceptance criteria and Definition of Done requirements.
- Validate technical documentation and user manual updates.
- Detect and reject scope drift and unauthorized architectural changes.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `reports/**/*`
- **Deny Scopes**: *None*

## Workflows

### Workflow: `two-pass-feature-development`

Mandatory 2-pass workflow. Pass 1 is investigation and planning only with a hard human approval gate. Pass 2 is verified implementation, testing, and QA review.

| Stage | Agents | Mode | Human Gate | Review Gate | RCA Path |
|---|---|---|---|---|---|
| **pass-1-investigation-and-planning** | `walkmark-po-orchestrator`, `kmp-architecture-engineer`, `map-location-engineer`, `compose-ui-ux-engineer`, `local-data-engineer`, `revenuecat-monetization-engineer`, `mobile-security-privacy-engineer`, `mobile-devops-engineer`, `walkmark-documentation-engineer` | `sequential` | `Required` | `No` | `N/A` |
| **pass-2-implementation-and-verification** | `kmp-architecture-engineer`, `map-location-engineer`, `compose-ui-ux-engineer`, `local-data-engineer`, `revenuecat-monetization-engineer`, `qa-automation-engineer`, `walkmark-documentation-engineer`, `qa-reviewer` | `sequential` | `No` | `Required` | `N/A` |

## Runtime & Container Enforcement Policies

- **Branch Naming Pattern**: `agents/{runtime}/{us}`
- **Direct Base Push**: `PROHIBITED (Enforced)`
- **Independent Clones**: `Enforced`
- **Docker Socket Inside Containers**: `PROHIBITED (Security enforced)`
- **Run as Non-Root**: `YES`

