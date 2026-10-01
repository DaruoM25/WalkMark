package com.walkmark.app.presentation.export

import com.walkmark.app.data.walk.InMemoryWalkRepository
import com.walkmark.app.domain.export.ExportWalkUseCase
import com.walkmark.app.domain.location.LocationPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GpxExportViewModelTest {

    private val repository = InMemoryWalkRepository()

    private class RecordingLauncher(
        private val result: GpxShareResult = GpxShareResult.Shared,
        private val autoComplete: Boolean = true
    ) : GpxShareLauncher {
        val requests = mutableListOf<GpxShareRequest>()
        private val pending = mutableListOf<(GpxShareResult) -> Unit>()

        override fun launch(request: GpxShareRequest, onResult: (GpxShareResult) -> Unit) {
            requests += request
            if (autoComplete) onResult(result) else pending += onResult
        }

        fun completePending() {
            val callbacks = pending.toList()
            pending.clear()
            callbacks.forEach { it(result) }
        }
    }

    /** Returns a [TestScope] so the test can drive `advanceUntilIdle`; it is a CoroutineScope. */
    private fun TestScope.newScope(): TestScope = TestScope(StandardTestDispatcher(testScheduler))

    private suspend fun seedCompletedWalk(walkId: String = "walk-1", title: String = "Morning loop") {
        repository.startWalk(walkId = walkId, title = title, startTimeEpochMs = 1_700_000_000_000L)
        repository.completeWalk(
            walkId = walkId,
            endTimeEpochMs = 1_700_003_600_000L,
            totalDistanceMeters = 10.0
        )
        repository.appendPoints(
            walkId = walkId,
            points = listOf(
                LocationPoint(1.0, 1.0, null, 1_700_000_000_000L, 4f),
                LocationPoint(2.0, 2.0, 10.0, 1_700_000_001_000L, 4f)
            )
        )
    }

    @Test
    fun initialStateIsIdle() = runTest {
        val scope = newScope()
        val viewModel = GpxExportViewModel(ExportWalkUseCase(repository), RecordingLauncher(), scope)

        assertIs<WalkExportState.Idle>(viewModel.exportState.value)
    }

    @Test
    fun successfulExportReportsSharedFileName() = runTest {
        val scope = newScope()
        seedCompletedWalk()
        val launcher = RecordingLauncher(GpxShareResult.Shared)
        val viewModel = GpxExportViewModel(ExportWalkUseCase(repository), launcher, scope)

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()

        val state = assertIs<WalkExportState.Success>(viewModel.exportState.value)
        assertEquals("walk-1", state.walkId)
        assertEquals("walkmark-morning-loop-walk-1.gpx", state.fileName)
        assertEquals(1, launcher.requests.size)
        assertTrue(launcher.requests.single().content.contains("version=\"1.1\""))
    }

    @Test
    fun launcherFailureReportsErrorWithoutFilesystemPath() = runTest {
        val scope = newScope()
        seedCompletedWalk()
        val viewModel = GpxExportViewModel(
            ExportWalkUseCase(repository),
            RecordingLauncher(GpxShareResult.Failure),
            scope
        )

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()

        val state = assertIs<WalkExportState.Error>(viewModel.exportState.value)
        assertEquals(WalkExportError.ShareFailed, state.reason)
        assertFalse(state.reason.toString().contains('/'), state.reason.toString())
        assertFalse(state.reason.toString().contains("\\"), state.reason.toString())
    }

    @Test
    fun noCompatibleAppReportsDedicatedError() = runTest {
        val scope = newScope()
        seedCompletedWalk()
        val viewModel = GpxExportViewModel(
            ExportWalkUseCase(repository),
            RecordingLauncher(GpxShareResult.NoCompatibleApp),
            scope
        )

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()

        val state = assertIs<WalkExportState.Error>(viewModel.exportState.value)
        assertEquals(WalkExportError.NoCompatibleApp, state.reason)
    }

    @Test
    fun missingWalkReportsWalkNotFoundAndNeverCallsLauncher() = runTest {
        val scope = newScope()
        val launcher = RecordingLauncher()
        val viewModel = GpxExportViewModel(ExportWalkUseCase(repository), launcher, scope)

        viewModel.exportGpx("does-not-exist")
        scope.advanceUntilIdle()

        val state = assertIs<WalkExportState.Error>(viewModel.exportState.value)
        assertEquals(WalkExportError.WalkNotFound, state.reason)
        assertTrue(launcher.requests.isEmpty())
    }

    @Test
    fun duplicateExportRequestWhileExportingIsIgnored() = runTest {
        val scope = newScope()
        seedCompletedWalk()
        val launcher = RecordingLauncher(autoComplete = false)
        val viewModel = GpxExportViewModel(ExportWalkUseCase(repository), launcher, scope)

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()
        assertIs<WalkExportState.Exporting>(viewModel.exportState.value)

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()

        assertEquals(1, launcher.requests.size)
        assertIs<WalkExportState.Exporting>(viewModel.exportState.value)

        launcher.completePending()
        assertIs<WalkExportState.Success>(viewModel.exportState.value)
    }

    @Test
    fun resetReturnsStateToIdle() = runTest {
        val scope = newScope()
        seedCompletedWalk()
        val viewModel = GpxExportViewModel(ExportWalkUseCase(repository), RecordingLauncher(), scope)

        viewModel.exportGpx("walk-1")
        scope.advanceUntilIdle()
        assertIs<WalkExportState.Success>(viewModel.exportState.value)

        viewModel.resetExportState()

        assertIs<WalkExportState.Idle>(viewModel.exportState.value)
    }
}
