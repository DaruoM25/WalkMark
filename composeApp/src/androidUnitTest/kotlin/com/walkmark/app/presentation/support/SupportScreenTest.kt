package com.walkmark.app.presentation.support

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.walkmark.app.presentation.theme.WalkMarkTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalResourceApi::class)
class SupportScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeLauncher(
        private val result: SupportContactResult = SupportContactResult.Success
    ) : SupportContactLauncher {
        var request: SupportContactRequest? = null
        override fun launch(request: SupportContactRequest, onResult: (SupportContactResult) -> Unit) {
            this.request = request
            onResult(result)
        }
    }

    @Test
    fun supportContentAndApprovedFaqTopicsAreVisible() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                WalkMarkTheme {
                    SupportScreen(SupportContactConfig.NotConfigured, {}, contactLauncher = FakeLauncher())
                }
            }
        }

        composeTestRule.onNodeWithTag("support_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("support_privacy_notice").assertIsDisplayed()
        supportFaqItems.forEach { composeTestRule.onNodeWithTag("support_faq_${it.id}").fetchSemanticsNode() }
        composeTestRule.onNodeWithTag("support_contact_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("support_contact_not_configured").assertIsDisplayed()
    }

    @Test
    fun configuredContactInvokesLauncherAndFailureIsVisible() {
        val launcher = FakeLauncher(SupportContactResult.NoCompatibleApp)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                WalkMarkTheme {
                    SupportScreen(SupportContactConfig.Email("support@example.com"), {}, contactLauncher = launcher)
                }
            }
        }

        composeTestRule.onNodeWithTag("support_contact_button").assertIsEnabled().performClick()
        check(launcher.request?.recipient == "support@example.com")
        composeTestRule.onNodeWithTag("support_contact_error").assertIsDisplayed()
    }
}
