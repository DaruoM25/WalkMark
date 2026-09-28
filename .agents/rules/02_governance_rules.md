# GOVERNANCE & EXECUTION DIRECTIVES: WalkMark

1. **Source of Truth**:
- Canonical agents roster in `AGENTS.md`.
- Canonical protocols in `.agent/protocols/`.
- Active skills in `skills/index.yaml`.
- Evidence manifests in `artifacts/<US>/evidence.yaml`.

2. **Mandatory Execution Lifecycle**:
`OBSERVE` -> `ANALYZE` -> `PLAN` (Pass 1) -> `HUMAN APPROVAL` -> `PRE-FLIGHT` -> `IMPLEMENT` -> `VERIFY` -> `COLLECT EVIDENCE` -> `REVIEW` -> `PR READY`.

3. **Non-Negotiable Verification & Evidence Rules**:
- Pass 1 is read-only analysis and plan only. NO code mutation before human approval.
- Pre-flight verification (branch, clean worktree, container, independent git, adb lease, ci) is required before implementation.
- Every `PASS` requires: `CLAIM` -> `COMMAND` -> `OUTPUT` -> `ASSERTION` recorded in `artifacts/<US>/evidence.yaml`.
- `CONFIGURED != EXECUTED != PASS`.
- `IMPLEMENTED != VERIFIED`.
- `COMPILED != runtime verified`.
- Unknown or incomplete evidence must be reported as `UNVERIFIED`.
- Test preservation: Existing tests must NEVER be deleted, disabled, skipped, or weakened to turn a build green.
