package com.walkmark.app.presentation.map

sealed interface MapUiState {
    data object Loading : MapUiState
    data object Idle : MapUiState
    data class Active(
        val walkId: String,
        val route: MapRouteUiModel,
        val markers: List<MapMarkerUiModel>,
        val totalDistanceMeters: Double
    ) : MapUiState
    data class Completed(val walkId: String, val totalDistanceMeters: Double) : MapUiState
    data class Error(val message: String) : MapUiState
}
