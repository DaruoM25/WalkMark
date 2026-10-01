package com.walkmark.app.domain.export.structured

import com.walkmark.app.core.model.LocationPoint
import com.walkmark.app.core.model.WalkNote
import com.walkmark.app.core.model.WalkPhoto
import com.walkmark.app.domain.walk.WalkRepository
import kotlinx.coroutines.flow.first

class ExportWalkDataUseCase(
    private val walkRepository: WalkRepository
) {
    suspend operator fun invoke(
        walkId: String,
        format: WalkExportFormat
    ): WalkStructuredExportResult {
        return try {
            val walk = walkRepository.getWalk(walkId)
                ?: return WalkStructuredExportResult.WalkNotFound(walkId)

            val points: List<LocationPoint> = walkRepository.observePoints(walkId).first()
            val notes: List<WalkNote> = walkRepository.observeNotes(walkId).first()
            val photos: List<WalkPhoto> = walkRepository.observePhotos(walkId).first()

            val content = when (format) {
                WalkExportFormat.JSON -> WalkStructuredExportGenerator.generateJson(walk, points, notes, photos)
                WalkExportFormat.CSV -> WalkStructuredExportGenerator.generateCsv(points)
            }

            val sanitizedId = walkId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "walk_${sanitizedId}.${format.extension}"

            WalkStructuredExportResult.Success(
                walkId = walkId,
                format = format,
                fileName = fileName,
                mimeType = format.mimeType,
                content = content
            )
        } catch (e: Exception) {
            WalkStructuredExportResult.Failure(
                walkId = walkId,
                message = "Failed to export walk data."
            )
        }
    }
}
