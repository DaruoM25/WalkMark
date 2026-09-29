package com.walkmark.app.domain.repository

data class StoredPhoto(
    val relativePath: String,
    val mimeType: String,
    val byteSize: Long
)

interface LocalMediaStore {
    suspend fun importPhoto(
        sourceUri: String,
        walkId: String,
        displayName: String? = null
    ): StoredPhoto

    suspend fun deletePhoto(relativePath: String)

    suspend fun exists(relativePath: String): Boolean

    fun absolutePath(relativePath: String): String
}
