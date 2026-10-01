package com.walkmark.app.presentation.export.structured

import androidx.compose.runtime.Composable

sealed interface StructuredExportShareResult {
    data object Presented : StructuredExportShareResult
    data object Unavailable : StructuredExportShareResult
    data object Failure : StructuredExportShareResult
}

interface StructuredExportShareLauncher {
    fun share(
        fileName: String,
        mimeType: String,
        content: String
    ): StructuredExportShareResult
}

@Composable
expect fun rememberStructuredExportShareLauncher(): StructuredExportShareLauncher
