package com.walkmark.app.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.core.database.entity.SampleEntity
import com.walkmark.app.data.database.entity.WalkEntity
import com.walkmark.app.domain.model.Walk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class AndroidRoomInstrumentationTest {

    @Test
    fun testRealAndroidRoomDatabaseLifecycleAndDaoOperations() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // 1. CREATE / OPEN IN-MEMORY ROOM DATABASE ON REAL ANDROID RUNTIME
        val db = Room.inMemoryDatabaseBuilder(context, WalkMarkDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        val sampleDao = db.sampleDao()
        val walkDao = db.walkDao()

        // 2. VERIFY INITIAL EMPTY STATE
        assertEquals(0, sampleDao.count())
        assertEquals(emptyList(), sampleDao.observeAll().first())
        assertEquals(0, walkDao.count())
        assertEquals(emptyList(), walkDao.observeAll().first())

        // 3. DAO INSERT
        val sample = SampleEntity(title = "Android Trail", createdAt = 12345L)
        val sampleId = sampleDao.insert(sample)
        assertEquals(1L, sampleId)

        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val walk = Walk(
            id = "walk_real_android_001",
            title = "Pine Forest Trail",
            summary = "Real Android Room Instrumentation Walk",
            startTime = now,
            endTime = Instant.fromEpochMilliseconds(1727451600000L),
            totalDistanceMeters = 3500.0,
            durationSeconds = 2400L
        )
        val walkEntity = WalkEntity.fromDomain(walk)
        walkDao.insert(walkEntity)

        // 4. DAO QUERY / COUNT
        assertEquals(1, sampleDao.count())
        val samples = sampleDao.observeAll().first()
        assertEquals(1, samples.size)
        assertEquals("Android Trail", samples.first().title)

        assertEquals(1, walkDao.count())
        val fetchedWalk = walkDao.getById("walk_real_android_001")
        assertNotNull(fetchedWalk)
        assertEquals("Pine Forest Trail", fetchedWalk.title)
        assertEquals(walk, fetchedWalk.toDomain())

        // 5. DAO DELETE / CLEAR
        sampleDao.clear()
        assertEquals(0, sampleDao.count())
        assertEquals(emptyList(), sampleDao.observeAll().first())

        walkDao.deleteById("walk_real_android_001")
        assertEquals(0, walkDao.count())
        assertNull(walkDao.getById("walk_real_android_001"))

        // 6. CLOSE DATABASE
        db.close()
    }
}