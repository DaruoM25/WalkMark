package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.domain.walk.WalkNote
import com.walkmark.app.domain.walk.WalkPhoto

enum class MapMarkerType {
    NOTE,
    PHOTO
}

sealed interface MarkerPreview {
    data class Text(val text: String) : MarkerPreview
    data class Image(val relativePath: String, val mimeType: String) : MarkerPreview
    data class Unavailable(val reason: String) : MarkerPreview
}

data class MapMarkerUiModel(
    val id: String,
    val walkId: String,
    val coordinate: GeoCoordinate,
    val type: MapMarkerType,
    val preview: MarkerPreview
)

fun WalkNote.toMapMarkerUiModel(): MapMarkerUiModel = MapMarkerUiModel(
    id = id,
    walkId = walkId,
    coordinate = coordinate,
    type = MapMarkerType.NOTE,
    preview = MarkerPreview.Text(text = text)
)

fun WalkPhoto.toMapMarkerUiModel(): MapMarkerUiModel = MapMarkerUiModel(
    id = id,
    walkId = walkId,
    coordinate = coordinate,
    type = MapMarkerType.PHOTO,
    preview = MarkerPreview.Image(relativePath = relativePath, mimeType = mimeType)
)
