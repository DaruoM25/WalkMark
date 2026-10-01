package com.walkmark.app.domain.sync

import kotlinx.coroutines.flow.Flow

interface SyncQueueRepository {
    fun observePendingCount(): Flow<Int>
    suspend fun enqueue(operation: SyncOperation)
    suspend fun getPending(): List<SyncOperation>
    suspend fun markAttempt(operationId: String, attemptEpochMs: Long)
    suspend fun remove(operationId: String)
    suspend fun clear()
}
