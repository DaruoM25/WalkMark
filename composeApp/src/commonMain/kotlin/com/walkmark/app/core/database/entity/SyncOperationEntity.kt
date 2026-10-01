package com.walkmark.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["entityType", "entityId"], unique = true, name = "index_sync_operations_entity_unique"),
        Index(value = ["walkId"], name = "index_sync_operations_walkId"),
        Index(value = ["createdAtEpochMs", "operationId"], name = "index_sync_operations_order")
    ]
)
data class SyncOperationEntity(
    @PrimaryKey val operationId: String,
    val entityType: String,
    val entityId: String,
    val walkId: String?,
    val operationType: String,
    val createdAtEpochMs: Long,
    val attemptCount: Int = 0,
    val lastAttemptEpochMs: Long? = null
)
