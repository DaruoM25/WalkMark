# Git Worktree & Host Isolation Protocol

## Isolation Boundaries
1. **Host Boundary**: Host Windows is strictly read-only for repository sources. Host may only execute Docker commands, host ADB server, run GUI emulator, and transport artifacts.
2. **Container Workspace**: All code writing, Gradle builds, and test executions must occur within the container runtime environment.
3. **Branch Enforcement**: All work occurs on `agents/{runtime}/{us}`. Direct push to `main` is strictly forbidden.
