package com.walkmark.app.domain.sync

data class SyncOperation(
    val operationId: String,
    val entityType: SyncEntityType,
    val entityId: String,
    val walkId: String?,
    val operationType: SyncOperationType,
    val createdAtEpochMs: Long,
    val attemptCount: Int = 0,
    val lastAttemptEpochMs: Long? = null
)
