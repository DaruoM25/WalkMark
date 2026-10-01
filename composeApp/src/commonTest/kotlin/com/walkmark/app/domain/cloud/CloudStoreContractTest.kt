package com.walkmark.app.domain.cloud

import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CloudStoreContractTest {

    private val ownerA = CloudOwnerId("owner-a")
    private val ownerB = CloudOwnerId("owner-b")

    private fun walk(localId: String, status: WalkStatus = WalkStatus.COMPLETED) = CloudWalk(
        localId = localId,
        cloudId = null,
        title = "Morning walk",
        summary = "Riverside loop",
        startTimeEpochMs = 1_700_000_000_000L,
        endTimeEpochMs = 1_700_003_600_000L,
        totalDistanceMeters = 4_210.5,
        durationSeconds = 3_600L,
        status = status
    )

    private fun note(localId: String, localWalkId: String) = CloudWalkNote(
        localId = localId,
        localWalkId = localWalkId,
        text = "Bench by the water",
        latitude = 48.8584,
        longitude = 2.2945,
        createdAtEpochMs = 1_700_000_500_000L
    )

    private fun photo(localId: String, localWalkId: String) = CloudPhotoMetadata(
        localId = localId,
        localWalkId = localWalkId,
        latitude = 48.8584,
        longitude = 2.2945,
        mimeType = "image/jpeg",
        byteSize = 204_800L,
        createdAtEpochMs = 1_700_000_900_000L
    )

    private fun point(localWalkId: String, seq: Int) = CloudRoutePoint(
        localWalkId = localWalkId,
        seq = seq,
        latitude = 48.8584 + seq,
        longitude = 2.2945 + seq,
        altitude = if (seq == 1) null else 35.0 + seq,
        timestampEpochMs = 1_700_000_000_000L + seq * 1_000L,
        accuracyMeters = 4.5f + seq
    )

    @Test
    fun ownerIsolationKeepsWalksInvisibleToAnotherAccount() = runTest {
        val store = FakeCloudWalkStore()

        store.upsert(ownerA, walk("walk-1"))

        assertNull(store.get(ownerB, "walk-1").valueOrNull())
        assertTrue(store.listByOwner(ownerB).valueOrNull().orEmpty().isEmpty())
        assertEquals(1, store.listByOwner(ownerA).valueOrNull().orEmpty().size)
    }

    @Test
    fun ownerIsolationPreventsCrossAccountDelete() = runTest {
        val store = FakeCloudWalkStore()
        store.upsert(ownerA, walk("walk-1"))

        store.delete(ownerB, "walk-1")

        assertEquals(1, store.listByOwner(ownerA).valueOrNull().orEmpty().size)
        store.delete(ownerA, "walk-1")
        assertNull(store.get(ownerA, "walk-1").valueOrNull())
    }

    @Test
    fun ownerIsolationAppliesToNotesAndPhotoMetadata() = runTest {
        val notes = FakeCloudNoteStore()
        val photos = FakeCloudPhotoMetadataStore()

        notes.upsert(ownerA, note("note-1", "walk-1"))
        photos.upsertAll(ownerA, listOf(photo("photo-1", "walk-1")))

        assertTrue(notes.getByWalk(ownerB, "walk-1").valueOrNull().orEmpty().isEmpty())
        assertTrue(photos.getByWalk(ownerB, "walk-1").valueOrNull().orEmpty().isEmpty())
        assertEquals(1, notes.getByWalk(ownerA, "walk-1").valueOrNull().orEmpty().size)
        assertEquals(1, photos.getByWalk(ownerA, "walk-1").valueOrNull().orEmpty().size)
    }

    @Test
    fun routePointsAreReturnedInPersistedSeqOrder() = runTest {
        val store = FakeCloudRouteStore()
        val outOfOrder = listOf(point("walk-1", 2), point("walk-1", 0), point("walk-1", 1))

        val outcome = runCatching { store.upsertPoints(ownerA, outOfOrder) }

        assertTrue(outcome.isSuccess, "bulk upsert must not throw")
        assertEquals(3, store.upsertPoints(ownerA, emptyList()).valueOrNull())

        val restored = store.getByWalk(ownerA, "walk-1").valueOrNull().orEmpty()
        assertEquals(listOf(0, 1, 2), restored.map { it.seq })
        assertEquals(listOf("walk-1", "walk-1", "walk-1"), restored.map { it.localWalkId })
    }

    @Test
    fun routePointsPreservePayloadFieldsIncludingNullableAltitude() = runTest {
        val store = FakeCloudRouteStore()
        val source = listOf(point("walk-1", 0), point("walk-1", 1), point("walk-1", 2))

        assertEquals(3, store.upsertPoints(ownerA, source).valueOrNull())

        val restored = store.getByWalk(ownerA, "walk-1").valueOrNull().orEmpty()
        assertEquals(source, restored)
        assertEquals(35.0, restored[0].altitude)
        assertNull(restored[1].altitude)
        assertEquals(35.0 + 2, restored[2].altitude)
    }

    @Test
    fun walkMappingRoundTripsEveryFieldAndKeepsLocalIdAuthoritative() = runTest {
        val store = FakeCloudWalkStore()
        val source = walk("walk-1", status = WalkStatus.ACTIVE)

        val stored = store.upsert(ownerA, source).valueOrNull()

        assertEquals(source, stored?.copy(cloudId = null))
        assertEquals("walk-1", stored?.localId)
        assertEquals(WalkStatus.ACTIVE, stored?.status)
        assertEquals(source.copy(cloudId = CloudEntityId("remote-1")), stored)
    }

    @Test
    fun noteMappingRoundTripsEveryField() = runTest {
        val store = FakeCloudNoteStore()
        val source = note("note-1", "walk-1")

        assertEquals(source, store.upsert(ownerA, source).valueOrNull())
        assertEquals(listOf(source), store.getByWalk(ownerA, "walk-1").valueOrNull())
    }

    @Test
    fun photoMetadataMappingRoundTripsWithoutAnyPathValue() = runTest {
        val store = FakeCloudPhotoMetadataStore()
        val source = photo("photo-1", "walk-1")

        assertEquals(1, store.upsertAll(ownerA, listOf(source)).valueOrNull())
        assertEquals(listOf(source), store.getByWalk(ownerA, "walk-1").valueOrNull())
        assertEquals("image/jpeg", source.mimeType)
        assertEquals(204_800L, source.byteSize)
        assertEquals(48.8584, source.latitude)
        assertEquals(2.2945, source.longitude)
    }

    @Test
    fun everyCloudFailureCategoryIsASingletonObject() {
        assertSame(CloudFailure.NotAuthenticated, CloudFailure.NotAuthenticated)
        assertSame(CloudFailure.NetworkUnavailable, CloudFailure.NetworkUnavailable)
        assertSame(CloudFailure.ServiceUnavailable, CloudFailure.ServiceUnavailable)
        assertSame(CloudFailure.Rejected, CloudFailure.Rejected)
        assertSame(CloudFailure.Unknown, CloudFailure.Unknown)
    }

    @Test
    fun rateLimitedIsTheOnlyFailureCarryingAPayloadAndItIsATimestamp() {
        assertEquals(CloudFailure.RateLimited(null), CloudFailure.RateLimited())
        assertEquals(CloudFailure.RateLimited(1_000L), CloudFailure.RateLimited(1_000L))
        assertNotEquals<CloudFailure>(CloudFailure.RateLimited(null), CloudFailure.RateLimited(1_000L))
        assertNull(CloudFailure.RateLimited().retryAfterEpochMs)
        assertEquals(1_000L, CloudFailure.RateLimited(1_000L).retryAfterEpochMs)
    }

    @Test
    fun cloudFailureExposesExactlySixCategories() {
        val categories: List<CloudFailure> = listOf(
            CloudFailure.NotAuthenticated,
            CloudFailure.NetworkUnavailable,
            CloudFailure.RateLimited(),
            CloudFailure.ServiceUnavailable,
            CloudFailure.Rejected,
            CloudFailure.Unknown
        )

        assertEquals(6, categories.size)
        categories.forEach { failure ->
            assertEquals(failure, CloudResult.Failure<Unit>(failure).failure)
        }
    }

    @Test
    fun remoteFailuresAreReturnedAsValuesAndNeverThrown() = runTest {
        val categories: List<CloudFailure> = listOf(
            CloudFailure.NotAuthenticated,
            CloudFailure.NetworkUnavailable,
            CloudFailure.RateLimited(),
            CloudFailure.ServiceUnavailable,
            CloudFailure.Rejected,
            CloudFailure.Unknown
        )

        categories.forEach { failure ->
            val outcome = runCatching {
                val walks = FakeCloudWalkStore(failure)
                val routes = FakeCloudRouteStore(failure)
                val notes = FakeCloudNoteStore(failure)
                val photos = FakeCloudPhotoMetadataStore(failure)
                val walkRow = walk("walk-1")
                val noteRow = note("note-1", "walk-1")
                val photoRow = photo("photo-1", "walk-1")
                val pointRow = point("walk-1", 0)

                assertEquals(failure, walks.upsert(ownerA, walkRow).failureOrNull())
                assertEquals(failure, walks.get(ownerA, "walk-1").failureOrNull())
                assertEquals(failure, walks.listByOwner(ownerA).failureOrNull())
                assertEquals(failure, walks.delete(ownerA, "walk-1").failureOrNull())
                assertEquals(failure, routes.upsertPoints(ownerA, listOf(pointRow)).failureOrNull())
                assertEquals(failure, routes.getByWalk(ownerA, "walk-1").failureOrNull())
                assertEquals(failure, notes.upsert(ownerA, noteRow).failureOrNull())
                assertEquals(failure, notes.getByWalk(ownerA, "walk-1").failureOrNull())
                assertEquals(failure, notes.delete(ownerA, "note-1").failureOrNull())
                assertEquals(failure, photos.upsertAll(ownerA, listOf(photoRow)).failureOrNull())
                assertEquals(failure, photos.getByWalk(ownerA, "walk-1").failureOrNull())
                assertEquals(failure, photos.deleteAllForWalk(ownerA, "walk-1").failureOrNull())
            }
            assertTrue(outcome.isSuccess, "port must surface the failure as a value, never as a thrown error")
        }
    }
}

