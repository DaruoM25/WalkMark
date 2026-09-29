package com.walkmark.app.domain.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberLocationTrackerManager(): LocationTrackerManager {
    val context = LocalContext.current.applicationContext
    return remember(context) { LocationTrackerManager(context) }
}
