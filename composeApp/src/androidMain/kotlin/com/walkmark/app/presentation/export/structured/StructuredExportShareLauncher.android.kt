package com.walkmark.app.presentation.export.structured

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

internal const val STRUCTURED_EXPORT_CACHE_DIR = "walkmark-exports"
internal const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

internal fun sanitizeFileName(fileName: String): String {
    val sanitized = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return if (sanitized.isBlank()) "export_data.txt" else sanitized
}

internal fun writeExportFileToCache(
    context: Context,
    fileName: String,
    content: String
): File {
    val dir = File(context.cacheDir, STRUCTURED_EXPORT_CACHE_DIR)
    if (!dir.exists() && !dir.mkdirs()) {
        throw IllegalStateException("Unable to create export cache directory")
    }
    val safeName = sanitizeFileName(fileName)
    val file = File(dir, safeName)
    file.writeText(content, Charsets.UTF_8)
    return file
}

internal fun createStructuredExportShareIntent(
    uri: Uri,
    mimeType: String,
    fileName: String
): Intent = Intent(Intent.ACTION_SEND).apply {
    type = mimeType
    putExtra(Intent.EXTRA_STREAM, uri)
    putExtra(Intent.EXTRA_SUBJECT, fileName)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

internal class AndroidStructuredExportShareLauncher(
    private val context: Context
) : StructuredExportShareLauncher {

    override fun share(
        fileName: String,
        mimeType: String,
        content: String
    ): StructuredExportShareResult {
        return try {
            val file = writeExportFileToCache(context, fileName, content)
            val authority = context.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX
            val uri = FileProvider.getUriForFile(context, authority, file)
            val shareIntent = createStructuredExportShareIntent(uri, mimeType, fileName)
            val chooserIntent = Intent.createChooser(shareIntent, "Export walk data").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
            StructuredExportShareResult.Presented
        } catch (_: ActivityNotFoundException) {
            StructuredExportShareResult.Unavailable
        } catch (_: Exception) {
            StructuredExportShareResult.Failure
        }
    }
}

@Composable
actual fun rememberStructuredExportShareLauncher(): StructuredExportShareLauncher {
    val context = LocalContext.current
    return remember(context) { AndroidStructuredExportShareLauncher(context) }
}