private fun <T> CloudResult<T>.valueOrNull(): T? = when (this) {
    is CloudResult.Success -> value
    is CloudResult.Failure -> null
}

private fun <T> CloudResult<T>.failureOrNull(): CloudFailure? = when (this) {
    is CloudResult.Success -> null
    is CloudResult.Failure -> failure
}

private class FakeCloudWalkStore(private val failure: CloudFailure? = null) : CloudWalkStore {

    private val rows = mutableListOf<Pair<CloudOwnerId, CloudWalk>>()

    override suspend fun upsert(ownerId: CloudOwnerId, walk: CloudWalk): CloudResult<CloudWalk> {
        failure?.let { return CloudResult.Failure(it) }
        val stored = walk.copy(cloudId = walk.cloudId ?: CloudEntityId("remote-${rows.size + 1}"))
        rows.removeAll { it.first == ownerId && it.second.localId == stored.localId }
        rows.add(ownerId to stored)
        return CloudResult.Success(stored)
    }

    override suspend fun get(ownerId: CloudOwnerId, localWalkId: String): CloudResult<CloudWalk?> {
        failure?.let { return CloudResult.Failure(it) }
        return CloudResult.Success(
            rows.firstOrNull { it.first == ownerId && it.second.localId == localWalkId }?.second
        )
    }

