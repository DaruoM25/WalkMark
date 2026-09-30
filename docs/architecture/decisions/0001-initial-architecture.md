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

---

## Addendum: US-006 Phase 1 monetization gate (Accepted)

### Context
The 3-free-walk commercial rule had to be enforced and verifiable **before** any payment SDK
enters the app. Adding RevenueCat first would have made the commercial rule unverifiable and
would have put a payment SDK on the classpath of a privacy-first, local-first application.

### Decision
1. **The free-walk quota is derived, never stored.**
   The count is the number of persisted, non-deleted `Walk` rows, read through a narrow
   `PersistedWalkCount` port. There is **no quota table, no counter column, and no migration**.
   `SCHEMA_CHANGE_REQUIRED = NO`.

2. **`StartWalkUseCase` is the authoritative *may-tracking-start* gate.**
   It combines subscription state with the persisted walk count and evaluates a pure policy:
   - entitled -> Allowed
   - count < `FREE_WALK_LIMIT` (3) -> Allowed
   - otherwise -> `RequiresSubscription(FreeWalkQuotaReached)`

   It owns **only** the decision to start GPS tracking. On `Allowed` it calls
   `LocationRepository.startTracking()`.

3. **Walk row creation remains owned by `WalkSessionRecorder`.**
   Persistence is deliberately *not* moved into the gate. `WalkSessionRecorder` still creates
   the `Walk` row on the first `LocationTrackingState.Tracking` emission. Consequence, accepted
   deliberately: if location permission is denied, no row is written and the attempt costs no
   quota unit.

4. **`startWalk()` re-reads the count at call time.**
   It never trusts the retained `access`/`freeWalkCount` `StateFlow` snapshot. A stale retained
   value must never be able to grant a walk. Enforced by
   `StartWalkUseCaseTest.staleRetainedStateCannotGrantAWalk`.

5. **`StartWalkUseCase` depends on `PersistedWalkCount`, not on `WalkRepository`.**
   `WalkRepository`, `RoomWalkRepository` and the Room schema are untouched, so US-005 History /
   Detail work (including `observeWalkById`) can land with zero conflict. The production
   adapter `WalkRepositoryWalkCount` only *delegates* to the existing
   `WalkRepository.observeAllWalks()`. No second `WalkRepository` implementation exists.

6. **A hard paywall gates the start of a walk, it does not trap the user.**
   `HardPaywallSheet` is dismissible via a visible Close control and system Back. Dismissal
   sets only presentation visibility. It must never mutate quota, entitlement, walk count, or
   persistent state, and it must never create an `alreadyOffered` / `paywallDismissed` latch.
   The next Start Walk tap re-runs the gate and re-shows the paywall.

7. **The Phase 1 provider is a production-safe NOT_CONFIGURED fallback, not a fake.**
   `UnavailableSubscriptionManager` lives in `commonMain` and returns
   `PurchaseResult.Unavailable` / `RestoreResult.Unavailable`, and never reports
   `OfferingsState.Loaded`. It exposes no entitlement mutator. A mutable
   `FakeSubscriptionManager` exists in `commonTest` only.
   `MonetizationProviderStatus.MONETIZATION_PROVIDER = "NOT_CONFIGURED"`.

8. **No fabricated runtime prices.**
   When offerings are unavailable, the runtime paywall renders no price at all. Only
   provider-supplied `PaywallProduct.priceFormatted` may ever be displayed. Planned pricing
   ($2.99/week, $19.99/year) is documentation-only. Enforced by
   `MonetizationDomainArchitectureTest.presentationLayerHasNoFabricatedStorePrices`.

9. **Presentation-layer in-flight guard, not a reservation.**
   `LocationViewModel.startTracking()` sets `isStartingWalk = true` *synchronously before*
   launching, and resets it in `finally`. This closes the double-tap window so N taps cause
   exactly one gate invocation. It is **not** a quota reservation: no persistence, no schema,
   no counters. Enforced by `concurrentStartTapsTriggerExactlyOneGateInvocation`.

### Consequences
- Zero dependency change, zero toolchain change, zero schema change, zero Room change.
- The commercial rule is fully testable with no payment SDK on the classpath.
- Adding a real provider later is additive: implement `SubscriptionManager`, inject it through
  the existing `App(subscriptionManager = ...)` parameter, and the gate and paywall need no
  architectural change.
