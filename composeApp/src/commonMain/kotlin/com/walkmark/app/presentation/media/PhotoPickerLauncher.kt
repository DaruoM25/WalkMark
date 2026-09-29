package com.walkmark.app.presentation.media

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that opens the system photo picker.
 * The emitted value is a platform-transient source reference (an Android `content://` URI or an
 * iOS file URL). It must be handed to `LocalMediaStore.importPhoto` immediately and must never
 * be persisted; only the returned app-private relative path is durable.
 */
@Composable
expect fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit
