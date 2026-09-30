package com.walkmark.app.presentation.settings

import com.walkmark.app.data.walk.InMemoryWalkRepository
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import com.walkmark.app.domain.walk.WalkPhoto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FailingMediaStore : LocalMediaStore {
        override suspend fun importPhoto(sourceUri: String, walkId: String, displayName: String?): StoredPhoto {
            return StoredPhoto("dummy.jpg", "image/jpeg", 100L)
        }
        override suspend fun deletePhoto(relativePath: String) {
            throw RuntimeException("Disk I/O failure")
        }
        override suspend fun exists(relativePath: String): Boolean = true
        override fun absolutePath(relativePath: String): String = relativePath
    }

    @Test
    fun deleteAllWalksWithNoWalksReportsSuccessZero() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repository = InMemoryWalkRepository()
        val viewModel = SettingsViewModel(walkRepository = repository, scope = scope)

        assertEquals(BulkDeleteState.Idle, viewModel.bulkDeleteState.value)

        viewModel.deleteAllWalks()
        scope.advanceUntilIdle()

        val state = viewModel.bulkDeleteState.value
        assertIs<BulkDeleteState.Success>(state)
        assertEquals(0, state.deletedCount)
    }

    @Test
    fun deleteAllWalksDeletesAllWalksSuccessfully() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repository = InMemoryWalkRepository()

        repository.startWalk("w1", "Morning Walk", 1000L)
        repository.startWalk("w2", "Evening Walk", 2000L)

        val viewModel = SettingsViewModel(walkRepository = repository, scope = scope)

        viewModel.deleteAllWalks()
        scope.advanceUntilIdle()

        val state = viewModel.bulkDeleteState.value
        assertIs<BulkDeleteState.Success>(state)
        assertEquals(2, state.deletedCount)
    }

    @Test
    fun deleteAllWalksReportsPartialSuccessWhenMediaCleanupFails() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repository = InMemoryWalkRepository(mediaStore = FailingMediaStore())

        val walk = repository.startWalk("w1", "Photo Walk", 1000L)
        repository.addPhoto(
            WalkPhoto(
                id = "p1",
                walkId = walk.id,
                latitude = 48.8566,
                longitude = 2.3522,
                relativePath = "photos/photo1.jpg",
                mimeType = "image/jpeg",
                byteSize = 1024L,
                createdAtEpochMs = 1050L
            )
        )

        val viewModel = SettingsViewModel(walkRepository = repository, scope = scope)

        viewModel.deleteAllWalks()
        scope.advanceUntilIdle()

        val state = viewModel.bulkDeleteState.value
        assertIs<BulkDeleteState.PartialSuccess>(state)
        assertEquals(1, state.deletedCount)
        assertEquals(1, state.mediaFailureCount)
    }

    @Test
    fun resetBulkDeleteStateRestoresIdleState() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(testDispatcher)
        val repository = InMemoryWalkRepository()
        val viewModel = SettingsViewModel(walkRepository = repository, scope = scope)

        viewModel.deleteAllWalks()
        scope.advanceUntilIdle()

        assertIs<BulkDeleteState.Success>(viewModel.bulkDeleteState.value)

        viewModel.resetBulkDeleteState()
        assertEquals(BulkDeleteState.Idle, viewModel.bulkDeleteState.value)
    }
}
