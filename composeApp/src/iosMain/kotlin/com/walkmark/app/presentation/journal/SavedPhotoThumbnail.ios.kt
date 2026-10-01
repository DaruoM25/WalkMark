package com.walkmark.app.presentation.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentModeScaleAspectFill

@Composable
actual fun SavedPhotoThumbnail(absolutePath: String, modifier: Modifier) {
    val image = remember(absolutePath) { UIImage.imageWithContentsOfFile(absolutePath) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (image == null) Text("Photo unavailable")
        else UIKitView(
            factory = { UIImageView().apply { contentMode = UIViewContentModeScaleAspectFill; clipsToBounds = true } },
            modifier = Modifier.fillMaxSize(),
            update = { it.image = image }
        )
    }
}
