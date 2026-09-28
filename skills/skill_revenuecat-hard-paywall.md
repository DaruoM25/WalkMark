---
name: revenuecat-hard-paywall
description: Enforces the 3-free-walk quota and triggers the RevenueCat subscription paywall ($2.99/week, $19.99/year).
---

### MONETIZATION & PAYWALL RULES
1. **Quota**: Users can save up to 3 walks for free without active subscription.
2. **Hard Paywall**: Attempting to save the 4th walk checks RevenueCat entitlement:
   - Active subscriber (`pro` entitlement): Walk is saved successfully.
   - Non-subscriber: Hard paywall modal is presented; walk save is held until purchase or user dismisses.
3. **Subscription Options**:
   - Weekly: $2.99 USD
   - Annual: $19.99 USD
4. **Offline Resilience**: Cache entitlement state securely locally; allow viewing past saved walks always.
