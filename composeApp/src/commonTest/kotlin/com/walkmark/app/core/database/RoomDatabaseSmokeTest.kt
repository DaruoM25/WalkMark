package com.walkmark.app.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.walkmark.app.core.database.entity.SampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomDatabaseSmokeTest {

    @Test
    fun testRoomDatabaseLifecycleAndDaoOperations() = runTest {
        // 1. CREATE / OPEN IN-MEMORY TEST DATABASE
        val db = Room.inMemoryDatabaseBuilder<WalkMarkDatabase>()
           .setDriver(BundledSQLiteDriver())
           .setQueryCoroutineContext(Dispatchers.Unconfined)
           .build()

        val dao = db.sampleDao()

        // 2. VERIFY INITIAL EMPTY STATE
        assertEquals(0, dao.count())
        assertEquals(emptyList(), dao.observeAll().first())

        // 3. DAO INSERT
        val entity = SampleEntity(title = "Test Trail", createdAt = 1000L)
        val insertedId = dao.insert(entity)
        assertEquals(1L, insertedId)

        // 4. DAO QUERY / COUNT
        assertEquals(1, dao.count())
        val items = dao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals("Test Trail", items.first().title)

        // 5. DAO DELETE / CLEAR
        dao.clear()
        assertEquals(0, dao.count())
        assertEquals(emptyList(), dao.observeAll().first())

        // 6. CLOSE DATABASE
        db.close()
    }
}
