# BLACK BOX ENFORCEMENT & ISOLATION POLICY: WalkMark

1. **Strict Read-Only Targets**:
- No specific read-only paths configured.

2. **Git Isolation**:
- Feature branch pattern: `agents/{runtime}/{us}`
- Direct push to `main`: PROHIBITED.

3. **Container Sandboxing**:
- Docker socket mounting: PROHIBITED.
- Run as non-root: YES.
