package com.walkmark.app.data.walk

import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryWalkRepositoryTest {

    private val mediaStore = FakeLocalMediaStore()
    private val repo = InMemoryWalkRepository(mediaStore = mediaStore)

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun photo(id: String, walkId: String, path: String) = WalkPhoto(
        id = id,
        walkId = walkId,
        latitude = 0.0,
        longitude = 0.0,
        relativePath = path,
        mimeType = "image/jpeg",
        byteSize = 512L,
        createdAtEpochMs = 1_000L
    )

    // ── observeWalkById ──────────────────────────────────────────────────────

    @Test
    fun observeWalkById_returnsNullForUnknownId() = runTest {
        assertNull(repo.observeWalkById("missing").first())
    }

    @Test
    fun observeWalkById_emitsWalkAfterStart() = runTest {
        repo.startWalk("w1", "Morning Walk", 1_000L)
        val walk = repo.observeWalkById("w1").first()
        assertEquals("w1", walk?.id)
        assertEquals(WalkStatus.ACTIVE, walk?.status)
    }

    @Test
    fun observeWalkById_emitsNullAfterDelete() = runTest {
        repo.startWalk("w2", "Evening Walk", 2_000L)
        repo.deleteWalk("w2")
        assertNull(repo.observeWalkById("w2").first())
    }

    // ── deleteWalk — Success ──────────────────────────────────────────────────

    @Test
    fun deleteWalk_returnsSuccessAndCleansUpMedia() = runTest {
        repo.startWalk("w3", "Park Walk", 3_000L)
        val p1 = photo("p1", "w3", "walks/w3/img1.jpg")
        val p2 = photo("p2", "w3", "walks/w3/img2.jpg")
        mediaStore.storedPaths += p1.relativePath
        mediaStore.storedPaths += p2.relativePath
        repo.addPhoto(p1)
        repo.addPhoto(p2)

        val result = repo.deleteWalk("w3")

        assertIs<WalkDeleteResult.Success>(result)
        assertTrue(mediaStore.deletedPaths.containsAll(listOf(p1.relativePath, p2.relativePath)))
        assertNull(repo.observeWalkById("w3").first())
    }

    // ── deleteWalk — SuccessWithMediaCleanupFailures ──────────────────────────

    @Test
    fun deleteWalk_returnsPartialFailureWhenMediaDeleteFails() = runTest {
        repo.startWalk("w4", "Hill Walk", 4_000L)
        val p1 = photo("p1", "w4", "walks/w4/img1.jpg")
        val p2 = photo("p2", "w4", "walks/w4/img2.jpg")
        mediaStore.storedPaths += p1.relativePath
        mediaStore.storedPaths += p2.relativePath
        mediaStore.failOnDelete += p2.relativePath   // p2 will fail to delete
        repo.addPhoto(p1)
        repo.addPhoto(p2)

        val result = repo.deleteWalk("w4")

        assertIs<WalkDeleteResult.SuccessWithMediaCleanupFailures>(result)
        assertEquals(listOf(p2.relativePath), result.failedPaths)
        assertTrue(mediaStore.deletedPaths.contains(p1.relativePath))
        // Walk DB record still removed
        assertNull(repo.observeWalkById("w4").first())
    }

    // ── addPhoto ─────────────────────────────────────────────────────────────

    @Test
    fun addPhoto_appearsInObservePhotos() = runTest {
        repo.startWalk("w5", "River Walk", 5_000L)
        val p = photo("p1", "w5", "walks/w5/img1.jpg")
        repo.addPhoto(p)
        val photoList = repo.observePhotos("w5").first()
        assertEquals(1, photoList.size)
        assertEquals(p.id, photoList.first().id)
    }
}