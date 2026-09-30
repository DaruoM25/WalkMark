package com.walkmark.app.presentation.settings

import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.WalkDeleteResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface BulkDeleteState {
    data object Idle : BulkDeleteState
    data object InProgress : BulkDeleteState
    data class Success(val deletedCount: Int) : BulkDeleteState
    data class PartialSuccess(val deletedCount: Int, val mediaFailureCount: Int) : BulkDeleteState
    data class Error(val message: String) : BulkDeleteState
}

class SettingsViewModel(
    private val walkRepository: WalkRepository,
    private val scope: CoroutineScope
) {
    private val _bulkDeleteState = MutableStateFlow<BulkDeleteState>(BulkDeleteState.Idle)
    val bulkDeleteState: StateFlow<BulkDeleteState> = _bulkDeleteState.asStateFlow()

    fun resetBulkDeleteState() {
        _bulkDeleteState.value = BulkDeleteState.Idle
    }

    fun deleteAllWalks() {
        if (_bulkDeleteState.value is BulkDeleteState.InProgress) return
        _bulkDeleteState.value = BulkDeleteState.InProgress

        scope.launch {
            try {
                val walks = walkRepository.observeAllWalks().first()
                if (walks.isEmpty()) {
                    _bulkDeleteState.value = BulkDeleteState.Success(0)
                    return@launch
                }

                var deletedCount = 0
                var mediaFailureCount = 0

                for (walk in walks) {
                    when (val result = walkRepository.deleteWalk(walk.id)) {
                        is WalkDeleteResult.Success -> {
                            deletedCount++
                        }
                        is WalkDeleteResult.SuccessWithMediaCleanupFailures -> {
                            deletedCount++
                            mediaFailureCount += result.failedPaths.size
                        }
                    }
                }

                if (mediaFailureCount > 0) {
                    _bulkDeleteState.value = BulkDeleteState.PartialSuccess(
                        deletedCount = deletedCount,
                        mediaFailureCount = mediaFailureCount
                    )
                } else {
                    _bulkDeleteState.value = BulkDeleteState.Success(deletedCount = deletedCount)
                }
            } catch (e: Throwable) {
                _bulkDeleteState.value = BulkDeleteState.Error(
                    e.message ?: "An error occurred while deleting walks"
                )
            }
        }
    }
}
