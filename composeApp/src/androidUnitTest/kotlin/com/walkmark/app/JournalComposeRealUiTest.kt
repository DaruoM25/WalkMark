package com.walkmark.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.presentation.journal.JournalScreen
import com.walkmark.app.presentation.location.LocationViewModel
import com.walkmark.app.presentation.location.TrackingScreen
import com.walkmark.app.presentation.theme.WalkMarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private class FakeTestRepo(
        initialState: LocationTrackingState = LocationTrackingState.Idle
    ) : LocationRepository {
        private val _trackingState = MutableStateFlow(initialState)
        override val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

        private val _acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
        override val acceptedPoints: StateFlow<List<LocationPoint>> = _acceptedPoints.asStateFlow()

        private val _totalDistanceMeters = MutableStateFlow(0.0)
        override val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

        private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
        override val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

        override fun startTracking() {
            _trackingState.value = LocationTrackingState.Tracking(1000L)
        }

        override fun pauseTracking() {}
        override fun resumeTracking() {}
        override fun stopTracking() {
            _trackingState.value = LocationTrackingState.Idle
        }
        override fun clearSession() {}
    }

    @Test
    fun testJournalScreenRendersWithSemanticTags() {
        composeTestRule.setContent {
            WalkMarkTheme {
                JournalScreen()
            }
        }

        // Assert container and header nodes are displayed
        composeTestRule.onNodeWithTag("journal_screen_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("journal_title_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("journal_subtitle").assertIsDisplayed()
    }

    @Test
    fun testTrackingScreenRendersIdleStateWithStartButton() {
        val repo = FakeTestRepo(LocationTrackingState.Idle)
        val viewModel = LocationViewModel(repo)

        composeTestRule.setContent {
            WalkMarkTheme {
                TrackingScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("tracking_screen_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tracking_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tracking_title").assertTextEquals("WalkMark")
        composeTestRule.onNodeWithTag("tracking_status_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tracking_status_text").assertTextEquals("Status: Idle")
        composeTestRule.onNodeWithTag("start_walk_button").assertIsDisplayed()
    }

    @Test
    fun testTrackingScreenRendersTrackingStateWithStopButton() {
        val repo = FakeTestRepo(LocationTrackingState.Tracking(1000L))
        val viewModel = LocationViewModel(repo)

        composeTestRule.setContent {
            WalkMarkTheme {
                TrackingScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("tracking_status_text").assertTextEquals("Status: Tracking")
        composeTestRule.onNodeWithTag("stop_walk_button").assertIsDisplayed()
    }
}

