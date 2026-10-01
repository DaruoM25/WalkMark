package com.walkmark.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walkmark.app.core.database.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(operation: SyncOperationEntity)

    @Query("SELECT COUNT(*) FROM sync_operations")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_operations ORDER BY createdAtEpochMs ASC, operationId ASC")
    suspend fun getPending(): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE walkId = :walkId ORDER BY createdAtEpochMs ASC, operationId ASC")
    suspend fun getPendingForWalk(walkId: String): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE entityType = :entityType AND entityId = :entityId LIMIT 1")
    suspend fun findByEntity(entityType: String, entityId: String): SyncOperationEntity?

    @Query("UPDATE sync_operations SET attemptCount = attemptCount + 1, lastAttemptEpochMs = :attemptEpochMs WHERE operationId = :operationId")
    suspend fun markAttempt(operationId: String, attemptEpochMs: Long)

    @Query("DELETE FROM sync_operations WHERE operationId = :operationId")
    suspend fun deleteByOperationId(operationId: String)

    @Query("DELETE FROM sync_operations WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteForEntity(entityType: String, entityId: String)

    @Query("DELETE FROM sync_operations")
    suspend fun clear()
}
