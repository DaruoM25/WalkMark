package com.walkmark.app.core.database

import com.walkmark.app.core.database.entity.SampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDatabaseSmokeTest {

    @Test
    fun testRoomDatabaseLifecycleAndDaoOperations() = runTest {
        // 1. CREATE / OPEN IN-MEMORY TEST DATABASE
        val db: WalkMarkDatabase = getInMemoryDatabaseBuilder()
           .setQueryCoroutineContext(Dispatchers.Default)
           .build()

        val dao = db.sampleDao()

        // 2. VERIFY INITIAL EMPTY STATE
        assertEquals(0, dao.count())
        assertEquals(emptyList<SampleEntity>(), dao.observeAll().first())

        // 3. DAO INSERT
        val entity = SampleEntity(title = "Test Trail", createdAt = 1000L)
        val insertedId = dao.insert(entity)
        assertEquals(1L, insertedId)

        // 4. DAO QUERY / COUNT
        assertEquals(1, dao.count())
        val items: List<SampleEntity> = dao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals("Test Trail", items.first().title)

        // 5. DAO DELETE / CLEAR
        dao.clear()
        assertEquals(0, dao.count())
        assertEquals(emptyList<SampleEntity>(), dao.observeAll().first())

        // 6. CLOSE DATABASE
        db.close()
    }
}
