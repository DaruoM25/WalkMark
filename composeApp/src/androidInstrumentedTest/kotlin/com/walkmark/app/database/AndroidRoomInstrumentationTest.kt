package com.walkmark.app.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.core.database.entity.SampleEntity
import com.walkmark.app.core.database.entity.WalkEntity
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Proves that the KMP Room database works on a real Android runtime:
 * the bundled driver links its native SQLite library, `@ConstructedBy` instantiation resolves,
 * and the DAOs perform insert/query/observe/count/delete against real SQLite.
 *
 * Scope notes:
 * - This test creates a fresh database at the current version, so no migration is registered and
 *   no migration behaviour is asserted here. The v1 -> v2 migration is proven by
 *   WalkMigration_1_2_Test on the JVM.
 * - Relational/cascade behaviour of the child tables is proven by WalkPersistenceIntegrationTest
 *   on the JVM, so it is intentionally not duplicated here.
 * - Entity <-> domain mapping is proven by WalkMappingTest, so assertions stay at entity level.
 */
@RunWith(AndroidJUnit4::class)
class AndroidRoomInstrumentationTest {

    @Test
    fun testRealAndroidRoomDatabaseLifecycleAndDaoOperations() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val db = Room.inMemoryDatabaseBuilder(context, WalkMarkDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        val sampleDao = db.sampleDao()
        val walkDao = db.walkDao()

        // initial empty state
        assertEquals(0, sampleDao.count())
        assertEquals(emptyList(), sampleDao.observeAll().first())
        assertEquals(0, walkDao.count())
        assertEquals(emptyList(), walkDao.observeAll().first())

        // insert
        val sample = SampleEntity(title = "Android Trail", createdAt = 12345L)
        val sampleId = sampleDao.insert(sample)
        assertEquals(1L, sampleId)

        val walk = WalkEntity(
            id = "walk_real_android_001",
            title = "Pine Forest Trail",
            summary = "Real Android Room Instrumentation Walk",
            startTimeEpochMs = 1727448000000L,
            endTimeEpochMs = 1727451600000L,
            totalDistanceMeters = 3500.0,
            durationSeconds = 2400L,
            status = WalkStatus.ACTIVE.name
        )
        walkDao.insert(walk)

        // query / count
        assertEquals(1, sampleDao.count())
        val samples = sampleDao.observeAll().first()
        assertEquals(1, samples.size)
        assertEquals("Android Trail", samples.first().title)
        assertEquals(12345L, samples.first().createdAt)

        assertEquals(1, walkDao.count())
        val fetchedWalk = walkDao.getById("walk_real_android_001")
        assertNotNull(fetchedWalk)
        assertEquals("Pine Forest Trail", fetchedWalk.title)
        assertEquals("Real Android Room Instrumentation Walk", fetchedWalk.summary)
        assertEquals(1727448000000L, fetchedWalk.startTimeEpochMs)
        assertEquals(1727451600000L, fetchedWalk.endTimeEpochMs)
        assertEquals(3500.0, fetchedWalk.totalDistanceMeters)
        assertEquals(2400L, fetchedWalk.durationSeconds)
        assertEquals(WalkStatus.ACTIVE.name, fetchedWalk.status)

        // delete
        sampleDao.clear()
        assertEquals(0, sampleDao.count())
        assertEquals(emptyList(), sampleDao.observeAll().first())

        walkDao.deleteById("walk_real_android_001")
        assertEquals(0, walkDao.count())
        assertNull(walkDao.getById("walk_real_android_001"))

        db.close()
    }
}
