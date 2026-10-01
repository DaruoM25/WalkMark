package com.walkmark.app.presentation.journal

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.walkmark.app.data.walk.RecordingWalkRepository
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import com.walkmark.app.presentation.theme.WalkMarkTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkHistoryDetailUiTest {
    @get:Rule val rule = createComposeRule()

    private val walk = Walk("walk-1", "Morning walk", "Park loop", 1_700_000_000_000, 1_700_000_600_000, 1200.0, 600, WalkStatus.COMPLETED)

    private object Media : LocalMediaStore {
        override suspend fun importPhoto(sourceUri: String, walkId: String, displayName: String?) = StoredPhoto("unused", "image/jpeg", 0)
        override suspend fun deletePhoto(relativePath: String) = Unit
        override suspend fun exists(relativePath: String) = false
        override fun absolutePath(relativePath: String) = "/missing/$relativePath"
    }

    private class Repo(private val walk: Walk) : WalkRepository by RecordingWalkRepository() {
        var deleted = false
        override fun observeWalkById(walkId: String): Flow<Walk?> = flowOf(walk)
        override fun observePoints(walkId: String): Flow<List<LocationPoint>> = flowOf(listOf(
            LocationPoint(48.0, 2.0, timestamp = 1, accuracy = 5f),
            LocationPoint(48.1, 2.1, timestamp = 2, accuracy = 5f)
        ))
        override fun observeNotes(walkId: String): Flow<List<WalkNote>> = flowOf(listOf(WalkNote("note-1", walkId, "Nice trees", 48.0, 2.0, 1)))
        override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> = flowOf(listOf(WalkPhoto("photo-1", walkId, 48.0, 2.0, "missing.jpg", "image/jpeg", 12, 1)))
        override suspend fun deleteWalk(walkId: String): WalkDeleteResult {
            deleted = true
            return WalkDeleteResult.SuccessWithMediaCleanupFailures(listOf("missing.jpg"))
        }
    }

    @Test fun emptyHistoryIsVisible() {
        rule.setContent { WalkMarkTheme { WalkHistoryContent(WalkHistoryState.Ready(emptyList()), onOpenWalk = {}) } }
        rule.onNodeWithTag("history_empty").assertIsDisplayed()
    }

    @Test fun selectedWalkDetailIsVisible() {
        var selected: String? = null
        rule.setContent { WalkMarkTheme { WalkHistoryContent(WalkHistoryState.Ready(listOf(walk)), onOpenWalk = { selected = it }) } }
        rule.onNodeWithTag("history_walk_walk-1").performClick()
        assertEquals("walk-1", selected)
    }

    @Test fun detailShowsPersistedContentAndRequiresDeleteConfirmation() {
        val repo = Repo(walk)
        var result: WalkDeleteResult? = null
        rule.setContent { WalkMarkTheme { WalkDetailScreen(walk.id, repo, Media, onBack = {}, onDeleted = { result = it }) } }
        rule.onNodeWithTag("walk_route_preview").assertIsDisplayed()
        rule.onNodeWithTag("detail_note_note-1").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("detail_photo_photo-1").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("detail_delete_button").performScrollTo().performClick()
        assertEquals(false, repo.deleted)
        rule.onNodeWithTag("detail_confirm_delete").performClick()
        rule.waitUntil(5_000) { repo.deleted && result != null }
        assertEquals("Walk deleted, but some photo files could not be removed.", deletionNotice(result!!))
    }
}