    override suspend fun listByOwner(ownerId: CloudOwnerId): CloudResult<List<CloudWalk>> {
        failure?.let { return CloudResult.Failure(it) }
        return CloudResult.Success(rows.filter { it.first == ownerId }.map { it.second })
    }

    override suspend fun delete(ownerId: CloudOwnerId, localWalkId: String): CloudResult<Unit> {
        failure?.let { return CloudResult.Failure(it) }
        rows.removeAll { it.first == ownerId && it.second.localId == localWalkId }
        return CloudResult.Success(Unit)
    }
}

private class FakeCloudRouteStore(private val failure: CloudFailure? = null) : CloudRouteStore {

    private val rows = mutableListOf<Pair<CloudOwnerId, CloudRoutePoint>>()

    override suspend fun upsertPoints(
        ownerId: CloudOwnerId,
        points: List<CloudRoutePoint>
    ): CloudResult<Int> {
        failure?.let { return CloudResult.Failure(it) }
        points.forEach { point ->
            rows.removeAll { row ->
                row.first == ownerId && row.second.localWalkId == point.localWalkId && row.second.seq == point.seq
            }
            rows.add(ownerId to point)
        }
        return CloudResult.Success(points.size)
    }

    override suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudRoutePoint>> {
        failure?.let { return CloudResult.Failure(it) }
        return CloudResult.Success(
            rows.filter { it.first == ownerId && it.second.localWalkId == localWalkId }
                .map { it.second }
                .sortedBy { it.seq }
        )
    }
}

