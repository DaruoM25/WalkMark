package com.walkmark.app.domain.sync

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data class Syncing(val pendingCount: Int) : SyncStatus
    data class Synced(val lastSyncEpochMs: Long) : SyncStatus
    data class Offline(val pendingCount: Int) : SyncStatus
    data class Failed(val pendingCount: Int, val failure: SyncFailure) : SyncStatus
}
