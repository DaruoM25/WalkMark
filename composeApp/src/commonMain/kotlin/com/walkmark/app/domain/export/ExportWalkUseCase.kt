package com.walkmark.app.domain.export

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import kotlinx.coroutines.flow.first

sealed interface WalkExportResult {
    data class Success(val walkId: String, val fileName: String, val gpxContent: String) :
        WalkExportResult

    data object WalkNotFound : WalkExportResult
}

/**
 * Produces a GPX 1.1 document for an already-persisted walk.
 *
 * Strictly read-only: it uses only [WalkRepository.getWalk] and [WalkRepository.observePoints],
 * and never mutates walk data, identifiers, or media. It requires no account, no network,
 * and no cloud session. Point order is the order the repository returns, which is the
 * persisted `ORDER BY seq ASC` sequence; points are never re-sorted here.
 */
class ExportWalkUseCase(
    private val walkRepository: WalkRepository
) {

    suspend fun exportGpx(walkId: String): WalkExportResult {
        val walk = walkRepository.getWalk(walkId) ?: return WalkExportResult.WalkNotFound
        val points: List<LocationPoint> = walkRepository.observePoints(walkId).first()
        return WalkExportResult.Success(
            walkId = walk.id,
            fileName = buildGpxFileName(walk),
            gpxContent = GpxDocumentGenerator.generate(walk = walk, points = points)
        )
    }

    companion object {
        private const val FILE_PREFIX = "walkmark"
        private const val FILE_EXTENSION = ".gpx"
        private const val MAX_TITLE_LENGTH = 40

        /**
         * Builds `walkmark-<sanitized-title>-<walk-id>.gpx`, degrading to
         * `walkmark-<walk-id>.gpx` when the title carries no usable characters.
         *
         * Both segments are restricted to an allow-list of `[A-Za-z0-9-_]`, so the result
         * can never contain a path separator, a drive-relative segment, or `..`.
         */
        fun buildGpxFileName(walk: Walk): String {
            val sanitizedTitle = sanitizeSegment(walk.title, MAX_TITLE_LENGTH)
            val sanitizedId = sanitizeSegment(walk.id, MAX_TITLE_LENGTH)
            return if (sanitizedTitle.isEmpty() || sanitizedId.isEmpty()) {
                "$FILE_PREFIX-$sanitizedId$FILE_EXTENSION"
            } else {
                "$FILE_PREFIX-$sanitizedTitle-$sanitizedId$FILE_EXTENSION"
            }
        }

        private fun sanitizeSegment(raw: String, maxLength: Int): String {
            val builder = StringBuilder(raw.length)
            var previousWasSeparator = false
            for (character in raw) {
                val isAllowed = character.isAsciiLetterOrDigit() || character == '-' || character == '_'
                if (isAllowed) {
                    builder.append(character.lowercaseChar())
                    previousWasSeparator = false
                } else if (!previousWasSeparator && builder.isNotEmpty()) {
                    builder.append('-')
                    previousWasSeparator = true
                }
            }

            return builder.toString()
                .trim('-')
                .take(maxLength)
                .trim('-')
        }

        /** Explicit ASCII test; locale-sensitive character classification is not used. */
        private fun Char.isAsciiLetterOrDigit(): Boolean =
            this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'
    }
}
