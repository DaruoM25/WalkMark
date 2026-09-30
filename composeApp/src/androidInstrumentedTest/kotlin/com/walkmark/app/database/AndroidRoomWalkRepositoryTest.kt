package com.walkmark.app.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.data.walk.RoomWalkRepository
import com.walkmark.app.domain.repository.LocalMediaStore
import com.walkmark.app.domain.repository.StoredPhoto
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkPhoto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidRoomWalkRepositoryTest {

    private lateinit var database: WalkMarkDatabase
    private lateinit var mediaStore: AndroidTestLocalMediaStore
    private lateinit var repo: RoomWalkRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WalkMarkDatabase::class.java
        ).allowMainThreadQueries().build()
        mediaStore = AndroidTestLocalMediaStore()
        repo = RoomWalkRepository(database, mediaStore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeWalkById_emitsNullForUnknownId() = runTest {
        assertNull(repo.observeWalkById("no-such-id").first())
    }

    @Test
    fun observeWalkById_emitsWalkAfterInsert() = runTest {
        repo.startWalk("w1", "Morning Walk", 1_000L)
        val walk = repo.observeWalkById("w1").first()
        assertEquals("w1", walk?.id)
    }

    @Test
    fun deleteWalk_returnsSuccessWhenNoPhotos() = runTest {
        repo.startWalk("w2", "Empty Walk", 2_000L)
        val result = repo.deleteWalk("w2")
        assertIs<WalkDeleteResult.Success>(result)
        assertNull(repo.observeWalkById("w2").first())
    }

    @Test
    fun deleteWalk_attemptsMediaCleanupAndReturnsSuccess() = runTest {
        repo.startWalk("w3", "Photo Walk", 3_000L)
        val photo = WalkPhoto(
            id = "p1",
            walkId = "w3",
            latitude = 0.0,
            longitude = 0.0,
            relativePath = "walks/w3/img1.jpg",
            mimeType = "image/jpeg",
            byteSize = 512L,
            createdAtEpochMs = 1_000L
        )
        mediaStore.storedPaths += photo.relativePath
        repo.addPhoto(photo)

        val result = repo.deleteWalk("w3")

        assertIs<WalkDeleteResult.Success>(result)
        assertTrue(mediaStore.deletedPaths.contains(photo.relativePath))
    }

    @Test
    fun deleteWalk_returnsPartialFailureOnMediaCleanupError() = runTest {
        repo.startWalk("w4", "Fail Walk", 4_000L)
        val photo = WalkPhoto(
            id = "p2",
            walkId = "w4",
            latitude = 0.0,
            longitude = 0.0,
            relativePath = "walks/w4/img2.jpg",
            mimeType = "image/jpeg",
            byteSize = 512L,
            createdAtEpochMs = 2_000L
        )
        mediaStore.storedPaths += photo.relativePath
        mediaStore.failOnDelete += photo.relativePath
        repo.addPhoto(photo)

        val result = repo.deleteWalk("w4")

        assertIs<WalkDeleteResult.SuccessWithMediaCleanupFailures>(result)
        assertTrue(result.failedPaths.contains(photo.relativePath))
        assertNull(repo.observeWalkById("w4").first())
    }
}

/**
 * Self-contained [LocalMediaStore] double for Android instrumented tests.
 * Avoids cross-source-set dependency on commonTest test doubles.
 */
private class AndroidTestLocalMediaStore : LocalMediaStore {
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