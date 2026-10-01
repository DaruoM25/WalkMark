package com.walkmark.app.domain.sync

sealed interface SyncFailure {
    data object NetworkUnavailable : SyncFailure
    data object Unauthenticated : SyncFailure
    data object RateLimited : SyncFailure
    data object ServiceUnavailable : SyncFailure
    data object Rejected : SyncFailure
    data object Unknown : SyncFailure
}
