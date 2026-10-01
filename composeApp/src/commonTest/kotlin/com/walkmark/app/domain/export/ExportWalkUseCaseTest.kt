package com.walkmark.app.domain.export

import com.walkmark.app.data.walk.InMemoryWalkRepository
import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk
import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExportWalkUseCaseTest {

    private val repository = InMemoryWalkRepository()
    private val useCase = ExportWalkUseCase(repository)

    private fun point(latitude: Double, longitude: Double, altitude: Double? = null) = LocationPoint(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        timestamp = 1_700_000_000_000L,
        accuracy = 4.5f
    )

    private suspend fun seedWalk(title: String = "Morning loop", walkId: String = "walk-1"): Walk {
        val walk = repository.startWalk(walkId = walkId, title = title, startTimeEpochMs = 1_700_000_000_000L)
        return repository.completeWalk(
            walkId = walk.id,
            endTimeEpochMs = 1_700_003_600_000L,
            totalDistanceMeters = 4321.5
        ) ?: error("walk was not persisted")
    }

    @Test
    fun exportProducesGpxForPersistedWalk() = runTest {
        val walk = seedWalk()
        repository.appendPoints(walk.id, listOf(point(1.0, 1.0), point(2.0, 2.0, altitude = 10.0)))

        val result = useCase.exportGpx(walk.id)

        val success = assertIs<WalkExportResult.Success>(result)
        assertEquals(walk.id, success.walkId)
        assertTrue(success.gpxContent.contains("version=\"1.1\""), success.gpxContent)
        assertTrue(success.gpxContent.contains("<ele>10.0</ele>"), success.gpxContent)
        assertEquals(
            listOf("1.0", "2.0"),
            Regex("lat=\"([^\"]+)\"").findAll(success.gpxContent).map { it.groupValues[1] }.toList()
        )
    }

    @Test
    fun exportOfMissingWalkReportsWalkNotFound() = runTest {
        val result = useCase.exportGpx("does-not-exist")

        assertIs<WalkExportResult.WalkNotFound>(result)
    }

    @Test
    fun exportDoesNotMutatePersistedWalkOrPoints() = runTest {
        val walk = seedWalk()
        repository.appendPoints(walk.id, listOf(point(1.0, 1.0), point(2.0, 2.0)))

        val walkBefore = repository.getWalk(walk.id)
        val pointsBefore = repository.observePoints(walk.id).first()

        val result = useCase.exportGpx(walk.id)

        assertIs<WalkExportResult.Success>(result)
        assertEquals(walkBefore, repository.getWalk(walk.id))
        assertEquals(pointsBefore, repository.observePoints(walk.id).first())
        assertEquals(2, pointsBefore.size)
    }

    @Test
    fun exportOfWalkWithoutPointsProducesEmptyTrack() = runTest {
        val walk = seedWalk()

        val result = useCase.exportGpx(walk.id)

        val success = assertIs<WalkExportResult.Success>(result)
        assertEquals(0, Regex("<trkpt ").findAll(success.gpxContent).count())
        assertTrue(success.gpxContent.contains("<trkseg>"), success.gpxContent)
    }

    @Test
    fun fileNameUsesSanitizedTitleAndWalkId() = runTest {
        val walk = seedWalk(title = "Morning loop")

        val result = useCase.exportGpx(walk.id)

        val success = assertIs<WalkExportResult.Success>(result)
        assertEquals("walkmark-morning-loop-walk-1.gpx", success.fileName)
    }

    @Test
    fun fileNameStripsPathTraversalAndSeparatorsFromTitle() = runTest {
        val walk = seedWalk(title = "../../etc/passwd")

        val result = useCase.exportGpx(walk.id)

        val success = assertIs<WalkExportResult.Success>(result)
        assertFalse(success.fileName.contains('/'), success.fileName)
        assertFalse(success.fileName.contains('\\'), success.fileName)
        assertFalse(success.fileName.contains(".."), success.fileName)
        assertEquals("walkmark-etc-passwd-walk-1.gpx", success.fileName)
    }

    @Test
    fun fileNameFallsBackToWalkIdWhenTitleHasNoUsableCharacters() = runTest {
        val walk = seedWalk(title = "///")

        val result = useCase.exportGpx(walk.id)

        val success = assertIs<WalkExportResult.Success>(result)
        assertEquals("walkmark-walk-1.gpx", success.fileName)
    }

    @Test
    fun fileNameIsBoundedAndCarriesExactlyOneGpxExtension() {
        val walk = Walk(
            id = "walk-1",
            title = "a".repeat(400),
            summary = null,
            startTimeEpochMs = 0L,
            endTimeEpochMs = null,
            totalDistanceMeters = 0.0,
            durationSeconds = 0L,
            status = WalkStatus.COMPLETED
        )

        val fileName = ExportWalkUseCase.buildGpxFileName(walk)

        assertEquals(1, Regex("\\.gpx").findAll(fileName).count())
        assertTrue(fileName.endsWith(".gpx"), fileName)
        assertTrue(fileName.length <= "walkmark-".length + 40 + "-".length + 40 + ".gpx".length, fileName)
    }
}
