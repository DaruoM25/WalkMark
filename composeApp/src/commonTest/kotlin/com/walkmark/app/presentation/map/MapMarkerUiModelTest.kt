package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse

class MapMarkerUiModelTest {

    private val note = WalkNote(
        id = "note-1",
        walkId = "walk-1",
        text = "Fountain in the square",
        latitude = 48.8584,
        longitude = 2.2945,
        createdAtEpochMs = 1_700_000_000_000L
    )

    private val photo = WalkPhoto(
        id = "photo-1",
        walkId = "walk-1",
        latitude = 48.8600,
        longitude = 2.3000,
        relativePath = "walk-1/abc-123.jpg",
        mimeType = "image/jpeg",
        byteSize = 2048L,
        createdAtEpochMs = 1_700_000_001_000L
    )

    @Test
    fun noteMapsToNoteMarkerWithTextPreview() {
        val marker = note.toMapMarkerUiModel()

        assertEquals("note-1", marker.id)
        assertEquals("walk-1", marker.walkId)
        assertEquals(MapMarkerType.NOTE, marker.type)
        assertEquals(GeoCoordinate(48.8584, 2.2945), marker.coordinate)
        assertEquals(MarkerPreview.Text("Fountain in the square"), marker.preview)
    }

    @Test
    fun photoMapsToPhotoMarkerWithImagePreview() {
        val marker = photo.toMapMarkerUiModel()

        assertEquals("photo-1", marker.id)
        assertEquals("walk-1", marker.walkId)
        assertEquals(MapMarkerType.PHOTO, marker.type)
        assertEquals(GeoCoordinate(48.86, 2.30), marker.coordinate)
        assertEquals(
            MarkerPreview.Image(relativePath = "walk-1/abc-123.jpg", mimeType = "image/jpeg"),
            marker.preview
        )
    }

    @Test
    fun noteAndPhotoTypesAreDiscriminated() {
        assertEquals(MapMarkerType.NOTE, note.toMapMarkerUiModel().type)
        assertEquals(MapMarkerType.PHOTO, photo.toMapMarkerUiModel().type)
    }

    @Test
    fun markersFromSameWalkShareWalkId() {
        assertEquals(note.toMapMarkerUiModel().walkId, photo.toMapMarkerUiModel().walkId)
    }

    @Test
    fun photoMarkerPreviewNeverCarriesContentUri() {
        val marker = photo.toMapMarkerUiModel()

        val image = assertIs<MarkerPreview.Image>(marker.preview)
        assertFalse(image.relativePath.startsWith("content://"))
        assertFalse(image.relativePath.contains("://"))
    }

    @Test
    fun unavailablePreviewIsRepresentable() {
        val marker = MapMarkerUiModel(
            id = "m",
            walkId = "walk-1",
            coordinate = GeoCoordinate(0.0, 0.0),
            type = MapMarkerType.PHOTO,
            preview = MarkerPreview.Unavailable("file missing")
        )

        assertEquals("file missing", assertIs<MarkerPreview.Unavailable>(marker.preview).reason)
    }
}
