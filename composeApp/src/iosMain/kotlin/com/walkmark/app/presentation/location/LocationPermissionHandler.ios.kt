package com.walkmark.app.presentation.location

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocationPermissionLauncher(
    onPermissionGranted: () -> Unit
): () -> Unit {
    return {
        onPermissionGranted()
    }
}
