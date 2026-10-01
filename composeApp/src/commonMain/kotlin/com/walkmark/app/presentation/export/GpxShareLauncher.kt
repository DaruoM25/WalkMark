package com.walkmark.app.presentation.export

import androidx.compose.runtime.Composable

/**
 * Provider-neutral contract for writing a GPX document to a temporary location and
 * handing it to the platform share/export mechanism.
 *
 * The interface deliberately carries only a file *name* and in-memory *content*, so
 * shared presentation code never sees a filesystem path, `Uri`, `NSURL`, `Intent`, or
 * `Context`. Implementations own file creation, cleanup, and the platform handoff.
 */
interface GpxShareLauncher {
    fun launch(request: GpxShareRequest, onResult: (GpxShareResult) -> Unit)
}

data class GpxShareRequest(
    val fileName: String,
    val content: String
)

enum class GpxShareResult {
    Shared,
    NoCompatibleApp,
    Failure
}

@Composable
expect fun rememberGpxShareLauncher(): GpxShareLauncher
