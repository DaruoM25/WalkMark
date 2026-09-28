# BLACK BOX ENFORCEMENT & ISOLATION POLICY: WalkMark

1. **Strict Read-Only Targets**:
- No specific read-only paths configured.

2. **Git Isolation**:
- Feature branch pattern: `agents/{runtime}/{us}`
- Direct push to `main`: PROHIBITED.

3. **Container Sandboxing & Host Isolation**:
- Docker socket mounting: PROHIBITED.
- Run as non-root: YES.
- Host Windows workspace modification: FORBIDDEN. All source edits must happen inside isolated container worktree.
- Host environment is permitted only for: Docker engine control, ADB daemon, emulator execution, and artifact transfer.
