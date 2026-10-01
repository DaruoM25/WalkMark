package com.walkmark.app.domain.export

import com.walkmark.app.domain.repository.WalkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WalkSummaryExportResult {
    data class Success(val text: String) : WalkSummaryExportResult
    data object WalkNotFound : WalkSummaryExportResult
    data object Failure : WalkSummaryExportResult
}

class ExportWalkSummaryUseCase(
    private val repository: WalkRepository,
    private val generator: WalkSummaryGenerator = WalkSummaryGenerator()
) {
    suspend operator fun invoke(walkId: String): WalkSummaryExportResult {
        return try {
            val walk = repository.getWalk(walkId) ?: return WalkSummaryExportResult.WalkNotFound
            val notes = repository.observeNotes(walkId).first()
            val photos = repository.observePhotos(walkId).first()
            WalkSummaryExportResult.Success(generator.generate(walk, notes.size, photos.size))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            WalkSummaryExportResult.Failure
        }
    }
}
