package com.walkmark.app.domain.export

import com.walkmark.app.data.walk.RecordingWalkRepository
import com.walkmark.app.domain.repository.WalkRepository
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkDeleteResult
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExportWalkSummaryUseCaseTest {
    private val walk = Walk(
        id = "internal-walk-42",
        title = "Morning walk",
        summary = "A quiet loop",
        startTimeEpochMs = 0L,
        endTimeEpochMs = 3_723_000L,
        totalDistanceMeters = 1_234.4,
        durationSeconds = 3_723L,
        status = WalkStatus.COMPLETED
    )

    private class ReadOnlyRepository(
        private val walk: Walk?,
        private val notes: List<WalkNote> = emptyList(),
        private val photos: List<WalkPhoto> = emptyList(),
        val writes: RecordingWalkRepository = RecordingWalkRepository()
    ) : WalkRepository by writes {
        var deleteCalls = 0
        var noteReads = 0
        var photoReads = 0

        override suspend fun deleteWalk(walkId: String): WalkDeleteResult {
            deleteCalls++
            return WalkDeleteResult.Success
        }
        override suspend fun getWalk(walkId: String): Walk? = walk
        override fun observeNotes(walkId: String): Flow<List<WalkNote>> {
            noteReads++
            return flowOf(notes)
        }
        override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> {
            photoReads++
            return flowOf(photos)
        }
    }

    @Test
    fun completeSummaryUsesOnlyPersistedPublicFields() = runTest {
        val note = WalkNote("note-1", walk.id, "Private note", 48.123456, 2.123456, 1L)
        val photo = WalkPhoto("photo-1", walk.id, 48.234567, 2.234567, "private/secret.jpg", "image/jpeg", 1L, 1L)
        val repository = ReadOnlyRepository(walk, listOf(note), listOf(photo))

        val result = assertIs<WalkSummaryExportResult.Success>(ExportWalkSummaryUseCase(repository)(walk.id))

        assertEquals(
            "Morning walk\nStarted: 1970-01-01 00:00:00 UTC\nDuration: 1h 2m 3s\n" +
                "Distance: 1234 m\nSummary: A quiet loop\nNotes: 1\nPhotos: 1",
            result.text
        )
        assertFalse(result.text.contains(walk.id))
        assertFalse(result.text.contains(photo.relativePath))
        assertFalse(result.text.contains("48.123456"))
        assertFalse(result.text.contains("2.234567"))
        assertEquals(1, repository.noteReads)
        assertEquals(1, repository.photoReads)
        assertTrue(repository.writes.startCalls.isEmpty())
        assertTrue(repository.writes.completeCalls.isEmpty())
        assertTrue(repository.writes.appendedPoints.isEmpty())
        assertTrue(repository.writes.notes.isEmpty())
        assertTrue(repository.writes.photos.isEmpty())
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun optionalSummaryAndEmptyCountsAreHandled() = runTest {
        val withoutSummary = walk.copy(summary = null)
        val result = assertIs<WalkSummaryExportResult.Success>(
            ExportWalkSummaryUseCase(ReadOnlyRepository(withoutSummary))(walk.id)
        )
        assertFalse(result.text.contains("Summary:"))
        assertTrue(result.text.contains("Notes: 0"))
        assertTrue(result.text.contains("Photos: 0"))
    }

    @Test
    fun missingWalkDoesNotReadRelatedData() = runTest {
        val repository = ReadOnlyRepository(null)
        assertEquals(WalkSummaryExportResult.WalkNotFound, ExportWalkSummaryUseCase(repository)("missing"))
        assertEquals(0, repository.noteReads)
        assertEquals(0, repository.photoReads)
    }

    @Test
    fun utcFormattingIsDeterministicAcrossDayAndLeapYearBoundaries() {
        val generator = WalkSummaryGenerator()
        assertEquals("1969-12-31 23:59:59 UTC", generator.formatUtc(-1_000L))
        assertEquals("1970-01-01 00:00:00 UTC", generator.formatUtc(0L))
        assertEquals("2000-02-29 12:34:56 UTC", generator.formatUtc(951_827_696_000L))
    }
}
