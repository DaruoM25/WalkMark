package com.walkmark.app.domain.walk

import com.walkmark.app.domain.geo.GeoCoordinate

data class WalkPhoto(
    val id: String,
    val walkId: String,
    val latitude: Double,
    val longitude: Double,
    val relativePath: String,
    val mimeType: String,
    val byteSize: Long,
    val createdAtEpochMs: Long
) {
    val coordinate: GeoCoordinate
        get() = GeoCoordinate(latitude, longitude)
}
