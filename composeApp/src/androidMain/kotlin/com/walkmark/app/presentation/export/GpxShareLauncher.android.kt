package com.walkmark.app.presentation.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal const val GPX_MIME_TYPE = "application/gpx+xml"
internal const val GPX_EXPORT_AUTHORITY_SUFFIX = ".fileprovider"
internal const val GPX_CACHE_DIRECTORY = "walkmark-gpx"

internal fun createGpxShareIntent(uri: Uri, fileName: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = GPX_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, fileName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

internal class AndroidGpxShareLauncher(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : GpxShareLauncher {

    override fun launch(request: GpxShareRequest, onResult: (GpxShareResult) -> Unit) {
        scope.launch {
            val intent = runCatching {
                val file = writeGpxToCache(request)
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + GPX_EXPORT_AUTHORITY_SUFFIX,
                    file
                )
                createGpxShareIntent(uri = uri, fileName = request.fileName)
            }.getOrNull() ?: return@launch withContext(Dispatchers.Main) {
                onResult(GpxShareResult.Failure)
            }

            withContext(Dispatchers.Main) {
                if (intent.resolveActivity(context.packageManager) == null) {
                    onResult(GpxShareResult.NoCompatibleApp)
                    return@withContext
                }
                runCatching { context.startActivity(intent) }
                    .onSuccess { onResult(GpxShareResult.Shared) }
                    .onFailure { onResult(GpxShareResult.Failure) }
            }
        }
    }

    /**
     * Writes the document to the app-private cache. Nothing is published to public or
     * external storage and no storage permission is required. The cache directory is
     * transient by design, so the platform can reclaim it after the share completes.
     */
    private fun writeGpxToCache(request: GpxShareRequest): File {
        val directory = File(context.cacheDir, GPX_CACHE_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("Unable to prepare the export cache directory")
        }
        val file = File(directory, request.fileName)
        file.writeText(request.content, Charsets.UTF_8)
        return file
    }
}

@Composable
actual fun rememberGpxShareLauncher(): GpxShareLauncher {
    val context = LocalContext.current
    return remember(context) { AndroidGpxShareLauncher(context) }
}
