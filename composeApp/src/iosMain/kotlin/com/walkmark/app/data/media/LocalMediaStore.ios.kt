package com.walkmark.app.data.media

import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

private const val WALK_PHOTOS_DIR = "walk_photos"

class IosLocalMediaStore : LocalMediaStore {

    private val fileManager = NSFileManager.defaultManager

    private fun rootPath(): String {
        val documents = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        ).firstOrNull() as? String ?: error("Documents directory unavailable")
        return "$documents/$WALK_PHOTOS_DIR"
    }

    override suspend fun importPhoto(
        sourceUri: String,
        walkId: String,
        displayName: String?
    ): StoredPhoto = withContext(Dispatchers.IO) {
        val source = NSURL.URLWithString(sourceUri) ?: error("Invalid source URL: $sourceUri")
        val fileName = NSUUID().UUIDString().let { uuid ->
            val extension = displayName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
                ?: source.lastPathComponent?.substringAfterLast('.', "").orEmpty().ifBlank { "jpg" }
            "$uuid.$extension"
        }
        val relativePath = "$walkId/$fileName"

        val directory = "${rootPath()}/$walkId"
        fileManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        val destinationPath = "${rootPath()}/$relativePath"
        val destination = NSURL.fileURLWithPath(destinationPath)

        val copied = fileManager.copyItemAtURL(source, destination, null)
        if (!copied) {
            fileManager.removeItemAtPath(destinationPath, null)
            error("Unable to copy photo from source: $sourceUri")
        }

        val attributes = fileManager.attributesOfItemAtPath(destinationPath, null)
        val byteSize = (attributes?.get(NSFileSize) as? Number)?.toLong() ?: 0L

        StoredPhoto(
            relativePath = relativePath,
            mimeType = mimeTypeForExtension(fileName.substringAfterLast('.', "")),
            byteSize = byteSize
        )
    }

    override suspend fun deletePhoto(relativePath: String) = withContext(Dispatchers.IO) {
        fileManager.removeItemAtPath("${rootPath()}/$relativePath", null)
        Unit
    }

    override suspend fun exists(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        fileManager.fileExistsAtPath("${rootPath()}/$relativePath")
    }

    override fun absolutePath(relativePath: String): String = "${rootPath()}/$relativePath"

    private fun mimeTypeForExtension(extension: String): String = when (extension.lowercase()) {
        "png" -> "image/png"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }
}
