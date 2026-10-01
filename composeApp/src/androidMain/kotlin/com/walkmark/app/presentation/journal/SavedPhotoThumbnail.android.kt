package com.walkmark.app.presentation.journal

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun SavedPhotoThumbnail(absolutePath: String, modifier: Modifier) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, absolutePath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(absolutePath, bounds)
                val sample = (maxOf(bounds.outWidth, bounds.outHeight) / 512).coerceAtLeast(1)
                BitmapFactory.decodeFile(absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
            }.getOrNull()
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (bitmap == null) Text("Photo unavailable")
        else Image(bitmap!!.asImageBitmap(), contentDescription = "Saved walk photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
