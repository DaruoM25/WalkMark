# WalkMark - Agent Governance & System Roster

> **Project Type**: mobile-kmp  
> **Base Branch**: `main`  
> **Repository**: `TODO / not_configured`

## Architecture Overview

WalkMark - Private local-first GPS walk journaling mobile application with photos, notes, and offline maps built on Kotlin Multiplatform, Compose Multiplatform, and Room KMP.

## Agent Roster

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
| `verification-gatekeeper` | **Verification Gatekeeper** | `reviewer` | `antigravity` | `critical` | `Yes` |
| `qa-reviewer` | **QA Reviewer & Quality Gate** | `reviewer` | `antigravity` | `critical` | `Yes` |

## Detailed Responsibilities & Scopes

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

### Agent: `verification-gatekeeper` (Verification Gatekeeper)

- **Type**: `reviewer`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `YES`
- **Delegates To**: *None*
- **Required Protocols**: `execution-lifecycle`, `evidence-policy`, `android-runtime-validation`, `visual-acceptance`, `validation-freeze`, `50-verification`
- **Required Skills**: `mobile-testing`

#### Responsibilities:
- Validate and verify execution evidence independently without writing or modifying production code.
- Audit artifacts/<US>/evidence.yaml manifest for exact command execution, outputs, and current commit hash matches.
- Enforce status model integrity (CONFIGURED != EXECUTED != PASS, IMPLEMENTED != VERIFIED).
- Verify Android runtime facts (ADB daemon, lease, serial, AVD identity, visible vs headless state, foreground app).
- Enforce Visual Acceptance checklist on visible emulator (separate SCREENSHOT_CAPTURE from VISUAL_ACCEPTANCE).
- Protect FROZEN_PASS validation gates from unnecessary re-execution.
- Deliver strict ACCEPT or REJECT verdicts with detailed reasons.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `artifacts/**/evidence.yaml`, `reports/verification/**/*`
- **Deny Scopes**: `src/**/*`, `app/**/*`, `core/**/*`, `feature/**/*`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/**/*`

### Agent: `qa-reviewer` (QA Reviewer & Quality Gate)

- **Type**: `reviewer`
- **Runtime**: `antigravity`
- **Criticality**: `critical`
- **Approval Authority**: `YES`
- **Delegates To**: *None*
- **Required Protocols**: `execution-lifecycle`, `evidence-policy`, `testing-levels`, `visual-acceptance`, `validation-freeze`, `50-verification`, `60-skill-lifecycle`
- **Required Skills**: `mobile-testing`, `documentation-and-user-manual`

#### Responsibilities:
- Perform independent final audit of implementations, tests, and evidence manifests.
- Verify all acceptance criteria and Definition of Done requirements against actual execution evidence.
- Reject any PASS claim not supported by concrete CLI commands, raw outputs, zero-failure assertions, and commit hash.
- Reject CONFIGURED or IMPLEMENTED reported as PASS.
- Reject previous-run evidence presented against modified code.
- Validate technical documentation and user manual updates.
- Detect and reject scope drift and unauthorized architectural changes.

#### Scope Boundaries:
- **Read Scopes**: `**/*`
- **Write Scopes**: `reports/**/*`
- **Deny Scopes**: `src/**/*`, `app/**/*`, `core/**/*`, `feature/**/*`

## Workflows

### Workflow: `two-pass-feature-development`

Mandatory 2-pass workflow. Pass 1 is investigation and planning only with a hard human approval gate. Pass 2 is verified implementation, testing, evidence collection, verification gatekeeping, and QA review.

| Stage | Agents | Mode | Human Gate | Review Gate | RCA Path |
|---|---|---|---|---|---|
| **pass-1-investigation-and-planning** | `walkmark-po-orchestrator`, `kmp-architecture-engineer`, `map-location-engineer`, `compose-ui-ux-engineer`, `local-data-engineer`, `revenuecat-monetization-engineer`, `mobile-security-privacy-engineer`, `mobile-devops-engineer`, `walkmark-documentation-engineer` | `sequential` | `Required` | `No` | `N/A` |
| **pass-2-implementation-and-verification** | `kmp-architecture-engineer`, `map-location-engineer`, `compose-ui-ux-engineer`, `local-data-engineer`, `revenuecat-monetization-engineer`, `qa-automation-engineer`, `walkmark-documentation-engineer`, `verification-gatekeeper`, `qa-reviewer` | `sequential` | `No` | `Required` | `N/A` |

## Runtime & Container Enforcement Policies

- **Branch Naming Pattern**: `agents/{runtime}/{us}`
- **Direct Base Push**: `PROHIBITED (Enforced)`
- **Independent Clones**: `Enforced`
- **Docker Socket Inside Containers**: `PROHIBITED (Security enforced)`
- **Run as Non-Root**: `YES`

## Non-Negotiable Governance & Execution Directives

1. **Execution Lifecycle**: `OBSERVE` -> `ANALYZE` -> `PLAN` (Pass 1) -> `HUMAN APPROVAL` -> `PRE-FLIGHT` -> `IMPLEMENT` -> `VERIFY` -> `COLLECT EVIDENCE` -> `REVIEW` -> `PR READY`.
2. **No Mutating Action Before Human Approval**: Pass 1 investigation must stop for explicit human approval before any production or configuration modification.
3. **Current-Evidence-First & State Override**: Before executing a gate, check current evidence. If valid, skip re-execution (`FROZEN_PASS`). Current proven state strictly overrides historical memory or old summaries.
4. **Evidence Over Assertion**: Every `PASS` verdict requires exact CLI command, output, zero-failure assertion, and current commit hash (`CLAIM` -> `COMMAND` -> `OUTPUT` -> `ASSERTION`). Unsupported claims default to `UNVERIFIED`.
5. **Strict Status Taxonomy**: Allowed statuses are `NOT_STARTED`, `PLANNED`, `IMPLEMENTED`, `EXECUTING`, `PASS`, `FAIL`, `ENVIRONMENT_BLOCKED`, `ENVIRONMENT_INCOMPATIBLE`, `NOT_APPLICABLE`, `UNVERIFIED`, `FROZEN_PASS`. `CONFIGURED != EXECUTED != PASS`; `IMPLEMENTED != VERIFIED`; `COMPILED != runtime verified`.
6. **Per-Gate Execution Budget**: `MAX_COMMANDS_PER_GATE = 12`, `MAX_DURATION_PER_GATE_SECONDS = 300`, `IDENTICAL_RETRY_MAX = 2`, `REMEDIATION_ATTEMPTS_MAX = 2`. On budget exhaustion, STOP gate immediately and request human decision.
7. **Automatic Gate Freeze**: Once complete evidence is recorded, transition immediately to `FROZEN_PASS`. A frozen gate reopens only if relevant source changes, environment changes, or regression evidence exists.
8. **Verify / Review Stage Read-Only Protection**: Mutations to application code/tests (`composeApp/**`, `iosApp/**`, `gradle/**`) are strictly forbidden during `VERIFY` and `REVIEW` stages. Source defects require `VERIFY = FAIL`, RCA, and a new implementation plan.
9. **Android Deterministic Recovery**: 1 state check, 1 visible AVD launch, max 120s boot wait, explicit app start, foreground verification. No monkey, random taps, keyevent loops, or ad-hoc runtime mutations.
10. **Host / Container Isolation**: Host environment is restricted to Docker control, ADB, emulator host, and artifact transport. Source modification must stay strictly within isolated container workspace.

