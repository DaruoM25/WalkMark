package com.walkmark.app.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.walkmark.app.domain.geo.GeoCoordinate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapTypeStandard
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.UIKit.UIColor
import platform.darwin.NSObject

private class MapViewDelegate : NSObject(), MKMapViewDelegateProtocol {
    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        if (rendererForOverlay is MKPolyline) {
            return MKPolylineRenderer(overlay = rendererForOverlay).apply {
                strokeColor = UIColor.colorWithRed(0.08, 0.40, 0.75, 1.0) // #1565C0
                lineWidth = 5.0
            }
        }
        return MKOverlayRenderer(overlay = rendererForOverlay)
    }

    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        if (viewForAnnotation is CurrentPositionAnnotation) {
            val view = (mapView.dequeueReusableAnnotationViewWithIdentifier("current_pos") as? MKMarkerAnnotationView)
                ?: MKMarkerAnnotationView(annotation = viewForAnnotation, reuseIdentifier = "current_pos")
            view.annotation = viewForAnnotation
            view.markerTintColor = UIColor.colorWithRed(0.08, 0.40, 0.75, 1.0) // #1565C0
            view.glyphText = ""
            return view
        }
        if (viewForAnnotation is StartPointAnnotation) {
            val view = (mapView.dequeueReusableAnnotationViewWithIdentifier("start_point") as? MKMarkerAnnotationView)
                ?: MKMarkerAnnotationView(annotation = viewForAnnotation, reuseIdentifier = "start_point")
            view.annotation = viewForAnnotation
            view.markerTintColor = UIColor.colorWithRed(0.18, 0.49, 0.20, 1.0) // #2E7D32
            view.glyphText = "S"
            return view
        }
        return null
    }
}

private class CurrentPositionAnnotation : MKPointAnnotation()
private class StartPointAnnotation : MKPointAnnotation()

@OptIn(ExperimentalForeignApi::class)
private fun createPolyline(coordinates: List<GeoCoordinate>): MKPolyline? {
    if (coordinates.size < 2) return null
    return memScoped {
        val coords = allocArray<CLLocationCoordinate2D>(coordinates.size)
        coordinates.forEachIndexed { index, coord ->
            coords[index].latitude = coord.latitude
            coords[index].longitude = coord.longitude
        }
        MKPolyline.polylineWithCoordinates(coords, coordinates.size.toULong())
    }
}

@Composable
fun IosLiveMap(
    state: LiveMapUiState,
    modifier: Modifier = Modifier
) {
    val delegate = remember { MapViewDelegate() }
    val mapView = remember {
        MKMapView().apply {
            mapType = MKMapTypeStandard
            showsUserLocation = false // MapKit is a renderer only; location pipeline is driven by WalkMark
            this.delegate = delegate
        }
    }

    val startAnnotation = remember(mapView) { StartPointAnnotation().apply { setTitle("Start") } }
    var startAnnotationAdded by remember(mapView) { mutableStateOf(false) }

    val currentPositionAnnotation = remember(mapView) { CurrentPositionAnnotation().apply { setTitle("Current Position") } }
    var currentPositionAnnotationAdded by remember(mapView) { mutableStateOf(false) }

    var currentPolyline by remember(mapView) { mutableStateOf<MKPolyline?>(null) }

    var hasCentered by remember(mapView) { mutableStateOf(false) }
    var handledRecenterId by remember(mapView) { mutableLongStateOf(state.recenterRequestId) }

    // Update Route polyline
    LaunchedEffect(mapView, state.route) {
        val newPolyline = createPolyline(state.route.coordinates)
        currentPolyline?.let { mapView.removeOverlay(it) }
        newPolyline?.let { mapView.addOverlay(it) }
        currentPolyline = newPolyline
    }

    // Update Start Annotation
    LaunchedEffect(mapView, state.startPoint) {
        val start = state.startPoint
        if (start != null) {
            startAnnotation.setCoordinate(CLLocationCoordinate2DMake(start.latitude, start.longitude))
            if (!startAnnotationAdded) {
                mapView.addAnnotation(startAnnotation)
                startAnnotationAdded = true
            }
        } else {
            if (startAnnotationAdded) {
                mapView.removeAnnotation(startAnnotation)
                startAnnotationAdded = false
            }
        }
    }

    // Update Current Position Annotation
    LaunchedEffect(mapView, state.currentPosition) {
        val currentPos = state.currentPosition
        if (currentPos != null) {
            currentPositionAnnotation.setCoordinate(CLLocationCoordinate2DMake(currentPos.latitude, currentPos.longitude))
            if (!currentPositionAnnotationAdded) {
                mapView.addAnnotation(currentPositionAnnotation)
                currentPositionAnnotationAdded = true
            }
        } else {
            if (currentPositionAnnotationAdded) {
                mapView.removeAnnotation(currentPositionAnnotation)
                currentPositionAnnotationAdded = false
            }
        }
    }

    // Explicit Recenter & Initial Center
    LaunchedEffect(mapView, state.currentPosition, state.recenterRequestId) {
        val position = state.currentPosition ?: return@LaunchedEffect
        if (!hasCentered || handledRecenterId != state.recenterRequestId) {
            val center = CLLocationCoordinate2DMake(position.latitude, position.longitude)
            val region = MKCoordinateRegionMakeWithDistance(center, 500.0, 500.0)
            mapView.setRegion(region, animated = true)
            hasCentered = true
            handledRecenterId = state.recenterRequestId
        }
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier
    )
}