# WalkMark - Project map

## AUTHORITATIVE_SOURCES

- Governance: `AGENTS.md`
- System registry: `.agent/system.yaml`
- Agent registry: `.agent/registries/agents.yaml`
- Skills: `skills/index.yaml` and each indexed source file
- Product and regulatory references: `docs/architecture`

## CURRENT_RELEASE

Release 1 develops `WalkMark`. `../Ledger-hub-mobile` is the strictly read-only target application.

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
- **Independent Clone**: `True`
- **Base Branch Push**: `PROHIBITED`
- **Docker Socket Ingestion**: `DENIED`
- **Read-Only Volume Targets**: None

