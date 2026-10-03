package com.walkmark.app.data.monetization

import android.app.Activity
import android.content.Context
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.monetization.Offering
import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.PaywallProduct
import com.walkmark.app.domain.monetization.PurchaseResult
import com.walkmark.app.domain.monetization.RestoreResult
import com.walkmark.app.domain.monetization.SubscriptionManager
import com.walkmark.app.domain.monetization.SubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RevenueCatSubscriptionManager(
    private val context: Context,
    private val apiKey: String? = null,
    private val authRepository: AuthRepository? = null,
    private val scope: CoroutineScope? = null
) : SubscriptionManager {

    private val _subscriptionState = MutableStateFlow(SubscriptionState.unknown(isLoading = false))
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _offerings = MutableStateFlow<OfferingsState>(OfferingsState.NotLoaded)
    override val offerings: StateFlow<OfferingsState> = _offerings.asStateFlow()

    private var isConfigured = false

    companion object {
        const val ENTITLEMENT_PREMIUM = "premium"
    }

    init {
        initPurchases()
        observeAuth()
    }

    private fun initPurchases() {
        if (!apiKey.isNullOrBlank()) {
            try {
                if (!Purchases.isConfigured) {
                    Purchases.configure(
                        PurchasesConfiguration.Builder(context, apiKey).build()
                    )
                }
                Purchases.sharedInstance.updatedCustomerInfoListener =
                    UpdatedCustomerInfoListener { customerInfo ->
                        _subscriptionState.value = mapCustomerInfo(customerInfo)
                    }
                isConfigured = true
            } catch (e: Throwable) {
                isConfigured = false
                _subscriptionState.value = SubscriptionState.free()
                _offerings.value = OfferingsState.Unavailable(e.message)
            }
        } else {
            _subscriptionState.value = SubscriptionState.free()
            _offerings.value = OfferingsState.Unavailable("Missing API Key")
        }
    }

    private fun observeAuth() {
        if (authRepository != null && scope != null) {
            scope.launch {
                authRepository.observeSession().collect { sessionState ->
                    when (sessionState) {
                        is AuthSessionState.Authenticated -> {
                            loginUser(sessionState.user.id)
                        }
                        is AuthSessionState.Guest -> {
                            logoutUser()
                        }
                        AuthSessionState.Restoring -> {
                            // Do nothing while restoring
                        }
                    }
                }
            }
        }
    }

    internal fun loginUser(userId: String) {
        if (!isConfigured || !Purchases.isConfigured) return
        try {
            Purchases.sharedInstance.logIn(
                userId,
                object : LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        _subscriptionState.value = mapCustomerInfo(customerInfo)
                    }

                    override fun onError(error: PurchasesError) {
                        // Keep current state on error
                    }
                }
            )
        } catch (e: Throwable) {
            // Ignore
        }
    }

    internal fun logoutUser() {
        if (!isConfigured || !Purchases.isConfigured) return
        try {
            if (!Purchases.sharedInstance.isAnonymous) {
                Purchases.sharedInstance.logOut(
                    object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            _subscriptionState.value = mapCustomerInfo(customerInfo)
                        }

                        override fun onError(error: PurchasesError) {
                            // Ignore
                        }
                    }
                )
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }

    internal fun mapCustomerInfo(customerInfo: CustomerInfo?): SubscriptionState {
        if (customerInfo == null) return SubscriptionState.free()
        val isEntitled = customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]?.isActive == true ||
            customerInfo.entitlements.active.containsKey(ENTITLEMENT_PREMIUM)
        return if (isEntitled) {
            SubscriptionState.entitled()
        } else {
            SubscriptionState.free()
        }
    }

    override suspend fun start() {
        refresh()
        fetchOfferings()
    }

    suspend fun fetchOfferings(): OfferingsState {
        if (!isConfigured || !Purchases.isConfigured) {
            val state = OfferingsState.Unavailable("RevenueCat not configured")
            _offerings.value = state
            return state
        }
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getOfferings(
                    object : ReceiveOfferingsCallback {
                        override fun onReceived(offerings: Offerings) {
                            val current = offerings.current
                            val state = if (current != null) {
                                val products = current.availablePackages.map { pkg ->
                                    PaywallProduct(
                                        id = pkg.product.id,
                                        title = pkg.product.title,
                                        period = pkg.packageType.name.lowercase().replaceFirstChar { it.uppercase() },
                                        priceFormatted = pkg.product.price.formatted,
                                        isRecommended = pkg.packageType.name == "ANNUAL"
                                    )
                                }
                                OfferingsState.Loaded(Offering(id = current.identifier, products = products))
                            } else {
                                OfferingsState.Unavailable("No current offering configured")
                            }
                            _offerings.value = state
                            if (continuation.isActive) continuation.resume(state)
                        }

                        override fun onError(error: PurchasesError) {
                            val state = OfferingsState.Unavailable(error.message)
                            _offerings.value = state
                            if (continuation.isActive) continuation.resume(state)
                        }
                    }
                )
            } catch (e: Throwable) {
                val state = OfferingsState.Unavailable(e.message)
                _offerings.value = state
                if (continuation.isActive) continuation.resume(state)
            }
        }
    }

    override suspend fun refresh(): SubscriptionState {
        if (!isConfigured || !Purchases.isConfigured) {
            _subscriptionState.value = SubscriptionState.free()
            return _subscriptionState.value
        }
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getCustomerInfo(
                    object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            val state = mapCustomerInfo(customerInfo)
                            _subscriptionState.value = state
                            if (continuation.isActive) continuation.resume(state)
                        }

                        override fun onError(error: PurchasesError) {
                            val state = SubscriptionState.free()
                            _subscriptionState.value = state
                            if (continuation.isActive) continuation.resume(state)
                        }
                    }
                )
            } catch (e: Throwable) {
                val state = SubscriptionState.free()
                _subscriptionState.value = state
                if (continuation.isActive) continuation.resume(state)
            }
        }
    }

    override suspend fun purchase(product: PaywallProduct): PurchaseResult {
        if (!isConfigured || !Purchases.isConfigured) {
            return PurchaseResult.Unavailable
        }
        val activity = context as? Activity ?: return PurchaseResult.Unavailable
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                    override fun onReceived(offerings: Offerings) {
                        val pkg = offerings.current?.availablePackages?.find { it.product.id == product.id }
                        if (pkg != null) {
                            Purchases.sharedInstance.purchase(
                                PurchaseParams.Builder(activity, pkg).build(),
                                object : PurchaseCallback {
                                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                                        val state = mapCustomerInfo(customerInfo)
                                        _subscriptionState.value = state
                                        if (continuation.isActive) continuation.resume(PurchaseResult.Completed(state))
                                    }

                                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                        if (userCancelled) {
                                            if (continuation.isActive) continuation.resume(PurchaseResult.Cancelled)
                                        } else {
                                            if (continuation.isActive) continuation.resume(PurchaseResult.Failed(error.message))
                                        }
                                    }
                                }
                            )
                        } else {
                            if (continuation.isActive) continuation.resume(PurchaseResult.Failed("Product not found in current offering"))
                        }
                    }

                    override fun onError(error: PurchasesError) {
                        if (continuation.isActive) continuation.resume(PurchaseResult.Failed(error.message))
                    }
                })
            } catch (e: Throwable) {
                if (continuation.isActive) continuation.resume(PurchaseResult.Failed(e.message ?: "Purchase error"))
            }
        }
    }

    override suspend fun restorePurchases(): RestoreResult {
        if (!isConfigured || !Purchases.isConfigured) {
            return RestoreResult.Unavailable
        }
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.restorePurchases(
                    object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            val state = mapCustomerInfo(customerInfo)
                            _subscriptionState.value = state
                            if (continuation.isActive) {
                                if (state.isEntitled) {
                                    continuation.resume(RestoreResult.Restored(state))
                                } else {
                                    continuation.resume(RestoreResult.NotRestored)
                                }
                            }
                        }

                        override fun onError(error: PurchasesError) {
                            if (continuation.isActive) continuation.resume(RestoreResult.Failed(error.message))
                        }
                    }
                )
            } catch (e: Throwable) {
                if (continuation.isActive) continuation.resume(RestoreResult.Failed(e.message ?: "Restore error"))
            }
        }
    }
}
