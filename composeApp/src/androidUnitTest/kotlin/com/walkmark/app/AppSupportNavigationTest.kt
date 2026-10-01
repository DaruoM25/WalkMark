package com.walkmark.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.walkmark.app.data.walk.RecordingWalkRepository
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.location.LocationRepository
import com.walkmark.app.domain.location.LocationTrackingState
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import com.walkmark.app.presentation.support.SupportContactConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalResourceApi::class)
class AppSupportNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private class TrackingRepository : LocationRepository {
        private val state = MutableStateFlow<LocationTrackingState>(LocationTrackingState.Tracking(1L))
        override val trackingState: StateFlow<LocationTrackingState> = state.asStateFlow()
        override val acceptedPoints = MutableStateFlow<List<LocationPoint>>(emptyList()).asStateFlow()
        override val totalDistanceMeters = MutableStateFlow(0.0).asStateFlow()
        override val lastLocation = MutableStateFlow<LocationPoint?>(null).asStateFlow()
        override fun startTracking() = Unit
        override fun pauseTracking() = Unit
        override fun resumeTracking() = Unit
        override fun stopTracking() { state.value = LocationTrackingState.Idle }
        override fun clearSession() = Unit
    }

    private object FakeMediaStore : LocalMediaStore {
        override suspend fun importPhoto(sourceUri: String, walkId: String, displayName: String?) =
            StoredPhoto("unused", "image/jpeg", 0L)
        override suspend fun deletePhoto(relativePath: String) = Unit
        override suspend fun exists(relativePath: String) = false
        override fun absolutePath(relativePath: String) = relativePath
    }

    @Test
    fun settingsAndSupportDestinationReturnsToPreservedTrackingState() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                App(
                    locationRepository = TrackingRepository(),
                    walkRepository = RecordingWalkRepository(),
                    mediaStore = FakeMediaStore,
                    supportContactConfig = SupportContactConfig.NotConfigured
                )
            }
        }

        composeTestRule.onNodeWithTag("stop_walk_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_account").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("settings_entry_button").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_support_button").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("support_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("support_back_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_back_button").performClick()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("stop_walk_button").assertIsDisplayed()
    }
}
