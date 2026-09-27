# ADR 0001: Initial WalkMark Technology Stack & Architecture

## Status
Accepted - [IMPLEMENTED]

## Context
WalkMark requires a high-performance, private, local-first mobile wank journal on Android and iOS.

## Decision
- Kotlin Multiplatform & Compose Multiplatform for shared UI and business logic  - [IMPLEMENTED]
- MapLibre Compose + OpenStreetMap for private mapping without third-party telemetry  - [PLANNED]
- Room KMP for local-first database persistence  - [IMPLEMENTED]
- RevenueCat for 3-free-walk quota monetization ($2.99/week, $19.99/year)  - [PLANNED]
