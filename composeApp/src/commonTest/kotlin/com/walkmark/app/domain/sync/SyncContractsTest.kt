package com.walkmark.app.domain.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncContractsTest {

    @Test
    fun syncOperationCreationPreservesAllFields() {
        val op = SyncOperation(
            operationId = "op-1",
            entityType = SyncEntityType.WALK,
            entityId = "walk-123",
            walkId = "walk-123",
            operationType = SyncOperationType.UPSERT,
            createdAtEpochMs = 1700000000000L,
            attemptCount = 0,
            lastAttemptEpochMs = null
        )

        assertEquals("op-1", op.operationId)
        assertEquals(SyncEntityType.WALK, op.entityType)
        assertEquals("walk-123", op.entityId)
        assertEquals("walk-123", op.walkId)
        assertEquals(SyncOperationType.UPSERT, op.operationType)
        assertEquals(1700000000000L, op.createdAtEpochMs)
        assertEquals(0, op.attemptCount)
        assertNull(op.lastAttemptEpochMs)
    }

    @Test
    fun syncOperationSupportsDeleteTypeWithoutWalkId() {
        val op = SyncOperation(
            operationId = "op-del-1",
            entityType = SyncEntityType.NOTE,
            entityId = "note-999",
            walkId = null,
            operationType = SyncOperationType.DELETE,
            createdAtEpochMs = 1700000050000L,
            attemptCount = 2,
            lastAttemptEpochMs = 1700000060000L
        )

        assertEquals("op-del-1", op.operationId)
        assertEquals(SyncEntityType.NOTE, op.entityType)
        assertEquals("note-999", op.entityId)
        assertNull(op.walkId)
        assertEquals(SyncOperationType.DELETE, op.operationType)
        assertEquals(2, op.attemptCount)
        assertEquals(1700000060000L, op.lastAttemptEpochMs)
    }

    @Test
    fun syncFailuresAreTypedWithoutProviderStrings() {
        val failures = listOf(
            SyncFailure.NetworkUnavailable,
            SyncFailure.Unauthenticated,
            SyncFailure.RateLimited,
            SyncFailure.ServiceUnavailable,
            SyncFailure.Rejected,
            SyncFailure.Unknown
        )

        assertEquals(6, failures.size)
        assertTrue(failures.contains(SyncFailure.NetworkUnavailable))
        assertTrue(failures.contains(SyncFailure.Unauthenticated))
    }

    @Test
    fun syncStatusModelsReflectExpectedLifecycleStates() {
        val idle: SyncStatus = SyncStatus.Idle
        val syncing: SyncStatus = SyncStatus.Syncing(pendingCount = 3)
        val synced: SyncStatus = SyncStatus.Synced(lastSyncEpochMs = 1700000000000L)
        val offline: SyncStatus = SyncStatus.Offline(pendingCount = 2)
        val failed: SyncStatus = SyncStatus.Failed(pendingCount = 2, failure = SyncFailure.NetworkUnavailable)

        assertIs<SyncStatus.Idle>(idle)
        assertIs<SyncStatus.Syncing>(syncing)
        assertEquals(3, syncing.pendingCount)
        assertIs<SyncStatus.Synced>(synced)
        assertEquals(1700000000000L, synced.lastSyncEpochMs)
        assertIs<SyncStatus.Offline>(offline)
        assertEquals(2, offline.pendingCount)
        assertIs<SyncStatus.Failed>(failed)
        assertEquals(SyncFailure.NetworkUnavailable, failed.failure)
    }

    @Test
    fun syncQueueRepositoryContractSupportsIdempotentOperations() = runTest {
        val inMemoryQueue = object : SyncQueueRepository {
            private val items = LinkedHashMap<String, SyncOperation>()
            private val countFlow = MutableStateFlow(0)

            override fun observePendingCount(): Flow<Int> = countFlow

            override suspend fun enqueue(operation: SyncOperation) {
                items[operation.operationId] = operation
                countFlow.value = items.size
            }

            override suspend fun getPending(): List<SyncOperation> = items.values.toList()

            override suspend fun markAttempt(operationId: String, attemptEpochMs: Long) {
                items[operationId]?.let {
                    items[operationId] = it.copy(
                        attemptCount = it.attemptCount + 1,
                        lastAttemptEpochMs = attemptEpochMs
                    )
                }
            }

            override suspend fun remove(operationId: String) {
                items.remove(operationId)
                countFlow.value = items.size
            }

            override suspend fun clear() {
                items.clear()
                countFlow.value = 0
            }
        }

        val op = SyncOperation(
            operationId = "op-idempotent-1",
            entityType = SyncEntityType.PHOTO_METADATA,
            entityId = "photo-123",
            walkId = "walk-1",
            operationType = SyncOperationType.UPSERT,
            createdAtEpochMs = 1700000000000L
        )

        inMemoryQueue.enqueue(op)
        inMemoryQueue.enqueue(op) // Idempotent repeat

        val pending = inMemoryQueue.getPending()
        assertEquals(1, pending.size)
        assertEquals("op-idempotent-1", pending.first().operationId)

        inMemoryQueue.markAttempt("op-idempotent-1", 1700000100000L)
        val updated = inMemoryQueue.getPending().first()
        assertEquals(1, updated.attemptCount)
        assertEquals(1700000100000L, updated.lastAttemptEpochMs)

        inMemoryQueue.remove("op-idempotent-1")
        assertTrue(inMemoryQueue.getPending().isEmpty())
    }
}
