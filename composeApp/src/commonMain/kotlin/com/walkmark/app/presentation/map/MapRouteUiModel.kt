package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.domain.location.LocationPoint

data class MapRouteUiModel(
    val coordinates: List<GeoCoordinate>
) {
    val pointCount: Int get() = coordinates.size
    val isEmpty: Boolean get() = coordinates.isEmpty()
    val bounds: MapBounds? get() = MapBoundsCalculator.calculate(coordinates)
}

fun List<LocationPoint>.toMapRouteUiModel(): MapRouteUiModel =
    MapRouteUiModel(coordinates = map { GeoCoordinate(latitude = it.latitude, longitude = it.longitude) })
