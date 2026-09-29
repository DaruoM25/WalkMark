package com.walkmark.app.presentation.location

import androidx.compose.runtime.Composable

@Composable
expect fun rememberLocationPermissionLauncher(
    onPermissionGranted: () -> Unit
): () -> Unit
