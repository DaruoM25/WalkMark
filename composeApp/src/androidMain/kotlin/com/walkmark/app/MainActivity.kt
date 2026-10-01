package com.walkmark.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.walkmark.app.data.auth.UnavailableAuthRepository
import com.walkmark.app.data.auth.supabase.SupabaseAuthConfig
import com.walkmark.app.data.auth.supabase.SupabaseAuthRepository
import com.walkmark.app.data.monetization.RevenueCatSubscriptionManager
import com.walkmark.app.data.monetization.UnavailableSubscriptionManager
import com.walkmark.app.domain.auth.AuthRepository
import com.walkmark.app.domain.monetization.SubscriptionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabaseConfig = SupabaseAuthConfig(
            projectUrl = BuildConfig.SUPABASE_URL,
            publishableKey = BuildConfig.SUPABASE_ANON_KEY
        )
        val authRepository: AuthRepository = if (supabaseConfig.isValid) {
            SupabaseAuthRepository(
                config = supabaseConfig,
                observationScope = lifecycleScope
            )
        } else {
            UnavailableAuthRepository()
        }

        val subscriptionManager: SubscriptionManager = if (BuildConfig.REVENUECAT_PUBLIC_KEY.isNotBlank()) {
            RevenueCatSubscriptionManager(
                context = applicationContext,
                apiKey = BuildConfig.REVENUECAT_PUBLIC_KEY,
                authRepository = authRepository,
                scope = lifecycleScope
            )
        } else {
            UnavailableSubscriptionManager()
        }

        setContent {
            App(
                authRepository = authRepository,
                subscriptionManager = subscriptionManager
            )
        }
    }
}
