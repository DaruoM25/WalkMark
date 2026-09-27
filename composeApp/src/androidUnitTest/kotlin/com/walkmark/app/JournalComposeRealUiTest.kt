package com.walkmark.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.walkmark.app.presentation.journal.JournalScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JournalComposeRealUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testJournalScreenRendersWithSemanticTags() {
        composeTestRule.setContent {
            WalkMarkTheme {
                JournalScreen()
            }
        }

        // Assert container and header nodes are displayed
        composeTestRule.onNodeWithTag("journal_screen_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_ready_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_ready_header").assertTextEquals("WalkMark: Ready for Shipaton")
        composeTestRule.onNodeWithTag("journal_subtitle").assertIsDisplayed()
    }
}