private class FakeCloudNoteStore(private val failure: CloudFailure? = null) : CloudNoteStore {

    private val rows = mutableListOf<Pair<CloudOwnerId, CloudWalkNote>>()

    override suspend fun upsert(ownerId: CloudOwnerId, note: CloudWalkNote): CloudResult<CloudWalkNote> {
        failure?.let { return CloudResult.Failure(it) }
        rows.removeAll { it.first == ownerId && it.second.localId == note.localId }
        rows.add(ownerId to note)
        return CloudResult.Success(note)
    }

    override suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudWalkNote>> {
        failure?.let { return CloudResult.Failure(it) }
        return CloudResult.Success(
            rows.filter { it.first == ownerId && it.second.localWalkId == localWalkId }.map { it.second }
        )
    }

    override suspend fun delete(ownerId: CloudOwnerId, localId: String): CloudResult<Unit> {
        failure?.let { return CloudResult.Failure(it) }
        rows.removeAll { it.first == ownerId && it.second.localId == localId }
        return CloudResult.Success(Unit)
    }
}

private class FakeCloudPhotoMetadataStore(private val failure: CloudFailure? = null) :
    CloudPhotoMetadataStore {

    private val rows = mutableListOf<Pair<CloudOwnerId, CloudPhotoMetadata>>()

    override suspend fun upsertAll(
        ownerId: CloudOwnerId,
        photos: List<CloudPhotoMetadata>
    ): CloudResult<Int> {
        failure?.let { return CloudResult.Failure(it) }
        photos.forEach { photo ->
            rows.removeAll { it.first == ownerId && it.second.localId == photo.localId }
            rows.add(ownerId to photo)
        }
        return CloudResult.Success(photos.size)
    }

    override suspend fun getByWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<List<CloudPhotoMetadata>> {
        failure?.let { return CloudResult.Failure(it) }
        return CloudResult.Success(
            rows.filter { it.first == ownerId && it.second.localWalkId == localWalkId }.map { it.second }
        )
    }

    override suspend fun deleteAllForWalk(
        ownerId: CloudOwnerId,
        localWalkId: String
    ): CloudResult<Unit> {
        failure?.let { return CloudResult.Failure(it) }
        rows.removeAll { it.first == ownerId && it.second.localWalkId == localWalkId }
        return CloudResult.Success(Unit)
    }
}