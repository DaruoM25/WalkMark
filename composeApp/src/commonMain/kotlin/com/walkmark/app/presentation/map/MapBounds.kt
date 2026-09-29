package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate

data class MapBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double
) {
    val latitudeSpan: Double get() = north - south
    val longitudeSpan: Double get() = east - west
    val centerLatitude: Double get() = (south + north) / 2.0
    val centerLongitude: Double get() = (west + east) / 2.0
    val isSinglePoint: Boolean get() = latitudeSpan == 0.0 && longitudeSpan == 0.0
}

object MapBoundsCalculator {
    fun calculate(coordinates: List<GeoCoordinate>): MapBounds? {
        if (coordinates.isEmpty()) return null

        var south = coordinates[0].latitude
        var north = coordinates[0].latitude
        var west = coordinates[0].longitude
        var east = coordinates[0].longitude

        for (coordinate in coordinates) {
            if (coordinate.latitude < south) south = coordinate.latitude
            if (coordinate.latitude > north) north = coordinate.latitude
            if (coordinate.longitude < west) west = coordinate.longitude
            if (coordinate.longitude > east) east = coordinate.longitude
        }

        return MapBounds(south = south, west = west, north = north, east = east)
    }
}
