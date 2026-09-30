package com.walkmark.app.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val STYLE_JSON = """{"version":8,"sources":{"osm":{"type":"raster","tiles":["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],"tileSize":256,"attribution":"© OpenStreetMap contributors"},"route":{"type":"geojson","data":{"type":"FeatureCollection","features":[]}},"position":{"type":"geojson","data":{"type":"FeatureCollection","features":[]}},"start":{"type":"geojson","data":{"type":"FeatureCollection","features":[]}}},"layers":[{"id":"osm-tiles","type":"raster","source":"osm"},{"id":"walk-route","type":"line","source":"route","paint":{"line-color":"#1565C0","line-width":5}},{"id":"walk-start","type":"circle","source":"start","paint":{"circle-color":"#2E7D32","circle-radius":7,"circle-stroke-color":"#FFFFFF","circle-stroke-width":2}},{"id":"walk-position","type":"circle","source":"position","paint":{"circle-color":"#1565C0","circle-radius":9,"circle-stroke-color":"#FFFFFF","circle-stroke-width":3}}]}"""
private const val EMPTY_FEATURES = """{"type":"FeatureCollection","features":[]}"""

@Composable
fun AndroidLiveMap(state: LiveMapUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context) {
        MapLibre.getInstance(context)
        // OSM requires a stable app-specific User-Agent; leave HTTP caching to OkHttp/MapLibre.
        HttpRequestUtil.setOkHttpClient(
            OkHttpClient.Builder().addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "WalkMark/1.0 (internal Android map MVP)")
                        .build()
                )
            }.build()
        )
        MapView(context).apply { onCreate(null) }
    }
    var map by remember(mapView) { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember(mapView) { mutableStateOf(false) }
    var hasCentered by remember(mapView) { mutableStateOf(false) }
    var handledRecenterId by remember(mapView) { mutableLongStateOf(state.recenterRequestId) }

    DisposableEffect(mapView, lifecycleOwner) {
        var started = false
        var resumed = false
        var destroyed = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> { mapView.onStart(); started = true }
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); resumed = true }
                Lifecycle.Event.ON_PAUSE -> { mapView.onPause(); resumed = false }
                Lifecycle.Event.ON_STOP -> { mapView.onStop(); started = false }
                Lifecycle.Event.ON_DESTROY -> { mapView.onDestroy(); destroyed = true }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (resumed) mapView.onPause()
            if (started) mapView.onStop()
            if (!destroyed) mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { readyMap ->
            readyMap.setPrefetchesTiles(false)
            readyMap.setPrefetchZoomDelta(0)
            readyMap.setStyle(Style.Builder().fromJson(STYLE_JSON)) {
                map = readyMap
                styleReady = true
            }
        }
    }

    LaunchedEffect(map, styleReady, state.route, state.currentPosition, state.startPoint) {
        val readyMap = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect
        val style = readyMap.style ?: return@LaunchedEffect
        val line = state.route.coordinates.map { Point.fromLngLat(it.longitude, it.latitude) }
        style.getSourceAs<GeoJsonSource>("route")?.setGeoJson(
            if (line.size >= 2) LineString.fromLngLats(line).toJson() else EMPTY_FEATURES
        )
        style.getSourceAs<GeoJsonSource>("position")?.setGeoJson(
            state.currentPosition?.let { Point.fromLngLat(it.longitude, it.latitude).toJson() } ?: EMPTY_FEATURES
        )
        style.getSourceAs<GeoJsonSource>("start")?.setGeoJson(
            state.startPoint?.let { Point.fromLngLat(it.longitude, it.latitude).toJson() } ?: EMPTY_FEATURES
        )
    }

    LaunchedEffect(map, styleReady, state.currentPosition, state.recenterRequestId) {
        val readyMap = map ?: return@LaunchedEffect
        val position = state.currentPosition ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect
        if (!hasCentered || handledRecenterId != state.recenterRequestId) {
            readyMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(position.latitude, position.longitude), 16.0)
            )
            hasCentered = true
            handledRecenterId = state.recenterRequestId
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
