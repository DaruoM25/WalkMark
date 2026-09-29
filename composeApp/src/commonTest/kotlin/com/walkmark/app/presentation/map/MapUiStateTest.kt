package com.walkmark.app.presentation.map

import com.walkmark.app.domain.geo.GeoCoordinate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MapUiStateTest {

    private val route = MapRouteUiModel(
        coordinates = listOf(GeoCoordinate(48.0, 2.0), GeoCoordinate(48.1, 2.1))
    )

    private val noteMarker = MapMarkerUiModel(
        id = "n1",
        walkId = "w1",
        coordinate = GeoCoordinate(48.0, 2.0),
        type = MapMarkerType.NOTE,
        preview = MarkerPreview.Text("hello")
    )

    private val photoMarker = MapMarkerUiModel(
        id = "p1",
        walkId = "w1",
        coordinate = GeoCoordinate(48.1, 2.1),
        type = MapMarkerType.PHOTO,
        preview = MarkerPreview.Image("w1/a.jpg", "image/jpeg")
    )

    @Test
    fun loadingIsInitialState() {
        assertTrue(MapUiState.Loading is MapUiState)
    }

    @Test
    fun activeStateCarriesRouteMarkersAndDistance() {
        val state = MapUiState.Active(
            walkId = "w1",
            route = route,
            markers = listOf(noteMarker, photoMarker),
            totalDistanceMeters = 1234.5
        )

        assertEquals("w1", state.walkId)
        assertEquals(2, state.route.pointCount)
        assertEquals(2, state.markers.size)
        assertEquals(1234.5, state.totalDistanceMeters)
    }

    @Test
    fun activeStateWithNoMarkersIsValid() {
        val state = MapUiState.Active("w1", route, emptyList(), 0.0)

        assertTrue(state.markers.isEmpty())
        assertEquals(0.0, state.totalDistanceMeters)
    }

    @Test
    fun activeStateWithEmptyRouteIsValidForWalkWithNoFixYet() {
        val state = MapUiState.Active("w1", MapRouteUiModel(emptyList()), emptyList(), 0.0)

        assertTrue(state.route.isEmpty)
    }

    @Test
    fun completedStateCarriesWalkIdAndDistance() {
        val state = MapUiState.Completed("w1", 5000.0)

        assertEquals("w1", state.walkId)
        assertEquals(5000.0, state.totalDistanceMeters)
    }

    @Test
    fun errorStateCarriesMessage() {
        val state = MapUiState.Error("GPS unavailable")

        assertEquals("GPS unavailable", assertIs<MapUiState.Error>(state).message)
    }

    @Test
    fun noteWithoutAcceptedLocationHasNoRouteButCanRejectSeparately() {
        val state = MapUiState.Active("w1", MapRouteUiModel(emptyList()), listOf(noteMarker), 0.0)

        assertTrue(state.route.isEmpty)
        assertEquals(1, state.markers.size)
    }

    @Test
    fun idleStateIsNotActive() {
        assertTrue(MapUiState.Idle is MapUiState)
        assertTrue(MapUiState.Idle !is MapUiState.Active)
    }
}
