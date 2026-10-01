package com.walkmark.app.core.database

import com.walkmark.app.core.database.entity.SyncOperationEntity
import com.walkmark.app.data.sync.RoomSyncQueueRepository
import com.walkmark.app.domain.sync.SyncEntityType
import com.walkmark.app.domain.sync.SyncOperation
import com.walkmark.app.domain.sync.SyncOperationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncOperationDaoTest {

    private class FakeSyncOperationDao : com.walkmark.app.core.database.dao.SyncOperationDao {
        private val storage = LinkedHashMap<String, SyncOperationEntity>()
        private val countFlow = kotlinx.coroutines.flow.MutableStateFlow(0)

        override suspend fun insertOrReplace(operation: SyncOperationEntity) {
            storage[operation.operationId] = operation
            countFlow.value = storage.size
        }

        override fun observePendingCount(): kotlinx.coroutines.flow.Flow<Int> = countFlow

        override suspend fun getPending(): List<SyncOperationEntity> {
            return storage.values.sortedWith(
                compareBy<SyncOperationEntity> { it.createdAtEpochMs }.thenBy { it.operationId }
            )
        }

        override suspend fun getPendingForWalk(walkId: String): List<SyncOperationEntity> {
            return storage.values.filter { it.walkId == walkId }.sortedWith(
                compareBy<SyncOperationEntity> { it.createdAtEpochMs }.thenBy { it.operationId }
            )
        }

        override suspend fun findByEntity(entityType: String, entityId: String): SyncOperationEntity? {
            return storage.values.firstOrNull { it.entityType == entityType && it.entityId == entityId }
        }

        override suspend fun markAttempt(operationId: String, attemptEpochMs: Long) {
            storage[operationId]?.let {
                storage[operationId] = it.copy(
                    attemptCount = it.attemptCount + 1,
                    lastAttemptEpochMs = attemptEpochMs
                )
            }
        }

        override suspend fun deleteByOperationId(operationId: String) {
            storage.remove(operationId)
            countFlow.value = storage.size
        }

        override suspend fun deleteForEntity(entityType: String, entityId: String) {
            storage.entries.removeIf { it.value.entityType == entityType && it.value.entityId == entityId }
            countFlow.value = storage.size
        }

        override suspend fun clear() {
            storage.clear()
            countFlow.value = 0
        }
    }

    @Test
    fun enqueueAndRetrievePendingOperationsPreservesOrder() = runTest {
        val dao = FakeSyncOperationDao()
        val repository = RoomSyncQueueRepository(dao)

        val op1 = SyncOperation("op-2", SyncEntityType.WALK, "walk-1", "walk-1", SyncOperationType.UPSERT, 2000L)
        val op2 = SyncOperation("op-1", SyncEntityType.NOTE, "note-1", "walk-1", SyncOperationType.UPSERT, 1000L)

        repository.enqueue(op1)
        repository.enqueue(op2)

        val pending = repository.getPending()
        assertEquals(2, pending.size)
        assertEquals("op-1", pending[0].operationId)
        assertEquals("op-2", pending[1].operationId)
    }

    @Test
    fun entityLevelCompactionReplacesOlderOperationForSameEntity() = runTest {
        val dao = FakeSyncOperationDao()
        val repository = RoomSyncQueueRepository(dao)

        val opUpsert = SyncOperation("op-1", SyncEntityType.WALK, "walk-1", "walk-1", SyncOperationType.UPSERT, 1000L)
        val opDelete = SyncOperation("op-2", SyncEntityType.WALK, "walk-1", "walk-1", SyncOperationType.DELETE, 2000L)

        repository.enqueue(opUpsert)
        repository.enqueue(opDelete)

        val pending = repository.getPending()
        assertEquals(1, pending.size)
        assertEquals("op-2", pending[0].operationId)
        assertEquals(SyncOperationType.DELETE, pending[0].operationType)
    }

    @Test
    fun markAttemptUpdatesCountAndTimestamp() = runTest {
        val dao = FakeSyncOperationDao()
        val repository = RoomSyncQueueRepository(dao)

        val op = SyncOperation("op-1", SyncEntityType.WALK, "walk-1", "walk-1", SyncOperationType.UPSERT, 1000L)
        repository.enqueue(op)

        repository.markAttempt("op-1", 1500L)

        val pending = repository.getPending().first()
        assertEquals(1, pending.attemptCount)
        assertEquals(1500L, pending.lastAttemptEpochMs)
    }

    @Test
    fun removeDeletesFromQueue() = runTest {
        val dao = FakeSyncOperationDao()
        val repository = RoomSyncQueueRepository(dao)

        val op = SyncOperation("op-1", SyncEntityType.WALK, "walk-1", "walk-1", SyncOperationType.UPSERT, 1000L)
        repository.enqueue(op)
        assertEquals(1, repository.observePendingCount().first())

        repository.remove("op-1")
        assertEquals(0, repository.observePendingCount().first())
    }
}
