package com.walkmark.app.presentation.paywall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.walkmark.app.domain.monetization.OfferingsState
import com.walkmark.app.domain.monetization.Offering
import com.walkmark.app.domain.monetization.PaywallProduct
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HardPaywallSheetComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val unavailableState = HardPaywallUiState(
        offerings = OfferingsState.Unavailable("Provider not configured"),
        freeWalkCount = 3,
        freeWalkLimit = 3
    )

    @Test
    fun sheetShowsHonestSubscriptionRequiredCopy() {
        composeTestRule.setContent {
            HardPaywallSheet(state = unavailableState, onDismiss = { })
        }

        composeTestRule.onNodeWithTag("paywall_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_title").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Purchases are not available in this build")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_provider_status").assertIsDisplayed()
    }

    @Test
    fun unavailableOfferingsRenderNoPriceNodes() {
        composeTestRule.setContent {
            HardPaywallSheet(state = unavailableState, onDismiss = { })
        }

        composeTestRule.onAllNodesWithTag("paywall_prices").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("paywall_price_weekly").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("paywall_price_annual").assertCountEquals(0)
    }

    @Test
    fun closeAffordanceIsVisibleAndDismisses() {
        var dismissed = false
        composeTestRule.setContent {
            HardPaywallSheet(state = unavailableState, onDismiss = { dismissed = true })
        }

        composeTestRule.onNodeWithTag("paywall_close_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_close_button").performClick()

        assertEquals(true, dismissed)
    }

    @Test
    fun purchaseControlIsNeverEnabledInPhase1() {
        composeTestRule.setContent {
            HardPaywallSheet(state = unavailableState, onDismiss = { })
        }

        composeTestRule.onNodeWithTag("paywall_purchase_button").assertIsNotEnabled()
    }

    @Test
    fun restoreControlIsNotEnabledInPhase1() {
        composeTestRule.setContent {
            HardPaywallSheet(state = unavailableState, onDismiss = { })
        }

        composeTestRule.onNodeWithTag("paywall_restore_button").assertIsNotEnabled()
    }

    @Test
    fun loadedOfferingsRenderProviderSuppliedPrices() {
        val loaded = HardPaywallUiState(
            offerings = OfferingsState.Loaded(
                Offering(
                    id = "default",
                    products = listOf(
                        PaywallProduct("weekly", "Weekly", "week", "2.99"),
                        PaywallProduct("annual", "Annual", "year", "19.99", isRecommended = true)
                    )
                )
            ),
            freeWalkCount = 3,
            freeWalkLimit = 3
        )

        composeTestRule.setContent {
            HardPaywallSheet(state = loaded, onDismiss = { })
        }

        composeTestRule.onNodeWithTag("paywall_price_weekly").assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_price_value_annual").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountEquals(expected: Int) {
        fetchSemanticsNodes().let { nodes ->
            check(nodes.size == expected) { "expected $expected nodes but found ${nodes.size}" }
        }
    }
}
