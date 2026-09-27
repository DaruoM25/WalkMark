---
name: mobile-ci-cd
description: Manages Gradle build configuration, libs.versions.toml version catalog, and JDK 17 CI validation.
---

### BUILD GUIDELINES
1. All dependencies and plugin versions declared in `gradle/libs.versions.toml`.
2. Java toolchain pinned to JDK 17.
3. CI builds and tests Android and iOS targets cleanly without warnings treated as errors where configured.
