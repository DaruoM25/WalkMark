package com.walkmark.app.data.sync

import com.walkmark.app.core.database.dao.SyncOperationDao
import com.walkmark.app.core.database.entity.SyncOperationEntity
import com.walkmark.app.domain.sync.SyncEntityType
import com.walkmark.app.domain.sync.SyncOperation
import com.walkmark.app.domain.sync.SyncOperationType
import com.walkmark.app.domain.sync.SyncQueueRepository
import kotlinx.coroutines.flow.Flow

class RoomSyncQueueRepository(
    private val dao: SyncOperationDao
) : SyncQueueRepository {

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override suspend fun enqueue(operation: SyncOperation) {
        val existing = dao.findByEntity(operation.entityType.name, operation.entityId)
        if (existing != null) {
            dao.deleteByOperationId(existing.operationId)
        }
        val entity = SyncOperationEntity(
            operationId = operation.operationId,
            entityType = operation.entityType.name,
            entityId = operation.entityId,
            walkId = operation.walkId,
            operationType = operation.operationType.name,
            createdAtEpochMs = operation.createdAtEpochMs,
            attemptCount = operation.attemptCount,
            lastAttemptEpochMs = operation.lastAttemptEpochMs
        )
        dao.insertOrReplace(entity)
    }

    override suspend fun getPending(): List<SyncOperation> {
        return dao.getPending().map { entity ->
            SyncOperation(
                operationId = entity.operationId,
                entityType = SyncEntityType.valueOf(entity.entityType),
                entityId = entity.entityId,
                walkId = entity.walkId,
                operationType = SyncOperationType.valueOf(entity.operationType),
                createdAtEpochMs = entity.createdAtEpochMs,
                attemptCount = entity.attemptCount,
                lastAttemptEpochMs = entity.lastAttemptEpochMs
            )
        }
    }

    override suspend fun markAttempt(operationId: String, attemptEpochMs: Long) {
        dao.markAttempt(operationId, attemptEpochMs)
    }

    override suspend fun remove(operationId: String) {
        dao.deleteByOperationId(operationId)
    }

    override suspend fun clear() {
        dao.clear()
    }
}
