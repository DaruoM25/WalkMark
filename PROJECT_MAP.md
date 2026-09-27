# WalkMark - Project map

## AUTHORITATIVE_SOURCES

- Governance: `AGENTS.md`
- System registry: `.agent/system.yaml`
- Agent registry: `.agent/registries/agents.yaml`
- Skills: `skills/index.yaml` and each indexed source file
- Product and architecture references: `PROJECT_CONTEXT-v2.md`, `docs/architecture`

## CURRENT_RELEASE

Release 1 develops `WalkMark` on canonical module `:composeApp`.

### CANONICAL_PACKAGES (in `composeApp/src/commonMain/kotlin/com/walkmark/app/`)
- `ui/`: Compose Multiplatform screens, adaptive layouts, and Material 3 theme.
- `domain/`: Business entities (`Walk`, `Waypoint`, `NotePhoto`) and logic.
- `data/`: Room KMP database (`WalkMarkDatabase`), entities, DAOs, and DTO mappers.
- `location/`: GPS tracking contracts (`LocationTracker`, `LocationPoint`).

## AGENT_SUBSYSTEMS

- **walkmark-po-orchestrator** (WalkMark Product Owner & Orchestrator): `orchestrator` on `antigravity`.
- **kmp-architecture-engineer** (KMP Architecture Engineer): `specialist` on `antigravity`.
- **map-location-engineer** (Map & Location Engineer): `specialist` on `antigravity`.
- **compose-ui-ux-engineer** (Compose UI/UX Engineer): `specialist` on `antigravity`.
- **local-data-engineer** (Local Data & Persistence Engineer): `specialist` on `antigravity`.
- **revenuecat-monetization-engineer** (RevenueCat Monetization Engineer): `specialist` on `antigravity`.
- **qa-automation-engineer** (QA Automation Engineer): `specialist` on `antigravity`.
- **mobile-security-privacy-engineer** (Mobile Security & Privacy Engineer): `specialist` on `antigravity`.
- **mobile-devops-engineer** (Mobile DevOps & Build Engineer): `specialist` on `antigravity`.
- **walkmark-documentation-engineer** (WalkMark Documentation Engineer): `specialist` on `antigravity`.
- **qa-reviewer** (QA Reviewer & Quality Gate): `reviewer` on `antigravity`.

## WORKFLOWS

### two-pass-feature-development
- Stage **pass-1-investigation-and-planning** -> Agents: walkmark-po-orchestrator, kmp-architecture-engineer, map-location-engineer, compose-ui-ux-engineer, local-data-engineer, revenuecat-monetization-engineer, mobile-security-privacy-engineer, mobile-devops-engineer, walkmark-documentation-engineer
- Stage **pass-2-implementation-and-verification** -> Agents: kmp-architecture-engineer, map-location-engineer, compose-ui-ux-engineer, local-data-engineer, revenuecat-monetization-engineer, qa-automation-engineer, walkmark-documentation-engineer, qa-reviewer

## RUNTIME_ISOLATION_POLICIES
- **Canonical Module**: `:composeApp` (shared module is strictly prohibited)
- **Independent Clone**: `True`
- **Base Branch Push**: `PROHIBITED`
- **Docker Socket Ingestion**: `DENIED`
- **Read-Only Volume Targets**: None


