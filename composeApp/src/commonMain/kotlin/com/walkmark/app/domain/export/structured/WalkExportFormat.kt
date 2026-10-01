package com.walkmark.app.domain.export.structured

enum class WalkExportFormat(
    val extension: String,
    val mimeType: String
) {
    JSON(extension = "json", mimeType = "application/json"),
    CSV(extension = "csv", mimeType = "text/csv")
}
