package com.walkmark.app.data.monetization

import android.content.Context
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.logOutWith
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCancellableCoroutine

class RevenueCatSubscriptionManager(
    private val context: Context,
    private val apiKey: String? = null,
    private val authRepository: AuthRepository? = null,
    private val scope: CoroutineScope? = null
) : SubscriptionManager {

    private val _subscriptionState = MutableStateFlow(SubscriptionState.unknown(isLoading = false))
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _offerings = MutableStateFlow<OfferingsState>(OfferingsState.Empty)
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
            }
        } else {
            _subscriptionState.value = SubscriptionState.free()
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
            Purchases.sharedInstance.logInWith(
                userId = userId,
                onError = { /* Keep current state */ },
                onSuccess = { customerInfo, _ ->
                    _subscriptionState.value = mapCustomerInfo(customerInfo)
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
                Purchases.sharedInstance.logOutWith(
                    onError = { /* Ignore */ },
                    onSuccess = { customerInfo ->
                        _subscriptionState.value = mapCustomerInfo(customerInfo)
                    }
                )
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }

    internal fun mapCustomerInfo(customerInfo: CustomerInfo?): SubscriptionState {
        if (customerInfo == null) return SubscriptionState.free()
        val isEntitled = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true
        return if (isEntitled) {
            SubscriptionState.entitled()
        } else {
            SubscriptionState.free()
        }
    }

    override suspend fun start() {
        refresh()
    }

    override suspend fun refresh(): SubscriptionState {
        if (!isConfigured || !Purchases.isConfigured) {
            _subscriptionState.value = SubscriptionState.free()
            return _subscriptionState.value
        }
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.getCustomerInfoWith(
                    onError = {
                        val state = SubscriptionState.free()
                        _subscriptionState.value = state
                        if (continuation.isActive) continuation.resume(state)
                    },
                    onSuccess = { customerInfo ->
                        val state = mapCustomerInfo(customerInfo)
                        _subscriptionState.value = state
                        if (continuation.isActive) continuation.resume(state)
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
        return PurchaseResult.Unavailable
    }

    override suspend fun restorePurchases(): RestoreResult {
        return RestoreResult.Unavailable
    }
}
