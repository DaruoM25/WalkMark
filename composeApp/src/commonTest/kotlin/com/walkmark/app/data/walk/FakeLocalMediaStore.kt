package com.walkmark.app.data.walk

import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto

class FakeLocalMediaStore : LocalMediaStore {

    val deletedPaths = mutableListOf<String>()
    val failOnDelete = mutableSetOf<String>()
    val storedPaths = mutableSetOf<String>()

    override suspend fun importPhoto(
        sourceUri: String,
        walkId: String,
        displayName: String?
    ): StoredPhoto {
        val path = "walks/$walkId/${displayName ?: "photo"}.jpg"
        storedPaths += path
        return StoredPhoto(relativePath = path, mimeType = "image/jpeg", byteSize = 1024L)
    }

    override suspend fun deletePhoto(relativePath: String) {
        if (relativePath in failOnDelete) {
            throw IllegalStateException("Simulated delete failure for: $relativePath")
        }
        storedPaths -= relativePath
        deletedPaths += relativePath
    }

    override suspend fun exists(relativePath: String): Boolean = relativePath in storedPaths

    override fun absolutePath(relativePath: String): String = "/data/local/tmp/$relativePath"
}