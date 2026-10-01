package com.walkmark.app.domain.cloud

import com.walkmark.app.domain.walk.WalkStatus
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Proves the wire format of the cloud models, in particular that no local
 * filesystem locator can ever leave the device through these contracts.
 */
class CloudWireFormatTest {

    private val json = Json { encodeDefaults = true }

    private val forbiddenLocators = listOf("path", "uri", "file", "url")

    private fun assertNoFilesystemLocator(encoded: String, model: String) {
        forbiddenLocators.forEach { needle ->
            assertFalse(
                encoded.contains(needle, ignoreCase = true),
                "$model wire format must not expose '$needle' -> $encoded"
            )
        }
    }

    private fun photo() = CloudPhotoMetadata(
        localId = "photo-1",
        localWalkId = "walk-1",
        latitude = 48.8584,
        longitude = 2.2945,
        mimeType = "image/jpeg",
        byteSize = 204_800L,
        createdAtEpochMs = 1_700_000_900_000L
    )

    private fun walk(status: WalkStatus) = CloudWalk(
        localId = "walk-1",
        cloudId = null,
        title = "Morning walk",
        summary = "Riverside loop",
        startTimeEpochMs = 1_700_000_000_000L,
        endTimeEpochMs = 1_700_003_600_000L,
        totalDistanceMeters = 4_210.5,
        durationSeconds = 3_600L,
        status = status
    )

    @Test
    fun photoMetadataWireFormatExposesNoFilesystemLocator() {
        val encoded = json.encodeToString(photo())

        assertNoFilesystemLocator(encoded, "CloudPhotoMetadata")
        assertTrue(encoded.contains("mimeType"), "mime type must remain part of the metadata contract")
        assertTrue(encoded.contains("byteSize"), "byte size must remain part of the metadata contract")
    }

    @Test
    fun photoMetadataRoundTripsThroughTheWireFormat() {
        val source = photo()

        val decoded = json.decodeFromString<CloudPhotoMetadata>(json.encodeToString(source))

        assertEquals(source, decoded)
    }

    @Test
    fun walkWireFormatKeepsLocalIdAuthoritativeAndRemoteIdOptional() {
        val localOnly = walk(WalkStatus.COMPLETED)
        val remoteAssigned = localOnly.copy(cloudId = CloudEntityId("remote-1"))

        val encodedLocalOnly = json.encodeToString(localOnly)
        val encodedRemote = json.encodeToString(remoteAssigned)

        assertNoFilesystemLocator(encodedLocalOnly, "CloudWalk")
        assertTrue(encodedLocalOnly.contains("walk-1"), "local id must be the persisted identity")
        assertTrue(encodedLocalOnly.contains("\"cloudId\":null"), "remote id must be optional before first upsert")
        assertTrue(encodedRemote.contains("remote-1"), "remote id must round-trip once assigned")
        assertEquals(localOnly, json.decodeFromString<CloudWalk>(encodedLocalOnly))
        assertEquals(remoteAssigned, json.decodeFromString<CloudWalk>(encodedRemote))
    }

    @Test
    fun walkStatusIsEncodedUsingTheExistingDomainEnumName() {
        val completed = json.encodeToString(walk(WalkStatus.COMPLETED))
        val active = json.encodeToString(walk(WalkStatus.ACTIVE))

        assertTrue(completed.contains("COMPLETED"), "status must reuse the domain WalkStatus names")
        assertTrue(active.contains("ACTIVE"), "status must reuse the domain WalkStatus names")
    }

    @Test
    fun routePointWireFormatCarriesExplicitSequenceForOrdering() {
        val points = listOf(
            CloudRoutePoint("walk-1", 0, 48.8584, 2.2945, 35.0, 1_700_000_000_000L, 4.5f),
            CloudRoutePoint("walk-1", 1, 48.8594, 2.2955, null, 1_700_000_001_000L, 4.5f)
        )

        val encoded = json.encodeToString(points)

        assertNoFilesystemLocator(encoded, "CloudRoutePoint")
        assertTrue(encoded.contains("\"seq\":0"), "route order must be explicit on the wire")
        assertTrue(encoded.contains("\"seq\":1"), "route order must be explicit on the wire")
        assertEquals(points, json.decodeFromString<List<CloudRoutePoint>>(encoded))
    }

    @Test
    fun noteWireFormatRoundTripsAndExposesNoFilesystemLocator() {
        val source = CloudWalkNote(
            localId = "note-1",
            localWalkId = "walk-1",
            text = "Bench by the water",
            latitude = 48.8584,
            longitude = 2.2945,
            createdAtEpochMs = 1_700_000_500_000L
        )

        val encoded = json.encodeToString(source)

        assertNoFilesystemLocator(encoded, "CloudWalkNote")
        assertEquals(source, json.decodeFromString<CloudWalkNote>(encoded))
    }

    @Test
    fun ownershipIsCallScopeAndIsNeverSerializedIntoModelData() {
        val owner = CloudOwnerId("owner-a")
        val encodedModels = listOf(
            json.encodeToString(photo()),
            json.encodeToString(
                CloudRoutePoint("walk-1", 0, 48.8584, 2.2945, null, 1_700_000_000_000L, 4.5f)
            ),
            json.encodeToString(
                CloudWalkNote("note-1", "walk-1", "Bench", 48.8584, 2.2945, 1_700_000_500_000L)
            ),
            json.encodeToString(walk(WalkStatus.ACTIVE))
        )

        assertTrue(encodedModels.isNotEmpty())
        encodedModels.forEach { encoded ->
            assertFalse(
                encoded.contains(owner.value),
                "ownership is a call scope, not model data -> $encoded"
            )
        }
    }
}