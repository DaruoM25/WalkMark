package com.walkmark.app.domain.walk

/**
 * Result of a [com.walkmark.app.domain.repository.WalkRepository.deleteWalk] operation.
 *
 * The DB walk record is always deleted when this result is returned.
 * [SuccessWithMediaCleanupFailures] indicates the walk was removed from the DB but one or more
 * associated media files could not be deleted from local storage.
 */
sealed class WalkDeleteResult {
    /** Walk and all associated media were deleted successfully. */
    object Success : WalkDeleteResult()

    /**
     * Walk DB record deleted successfully, but some media files could not be removed.
     *
     * @param failedPaths relative paths of media files that could not be deleted.
     */
    data class SuccessWithMediaCleanupFailures(
        val failedPaths: List<String>
    ) : WalkDeleteResult()
}
