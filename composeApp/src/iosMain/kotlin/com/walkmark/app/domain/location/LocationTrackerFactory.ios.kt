package com.walkmark.app.domain.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberLocationTrackerManager(): LocationTrackerManager {
    return remember { LocationTrackerManager() }
}
