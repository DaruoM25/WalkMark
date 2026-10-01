package com.walkmark.app.presentation.share

import androidx.compose.runtime.Composable

sealed interface WalkSummaryShareResult {
    data object Shared : WalkSummaryShareResult
    data object NoCompatibleApp : WalkSummaryShareResult
    data object Failure : WalkSummaryShareResult
}

interface WalkSummaryShareLauncher {
    /** Reports whether the platform share UI could be opened, not whether the user completed sharing. */
    fun share(text: String): WalkSummaryShareResult
}

@Composable
expect fun rememberWalkSummaryShareLauncher(): WalkSummaryShareLauncher
