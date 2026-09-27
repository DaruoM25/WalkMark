# ADR 0001: Initial WalkMark Technology Stack & Architecture

## Status
Accepted

## Context
WalkMark requires a high-performance, private, local-first mobile walk journal on Android and iOS.

## Decision
- Kotlin Multiplatform & Compose Multiplatform for shared UI and business logic.
- MapLibre Compose + OpenStreetMap for private mapping without third-party telemetry.
- Room KMP for local-first database persistence.
- RevenueCat for 3-free-walk quota monetization (.99/week, .99/year).
