package com.walkmark.app.data.media

import android.content.Context
import android.net.Uri
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

const val WALK_PHOTOS_DIR: String = "walk_photos"

class AndroidLocalMediaStore(private val context: Context) : LocalMediaStore {

    private val rootDirectory: File
        get() = File(context.filesDir, WALK_PHOTOS_DIR)

    override suspend fun importPhoto(
        sourceUri: String,
        walkId: String,
        displayName: String?
    ): StoredPhoto = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val fileName = "${UUID.randomUUID()}.${extensionFor(mimeType, displayName)}"
        val relativePath = "$walkId/$fileName"

        val target = File(rootDirectory, relativePath)
        target.parentFile?.mkdirs()

        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
            true
        } ?: false

        if (!copied) {
            target.delete()
            throw IllegalStateException("Unable to read photo from source: $sourceUri")
        }

        StoredPhoto(
            relativePath = relativePath,
            mimeType = mimeType,
            byteSize = target.length()
        )
    }

    override suspend fun deletePhoto(relativePath: String) = withContext(Dispatchers.IO) {
        val target = File(rootDirectory, relativePath)
        if (target.exists()) target.delete()
        Unit
    }

    override suspend fun exists(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        File(rootDirectory, relativePath).exists()
    }

    override fun absolutePath(relativePath: String): String =
        File(rootDirectory, relativePath).absolutePath

    private fun extensionFor(mimeType: String, displayName: String?): String {
        displayName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }?.let { return it }
        return when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            else -> "jpg"
        }
    }
}
