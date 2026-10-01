package com.walkmark.app.presentation.export

import com.walkmark.app.domain.export.ExportWalkUseCase
import com.walkmark.app.domain.export.WalkExportResult
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WalkExportError {
    WalkNotFound,
    NoCompatibleApp,
    ShareFailed
}

sealed interface WalkExportState {
    data object Idle : WalkExportState
    data object Exporting : WalkExportState
    data class Success(val walkId: String, val fileName: String) : WalkExportState
    data class Error(val reason: WalkExportError) : WalkExportState
}

/**
 * Presentation state for the saved-walks export flow.
 *
 * Platform-neutral by construction: it depends only on [WalkRepository], the domain
 * [ExportWalkUseCase], and the provider-neutral [GpxShareLauncher]. It holds no Context,
 * Intent, Uri, NSURL, or filesystem path, and [WalkExportState.Error] carries a closed
 * enum reason rather than an exception message so no internal detail reaches the UI.
 */
class SavedWalksViewModel(
    walkRepository: WalkRepository,
    private val exportWalk: ExportWalkUseCase,
    private val gpxShareLauncher: GpxShareLauncher,
    private val scope: CoroutineScope
) {

    val walks: StateFlow<List<Walk>> = walkRepository.observeAllWalks()
        .map { persisted -> persisted.filter { it.status == WalkStatus.COMPLETED } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val _exportState = MutableStateFlow<WalkExportState>(WalkExportState.Idle)
    val exportState: StateFlow<WalkExportState> = _exportState.asStateFlow()

    fun resetExportState() {
        _exportState.value = WalkExportState.Idle
    }

    /**
     * Generates the GPX document and hands it to the platform share mechanism.
     * Ignored while an export is already in flight so a single walk cannot produce
     * duplicate share requests.
     */
    fun exportGpx(walkId: String) {
        if (_exportState.value is WalkExportState.Exporting) return
        _exportState.value = WalkExportState.Exporting

        scope.launch {
            val result = try {
                exportWalk.exportGpx(walkId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _exportState.value = WalkExportState.Error(WalkExportError.ShareFailed)
                return@launch
            }

            when (result) {
                is WalkExportResult.WalkNotFound ->
                    _exportState.value = WalkExportState.Error(WalkExportError.WalkNotFound)

                is WalkExportResult.Success -> gpxShareLauncher.launch(
                    GpxShareRequest(fileName = result.fileName, content = result.gpxContent)
                ) { shareResult ->
                    _exportState.value = when (shareResult) {
                        GpxShareResult.Shared ->
                            WalkExportState.Success(walkId = result.walkId, fileName = result.fileName)

                        GpxShareResult.NoCompatibleApp ->
                            WalkExportState.Error(WalkExportError.NoCompatibleApp)

                        GpxShareResult.Failure ->
                            WalkExportState.Error(WalkExportError.ShareFailed)
                    }
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
