# ADR 0001: Initial WalkMark Technology Stack & Architecture

## Status
Accepted - [VERIFIED]

## Context
WalkMark requires a high-performance, private, local-first mobile walk journal on Android and iOS.

## Decision
- Kotlin Multiplatform & Compose Multiplatform for shared UI and business logic - [VERIFIED]
- Canonical `:composeApp` module with domain/data/ui/location layers - [VERIFIED]
- MapLibre Compose + OpenStreetMap for private mapping without third-party telemetry - [PLANNED]
- Room KMP + Bundled SQLite for local-first database persistence - [VERIFIED]
- RevenueCat for 3-free-walk quota monetization ($2.99/week, $19.99/year) - [PLANNED]

