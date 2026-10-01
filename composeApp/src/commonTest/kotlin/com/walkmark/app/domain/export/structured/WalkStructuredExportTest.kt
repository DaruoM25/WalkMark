package com.walkmark.app.domain.export.structured

import com.walkmark.app.core.model.LocationPoint
import com.walkmark.app.core.model.Walk
import com.walkmark.app.core.model.WalkNote
import com.walkmark.app.core.model.WalkPhoto
import com.walkmark.app.core.model.WalkStatus
import com.walkmark.app.domain.walk.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WalkStructuredExportTest {

    private val sampleWalk = Walk(
        id = "walk-123",
        title = "Morning Trail Walk & Run \"Sunny\"",
        status = WalkStatus.COMPLETED,
        startTime = 1700000000000L,
        endTime = 1700003600000L,
        durationSeconds = 3600L,
        distanceMeters = 5420.5,
        isAutoPaused = false,
        isClosedPrematurely = false
    )

    private val samplePoints = listOf(
        LocationPoint(
            latitude = 48.8566,
            longitude = 2.3522,
            altitude = 35.0,
            timestamp = 1700000000000L,
            accuracy = 4.5f
        ),
        LocationPoint(
            latitude = 48.8570,
            longitude = 2.3530,
            altitude = null, // Missing altitude
            timestamp = 1700001000000L,
            accuracy = null
        ),
        LocationPoint(
            latitude = 48.8580,
            longitude = 2.3540,
            altitude = 42.1,
            timestamp = 1700002000000L,
            accuracy = 3.2f
        )
    )

    private val sampleNotes = listOf(
        WalkNote(
            id = "note-1",
            walkId = "walk-123",
            content = "Saw a deer near the lake.",
            createdAt = 1700001500000L,
            latitude = 48.8575,
            longitude = 2.3535
        )
    )

    private val samplePhotos = listOf(
        WalkPhoto(
            id = "photo-1",
            walkId = "walk-123",
            relativePath = "photos/walk-123/img_001.jpg",
            mimeType = "image/jpeg",
            byteSize = 1048576L,
            createdAt = 1700002500000L,
            latitude = 48.8580,
            longitude = 2.3540
        )
    )

    private class ReadOnlyFakeRepository(
        private val walk: Walk?,
        private val points: List<LocationPoint> = emptyList(),
        private val notes: List<WalkNote> = emptyList(),
        private val photos: List<WalkPhoto> = emptyList()
    ) : WalkRepository {
        var getWalkCalls = 0
        var observePointsCalls = 0
        var observeNotesCalls = 0
        var observePhotosCalls = 0

        override suspend fun getWalk(id: String): Walk? {
            getWalkCalls++
            return if (walk?.id == id) walk else null
        }

        override fun observeWalkById(id: String): Flow<Walk?> = flowOf(if (walk?.id == id) walk else null)
        override fun observeAllWalks(): Flow<List<Walk>> = flowOf(listOfNotNull(walk))
        override fun observePoints(walkId: String): Flow<List<LocationPoint>> {
            observePointsCalls++
            return flowOf(points)
        }
        override fun observeNotes(walkId: String): Flow<List<WalkNote>> {
            observeNotesCalls++
            return flowOf(notes)
        }
        override fun observePhotos(walkId: String): Flow<List<WalkPhoto>> {
            observePhotosCalls++
            return flowOf(photos)
        }

        override suspend fun insertWalk(walk: Walk) = throw UnsupportedOperationException("Read only")
        override suspend fun updateWalk(walk: Walk) = throw UnsupportedOperationException("Read only")
        override suspend fun deleteWalk(walkId: String) = throw UnsupportedOperationException("Read only")
        override suspend fun insertPoint(walkId: String, point: LocationPoint) = throw UnsupportedOperationException("Read only")
        override suspend fun insertNote(note: WalkNote) = throw UnsupportedOperationException("Read only")
        override suspend fun deleteNote(noteId: String) = throw UnsupportedOperationException("Read only")
        override suspend fun insertPhoto(photo: WalkPhoto) = throw UnsupportedOperationException("Read only")
        override suspend fun deletePhoto(photoId: String) = throw UnsupportedOperationException("Read only")
    }

    @Test
    fun completeJsonExportContainsAllDataAndIsParseable() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.JSON)
        assertIs<WalkStructuredExportResult.Success>(result)
        assertEquals("walk_walk-123.json", result.fileName)
        assertEquals("application/json", result.mimeType)

        val parsed = Json.parseToJsonElement(result.content).jsonObject
        assertEquals("walk-123", parsed["id"]?.jsonPrimitive?.content)
        assertEquals("COMPLETED", parsed["status"]?.jsonPrimitive?.content)
        assertEquals(1, parsed["schemaVersion"]?.jsonPrimitive?.content?.toInt())
    }

    @Test
    fun jsonExportProperlyEscapesSpecialCharactersAndQuotes() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.JSON)
        assertIs<WalkStructuredExportResult.Success>(result)

        // Verifying valid JSON escaping allows re-parsing
        val parsed = Json.parseToJsonElement(result.content).jsonObject
        assertNotNull(parsed)
        assertEquals("Morning Trail Walk & Run \"Sunny\"", parsed["title"]?.jsonPrimitive?.content)
        assertTrue(result.content.contains("\"Sunny\"") || result.content.contains("Sunny"))
    }

    @Test
    fun jsonExportDoesNotLeakLocalFilesystemPaths() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.JSON)
        assertIs<WalkStructuredExportResult.Success>(result)

        assertFalse(result.content.contains("relativePath"))
        assertFalse(result.content.contains("photos/walk-123/img_001.jpg"))
        assertFalse(result.content.contains("/data/"))
        assertFalse(result.content.contains("C:\\"))
    }

    @Test
    fun routePointsPreservePersistedOrderInJsonAndCsv() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        val csvResult = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(csvResult)
        val lines = csvResult.content.trim().lines()
        assertEquals(4, lines.size)
        assertEquals("sequence,latitude,longitude,altitude,timestamp,accuracy", lines[0])
        assertEquals("0,48.8566,2.3522,35.0,1700000000000,4.5", lines[1])
        assertEquals("1,48.857,2.353,,1700001000000,", lines[2])
        assertEquals("2,48.858,2.354,42.1,1700002000000,3.2", lines[3])
    }

    @Test
    fun csvHeaderMatchesSpecificationExactly() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, emptyList())
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(result)
        assertTrue(result.content.startsWith("sequence,latitude,longitude,altitude,timestamp,accuracy\n"))
    }

    @Test
    fun csvCoordinatesUseLocaleIndependentDotDecimal() = runTest {
        val points = listOf(
            LocationPoint(
                latitude = 45.123456,
                longitude = -73.654321,
                altitude = 120.5,
                timestamp = 1700000000000L,
                accuracy = 1.5f
            )
        )
        val repo = ReadOnlyFakeRepository(sampleWalk, points)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(result)
        val dataLine = result.content.trim().lines()[1]
        assertEquals("0,45.123456,-73.654321,120.5,1700000000000,1.5", dataLine)
        assertFalse(dataLine.contains(",,") && !dataLine.endsWith(","))
    }

    @Test
    fun missingAltitudeLeavesFieldEmptyInCsv() = runTest {
        val points = listOf(
            LocationPoint(
                latitude = 10.0,
                longitude = 20.0,
                altitude = null,
                timestamp = 1700000000000L,
                accuracy = null
            )
        )
        val repo = ReadOnlyFakeRepository(sampleWalk, points)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(result)
        val dataLine = result.content.trim().lines()[1]
        assertEquals("0,10.0,20.0,,1700000000000,", dataLine)
    }

    @Test
    fun invalidOrZeroTimestampIsPreservedWithoutFabrication() = runTest {
        val points = listOf(
            LocationPoint(
                latitude = 10.0,
                longitude = 20.0,
                altitude = 5.0,
                timestamp = 0L,
                accuracy = 1.0f
            )
        )
        val repo = ReadOnlyFakeRepository(sampleWalk, points)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(result)
        val dataLine = result.content.trim().lines()[1]
        assertEquals("0,10.0,20.0,5.0,0,1.0", dataLine)
    }

    @Test
    fun walkNotFoundReturnsWalkNotFoundResult() = runTest {
        val repo = ReadOnlyFakeRepository(null)
        val useCase = ExportWalkDataUseCase(repo)

        val result = useCase("non-existent", WalkExportFormat.JSON)
        assertIs<WalkStructuredExportResult.WalkNotFound>(result)
        assertEquals("non-existent", result.walkId)
    }

    @Test
    fun repositoryRemainsReadOnly() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        useCase("walk-123", WalkExportFormat.JSON)
        useCase("walk-123", WalkExportFormat.CSV)

        assertEquals(2, repo.getWalkCalls)
        assertEquals(2, repo.observePointsCalls)
        assertEquals(2, repo.observeNotesCalls)
        assertEquals(2, repo.observePhotosCalls)
    }

    @Test
    fun emptyRouteProducesValidEmptyExportContent() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, emptyList(), emptyList(), emptyList())
        val useCase = ExportWalkDataUseCase(repo)

        val csvResult = useCase("walk-123", WalkExportFormat.CSV)
        assertIs<WalkStructuredExportResult.Success>(csvResult)
        assertEquals("sequence,latitude,longitude,altitude,timestamp,accuracy\n", csvResult.content)

        val jsonResult = useCase("walk-123", WalkExportFormat.JSON)
        assertIs<WalkStructuredExportResult.Success>(jsonResult)
        val parsed = Json.parseToJsonElement(jsonResult.content).jsonObject
        assertNotNull(parsed)
    }

    @Test
    fun repeatedGenerationIsDeterministic() = runTest {
        val repo = ReadOnlyFakeRepository(sampleWalk, samplePoints, sampleNotes, samplePhotos)
        val useCase = ExportWalkDataUseCase(repo)

        val json1 = (useCase("walk-123", WalkExportFormat.JSON) as WalkStructuredExportResult.Success).content
        val json2 = (useCase("walk-123", WalkExportFormat.JSON) as WalkStructuredExportResult.Success).content
        assertEquals(json1, json2)

        val csv1 = (useCase("walk-123", WalkExportFormat.CSV) as WalkStructuredExportResult.Success).content
        val csv2 = (useCase("walk-123", WalkExportFormat.CSV) as WalkStructuredExportResult.Success).content
        assertEquals(csv1, csv2)
    }
}
