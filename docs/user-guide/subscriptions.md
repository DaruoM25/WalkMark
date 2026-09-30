# Subscriptions & 3-Free-Walk Quota

## Current build status - Phase 1 (no purchase provider)

WalkMark now enforces the 3-free-walk quota, but **this build cannot sell you anything**.

- The quota and the paywall screen are live and fully enforced.
- **Purchases are not available in this build.**
- **Provider not configured** - no store, no payment sheet, no network call.
- No RevenueCat SDK is included in the app.

The paywall never displays a store price in this build, because no real price is ever
supplied by a provider. Instead it tells you, in plain terms:

1. `Subscription required` - what the paywall is for.
2. An explanation that the free walks are used up and that **purchases are not available in
   this build**.
3. `Provider not configured` - the honest technical reason.
4. Subscribe and Restore controls, both **disabled**, plus a working `Close` control.

## The free-walk quota

- **Free Tier**: the first 3 saved walks are free.
- When you press **Start Walk** for a 4th time, the app shows the paywall instead of
  starting a recording.
- Past saved walks stay fully accessible with or without a subscription.

## How the quota is counted

- The count is simply the number of saved walks already stored on your device.
- There is **no quota table, no counter column, and no background upload**. Nothing about
  your walking activity leaves the phone.
- The count is re-read every single time you press Start Walk, so a stale screen can never
  grant a walk.

## The paywall can be closed — and that grants you nothing

You can dismiss the paywall with **Close** or with the system **Back** button and return to
the normal app. This is deliberate: you are never trapped.

Dismissing the paywall does **not** unlock anything:

- it does not change the quota,
- it does not change your subscription state,
- it does not consume a free walk,
- it does not remember that the paywall was already shown.

Pressing **Start Walk** again simply re-runs the same check, and the paywall reappears while
the quota is exhausted. There is no free bypass and no automatic entitlement.

## Planned pricing (not on sale yet)

The intended product pricing is:

- Weekly Subscription: $2.99 USD
- Annual Subscription: $19.99 USD

These figures are recorded here as **planned** product pricing only. They are not store
prices, they are not displayed in the app, and they are not purchasable. They will appear at
runtime only if and when a real purchase provider supplies genuine formatted prices.

## Why purchases are unavailable

WalkMark's privacy model is local-first and zero-tracking. Phase 1 deliberately ships the
monetization decision logic with **no purchase provider** so that the commercial rule can be
verified before any payment SDK is added. Adding a provider is a separate, explicitly
authorized step.

Until then the app cannot fake a purchase, a restore, or a price.
