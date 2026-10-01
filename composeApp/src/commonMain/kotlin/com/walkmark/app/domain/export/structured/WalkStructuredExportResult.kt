package com.walkmark.app.domain.export.structured

sealed interface WalkStructuredExportResult {
    data class Success(
        val walkId: String,
        val format: WalkExportFormat,
        val fileName: String,
        val mimeType: String,
        val content: String
    ) : WalkStructuredExportResult

    data class WalkNotFound(val walkId: String) : WalkStructuredExportResult

    data class Failure(
        val walkId: String,
        val message: String
    ) : WalkStructuredExportResult
}
