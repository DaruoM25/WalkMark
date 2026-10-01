package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import com.walkmark.app.presentation.location.LocationTrackingUiState

data class LiveMapUiState(
    val currentPosition: GeoCoordinate?,
    val route: MapRouteUiModel,
    val recenterRequestId: Long
) {
    val startPoint: GeoCoordinate? get() = route.coordinates.firstOrNull()
}

fun LocationTrackingUiState.toLiveMapUiState(recenterRequestId: Long): LiveMapUiState =
    LiveMapUiState(
        currentPosition = lastLocation?.let { GeoCoordinate(it.latitude, it.longitude) },
        route = points.toMapRouteUiModel(),
        recenterRequestId = recenterRequestId
    )
