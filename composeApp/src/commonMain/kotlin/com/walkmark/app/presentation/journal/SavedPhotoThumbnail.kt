package com.walkmark.app.presentation.journal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Displays only an existing app-private local photo; no picker or network access. */
@Composable
expect fun SavedPhotoThumbnail(absolutePath: String, modifier: Modifier = Modifier)
