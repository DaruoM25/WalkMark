package com.walkmark.app.presentation.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptiveScaffoldComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScaffoldRendersCompactSinglePane() {
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                AdaptiveWalkScaffold(
                    posture = DevicePosture.Normal,
                    primaryContent = { _ ->
                        Text("Primary Content", modifier = Modifier.testTag("test_primary_content"))
                    },
                    secondaryContent = { _ ->
                        Text("Secondary Content", modifier = Modifier.testTag("test_secondary_content"))
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("adaptive_walk_scaffold_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_primary_content").assertIsDisplayed()
    }

    @Test
    fun testScaffoldRendersFlexTableTopLayout() {
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                AdaptiveWalkScaffold(
                    posture = DevicePosture.TableTop,
                    primaryContent = { _ ->
                        Text("Upper Primary", modifier = Modifier.testTag("test_upper_primary"))
                    },
                    secondaryContent = { _ ->
                        Text("Lower Secondary", modifier = Modifier.testTag("test_lower_secondary"))
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("scaffold_flex_layout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_upper_primary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_lower_secondary").assertIsDisplayed()
    }
}

