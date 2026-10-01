package com.walkmark.app.data.monetization

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.walkmark.app.domain.auth.AuthOperationResult
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.auth.AuthSessionState
import com.walkmark.app.domain.auth.AuthUser
import com.walkmark.app.domain.monetization.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
class RevenueCatSubscriptionManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun unconfiguredManagerDefaultsToFreeState() = runTest {
        val manager = RevenueCatSubscriptionManager(context, apiKey = null)

        assertEquals(SubscriptionStatus.Free, manager.subscriptionState.value.status)
        assertFalse(manager.subscriptionState.value.isEntitled)
    }

    @Test
    fun nullCustomerInfoMapsToFreeState() {
        val manager = RevenueCatSubscriptionManager(context, apiKey = null)
        val state = manager.mapCustomerInfo(null)

        assertEquals(SubscriptionStatus.Free, state.status)
        assertFalse(state.isEntitled)
    }

    @Test
    fun authObservationBridgeConnectsAuthUserId() = runTest {
        val sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Guest)
        val fakeAuthRepo = object : AuthRepository {
            override fun observeSession(): StateFlow<AuthSessionState> = sessionState.asStateFlow()
            override suspend fun signUp(email: String, password: String) = AuthOperationResult.Failure(com.walkmark.app.domain.auth.AuthFailure.Unknown)
            override suspend fun signIn(email: String, password: String) = AuthOperationResult.Failure(com.walkmark.app.domain.auth.AuthFailure.Unknown)
            override suspend fun signOut() = AuthOperationResult.Success(Unit)
            override suspend fun restoreSession() = AuthOperationResult.Success(AuthSessionState.Guest)
        }

        val manager = RevenueCatSubscriptionManager(
            context = context,
            apiKey = null,
            authRepository = fakeAuthRepo,
            scope = backgroundScope
        )

        // Verifies no crashes and safe handling
        sessionState.value = AuthSessionState.Authenticated(AuthUser("user-opaque-id", "test@example.com"))
        sessionState.value = AuthSessionState.Guest

        assertEquals(SubscriptionStatus.Free, manager.subscriptionState.value.status)
    }
}
