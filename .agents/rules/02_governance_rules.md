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
- Current-evidence-first: Check current commit and evidence manifest before executing a gate. If valid, skip re-execution (`FROZEN_PASS`).
- Current state overrides historical summary: A retrospective or old execution log may never downgrade a proven gate without explicit invalidation evidence.
- `CONFIGURED != EXECUTED != PASS`.
- `IMPLEMENTED != VERIFIED`.
- `COMPILED != runtime verified`.
- Unknown or incomplete evidence must be reported as `UNVERIFIED`.
- Test preservation: Existing tests must NEVER be deleted, disabled, skipped, or weakened to turn a build green.
- Verify / Review Read-Only Protection: Mutations to `composeApp/**`, `iosApp/**`, `gradle/**`, build configuration, and application tests are strictly FORBIDDEN during `VERIFY` and `REVIEW` stages.
- No Ad-Hoc Runtime Mutation: Validation must NEVER repair runtime prerequisites ad hoc (e.g. symlinks, PATH edits, package installations). Missing prerequisites are classified as `RUNTIME_IMAGE_FIX` or `ENVIRONMENT_BLOCKED`.
