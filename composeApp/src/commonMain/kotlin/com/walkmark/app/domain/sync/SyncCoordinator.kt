package com.walkmark.app.domain.sync

import kotlinx.coroutines.flow.Flow

sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class Failure(val failure: SyncFailure) : SyncResult
}

interface SyncCoordinator {
    val syncStatus: Flow<SyncStatus>
    suspend fun syncPending(): SyncResult
    suspend fun refreshRemote(): SyncResult
    suspend fun syncWalk(walkId: String): SyncResult
}
