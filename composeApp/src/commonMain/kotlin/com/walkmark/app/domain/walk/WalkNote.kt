package com.walkmark.app.domain.walk

import com.walkmark.app.domain.geo.GeoCoordinate

data class WalkNote(
    val id: String,
    val walkId: String,
    val text: String,
    val latitude: Double,
    val longitude: Double,
    val createdAtEpochMs: Long
) {
    val coordinate: GeoCoordinate
        get() = GeoCoordinate(latitude, longitude)
}
