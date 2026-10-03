package com.walkmark.app.data.walk

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.core.database.WalkMarkMigrations
import com.walkmark.app.core.database.getInMemoryDatabaseBuilder
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkPersistenceIntegrationTest {

    private lateinit var database: WalkMarkDatabase
    private lateinit var repository: RoomWalkRepository

    @BeforeTest
    fun setUp() {
        database = getInMemoryDatabaseBuilder().build()
        repository = RoomWalkRepository(database)
    }

    @AfterTest
    fun tearDown() {
        if (this::database.isInitialized) database.close()
    }

    private fun point(latitude: Double, longitude: Double, timestamp: Long) = LocationPoint(
        latitude = latitude,
        longitude = longitude,
        altitude = 35.0,
        timestamp = timestamp,
        accuracy = 4f
    )

    @Test
    fun startWalkPersistsActiveWalk() = runTest {
        val walk = repository.startWalk("walk-1", "Morning loop", 1_700_000_000_000L)

        assertEquals("walk-1", walk.id)
        assertEquals(WalkStatus.ACTIVE, walk.status)
        assertNull(walk.endTimeEpochMs)
        assertEquals(0.0, walk.totalDistanceMeters)

        val stored = repository.getWalk("walk-1")
        assertNotNull(stored)
        assertEquals(WalkStatus.ACTIVE, stored.status)
        assertEquals("Morning loop", stored.title)
    }

    @Test
    fun appendPointsAssignsSequentialIndexesInOrder() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.appendPoints(
            "walk-1",
            listOf(
                point(48.0, 2.0, 10L),
                point(48.1, 2.1, 20L),
                point(48.2, 2.2, 30L)
            )
        )

        val stored = database.walkPointDao().getByWalk("walk-1")

        assertEquals(3, stored.size)
        assertEquals(listOf(0, 1, 2), stored.map { it.seq })
        assertEquals(48.0, stored[0].latitude)
        assertEquals(48.1, stored[1].latitude)
        assertEquals(48.2, stored[2].latitude)
        assertEquals(10L, stored[0].timestampEpochMs)
        assertEquals(30L, stored[2].timestampEpochMs)
    }

    @Test
    fun appendPointsContinuesSequencingAcrossBatches() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.appendPoints("walk-1", listOf(point(48.0, 2.0, 10L), point(48.1, 2.1, 20L)))
        repository.appendPoints("walk-1", listOf(point(48.2, 2.2, 30L)))

        val stored = database.walkPointDao().getByWalk("walk-1")

        assertEquals(3, stored.size)
        assertEquals(listOf(0, 1, 2), stored.map { it.seq })
        assertEquals(48.2, stored[2].latitude)
    }

    @Test
    fun appendPointsWithEmptyListIsANoOp() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.appendPoints("walk-1", emptyList())

        assertEquals(0, database.walkPointDao().countForWalk("walk-1"))
    }

    @Test
    fun completeWalkRecordsEndTimeAndDuration() = runTest {
        repository.startWalk("walk-1", "Walk", 1_000_000L)

        val completed = repository.completeWalk(
            walkId = "walk-1",
            endTimeEpochMs = 3_661_000L,
            totalDistanceMeters = 4321.5
        )

        assertNotNull(completed)
        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(3_661_000L, completed.endTimeEpochMs)
        assertEquals(2661L, completed.durationSeconds)
        assertEquals(4321.5, completed.totalDistanceMeters)
    }

    @Test
    fun completeWalkNeverProducesNegativeDuration() = runTest {
        repository.startWalk("walk-1", "Walk", 5_000L)

        val completed = repository.completeWalk("walk-1", 1_000L, 0.0)

        assertNotNull(completed)
        assertEquals(0L, completed.durationSeconds)
    }

    @Test
    fun completeWalkForUnknownIdReturnsNull() = runTest {
        assertNull(repository.completeWalk("missing", 1L, 0.0))
    }

    @Test
    fun noteRoundTripsWithCoordinate() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.addNote(
            WalkNote("note-1", "walk-1", "Bench near fountain", 48.8584, 2.2945, 1_700L)
        )

        val notes = database.walkNoteDao().getByWalk("walk-1")

        assertEquals(1, notes.size)
        assertEquals("Bench near fountain", notes[0].text)
        assertEquals(48.8584, notes[0].latitude)
        assertEquals(2.2945, notes[0].longitude)
        assertEquals("walk-1", notes[0].walkId)
    }

    @Test
    fun photoRoundTripsWithRelativePathAndMetadata() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.addPhoto(
            WalkPhoto(
                id = "photo-1",
                walkId = "walk-1",
                latitude = 48.86,
                longitude = 2.30,
                relativePath = "walk-1/abc-123.jpg",
                mimeType = "image/jpeg",
                byteSize = 2048L,
                createdAtEpochMs = 1_800L
            )
        )

        val photos = database.walkPhotoDao().getByWalk("walk-1")

        assertEquals(1, photos.size)
        assertEquals("walk-1/abc-123.jpg", photos[0].relativePath)
        assertEquals("image/jpeg", photos[0].mimeType)
        assertEquals(2048L, photos[0].byteSize)
    }

    @Test
    fun childRowsSurviveDatabaseReopen() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val databaseName = "walkmark-reopen-test-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)

        var reopened: WalkMarkDatabase? = null
        try {
            val firstPass = fileBackedDatabase(context, databaseName)
            val firstRepository = RoomWalkRepository(firstPass)
            firstRepository.startWalk("walk-1", "Walk", 1L)
            firstRepository.appendPoints(
                "walk-1",
                listOf(point(48.0, 2.0, 10L), point(48.1, 2.1, 20L))
            )
            firstRepository.addNote(WalkNote("n1", "walk-1", "note text", 48.0, 2.0, 1L))
            firstRepository.addPhoto(
                WalkPhoto("p1", "walk-1", 48.0, 2.0, "walk-1/a.jpg", "image/jpeg", 10L, 1L)
            )
            firstPass.close()

            val secondPass = fileBackedDatabase(context, databaseName)
            reopened = secondPass
            val secondRepository = RoomWalkRepository(secondPass)

            val walk = secondRepository.getWalk("walk-1")
            assertNotNull(walk, "walk row must survive a database reopen")
            assertEquals("walk-1", walk.id)
            assertEquals("Walk", walk.title)

            val points = secondPass.walkPointDao().getByWalk("walk-1")
            assertEquals(2, points.size)
            assertEquals(listOf(0, 1), points.map { it.seq })
            assertEquals(48.1, points[1].latitude)

            val notes = secondPass.walkNoteDao().getByWalk("walk-1")
            assertEquals("note text", notes.single().text)
            assertEquals(48.0, notes.single().latitude)

            val photos = secondPass.walkPhotoDao().getByWalk("walk-1")
            assertEquals("walk-1/a.jpg", photos.single().relativePath)
            assertEquals("image/jpeg", photos.single().mimeType)
            assertEquals(10L, photos.single().byteSize)
        } finally {
            reopened?.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun fileBackedDatabase(
        context: android.content.Context,
        name: String
    ): WalkMarkDatabase {
        context.getDatabasePath(name).parentFile?.mkdirs()
        return Room.databaseBuilder(context, WalkMarkDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(WalkMarkMigrations.MIGRATION_1_2)
            .build()
    }

    @Test
    fun deletingWalkCascadesToPointsNotesAndPhotos() = runTest {
        repository.startWalk("walk-1", "Walk", 1L)
        repository.appendPoints("walk-1", listOf(point(48.0, 2.0, 10L)))
        repository.addNote(WalkNote("n1", "walk-1", "note", 48.0, 2.0, 1L))
        repository.addPhoto(WalkPhoto("p1", "walk-1", 48.0, 2.0, "walk-1/a.jpg", "image/jpeg", 10L, 1L))

        repository.deleteWalk("walk-1")

        assertNull(repository.getWalk("walk-1"))
        assertEquals(0, database.walkPointDao().countForWalk("walk-1"))
        assertEquals(0, database.walkNoteDao().countForWalk("walk-1"))
        assertEquals(0, database.walkPhotoDao().countForWalk("walk-1"))
    }

    @Test
    fun activeWalkIsDiscoverableWhileCompletedWalkIsNot() = runTest {
        repository.startWalk("walk-1", "First", 1L)
        repository.completeWalk("walk-1", 100L, 10.0)
        repository.startWalk("walk-2", "Second", 200L)

        val active = repository.getActiveWalk()

        assertNotNull(active)
        assertEquals("walk-2", active.id)
        assertEquals(WalkStatus.ACTIVE, active.status)
    }

    @Test
    fun observeAllWalksReturnsNewestFirst() = runTest {
        repository.startWalk("walk-1", "First", 1_000L)
        repository.startWalk("walk-2", "Second", 2_000L)

        val walks = repository.observeAllWalks().first()

        assertEquals(listOf("walk-2", "walk-1"), walks.map { it.id })
    }

    @Test
    fun sampleScaffoldingTableIsStillUsableAtVersion2() = runTest {
        database.sampleDao().insert(
            com.walkmark.app.core.database.entity.SampleEntity(title = "Forest Trail", createdAt = 2000L)
        )

        assertEquals(1, database.sampleDao().count())
    }
}
