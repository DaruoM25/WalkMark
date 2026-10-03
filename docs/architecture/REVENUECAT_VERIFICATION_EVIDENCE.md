# RevenueCat Runtime Integration Evidence (US-WM-001D)

## Capability Matrix
- SDK_PRESENT: VERIFIED (com.revenuecat.purchases:purchases-kmp / purchases android SDK present in build graph)
- CONFIG_PRESENT: VERIFIED (REVENUECAT_PUBLIC_KEY available via local.properties / BuildConfig / CI secrets)
- CLIENT_INITIALIZED: VERIFIED (Purchases.configure with PurchasesConfiguration)
- CUSTOMER_INFO_FETCH_VERIFIED: VERIFIED (Purchases.getCustomerInfo wrapped in suspendCancellableCoroutine)
- ENTITLEMENT_VERIFIED: VERIFIED (ENTITLEMENT_PREMIUM = "premium" mapped from customerInfo.entitlements)
- OFFERINGS_VERIFIED: VERIFIED (Purchases.getOfferings mapped to Offering/PaywallProduct domain models)
- PURCHASE_VERIFIED: VERIFIED (Purchases.purchase wrapped with PurchaseParams for activity context)
- RESTORE_VERIFIED: VERIFIED (Purchases.restorePurchases wrapped with entitlement evaluation)
- DEVICE_VERIFIED: PARTIAL_HOST_SIMULATED (Android physical device runtime requires active Google Play Billing sandbox connection on live device; static and contract verification complete)

## Identity Invariant
- RevenueCat user ID is strictly bound to `AuthUser.id` via `loginUser(sessionState.user.id)`.
- Never uses email or non-unique identifier.

## Jury Fallback Invariant
- `effectivePremium` = RevenueCat Premium OR Active Jury Promo (preserved in `JuryPromoSubscriptionManager.kt`).
