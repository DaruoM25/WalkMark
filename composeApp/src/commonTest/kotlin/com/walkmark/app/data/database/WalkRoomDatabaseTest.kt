package com.walkmark.app.data.database

import com.walkmark.app.core.database.WalkMarkDatabase
import com.walkmark.app.core.database.getInMemoryDatabaseBuilder
import com.walkmark.app.data.database.entity.WalkEntity
import com.walkmark.app.domain.model.Walk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalkRoomDatabaseTest {

    @Test
    fun testWalkEntityDaoCrudOperations() = runTest {
        val db: WalkMarkDatabase = getInMemoryDatabaseBuilder()
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()

        val dao = db.walkDao()

        // 1. Initial count is 0
        assertEquals(0, dao.count())
        assertEquals(emptyList(), dao.observeAll().first())

        // 2. Insert Walk
        val now = Instant.fromEpochMilliseconds(1727448000000L)
        val walk = Walk(
            id = "walk_db_001",
            title = "Forest Loop",
            summary = "Autumn forest walk",
            startTime = now,
            endTime = Instant.fromEpochMilliseconds(1727451600000L),
            totalDistanceMeters = 4200.0,
            durationSeconds = 3600L
        )
        val entity = WalkEntity.fromDomain(walk)
        dao.insert(entity)

        // 3. Verify count and retrieval
        assertEquals(1, dao.count())
        val fetched = dao.getById("walk_db_001")
        assertNotNull(fetched)
        assertEquals("Forest Loop", fetched.title)
        assertEquals(walk, fetched.toDomain())

        val list = dao.observeAll().first()
        assertEquals(1, list.size)
        assertEquals("walk_db_001", list.first().id)

        // 4. Delete by ID
        dao.deleteById("walk_db_001")
        assertEquals(0, dao.count())
        assertNull(dao.getById("walk_db_001"))

        db.close()
    }
}
