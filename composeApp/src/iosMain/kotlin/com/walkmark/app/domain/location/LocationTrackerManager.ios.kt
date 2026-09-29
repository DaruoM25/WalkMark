package com.walkmark.app.domain.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationAccuracyBest
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLDistanceFilterNone
import platform.darwin.NSObject

actual class LocationTrackerManager {

    private val _rawLocationUpdates = MutableSharedFlow<LocationPoint>(extraBufferCapacity = 64)
    actual val rawLocationUpdates: Flow<LocationPoint> = _rawLocationUpdates.asSharedFlow()

    private var locationManager: CLLocationManager? = null
    private var delegate: CLLocationManagerDelegateProtocol? = null

    private fun setupLocationManager() {
        if (locationManager == null) {
            locationManager = CLLocationManager().apply {
                desiredAccuracy = CLLocationAccuracyBest
                distanceFilter = kCLDistanceFilterNone
                allowsBackgroundLocationUpdates = true
                showsBackgroundLocationIndicator = true
                pausesLocationUpdatesAutomatically = false
            }

            delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    for (loc in didUpdateLocations) {
                        val clLoc = loc as? CLLocation ?: continue
                        val point = LocationPoint(
                            latitude = clLoc.coordinate.latitude,
                            longitude = clLoc.coordinate.longitude,
                            altitude = clLoc.altitude,
                            timestamp = (clLoc.timestamp.timeIntervalSince1970 * 1000).toLong(),
                            accuracy = clLoc.horizontalAccuracy.toFloat()
                        )
                        _rawLocationUpdates.tryEmit(point)
                    }
                }
            }
            locationManager?.delegate = delegate
        }
    }

    actual fun start() {
        setupLocationManager()
        locationManager?.requestWhenInUseAuthorization()
        locationManager?.startUpdatingLocation()
    }

    actual fun stop() {
        locationManager?.stopUpdatingLocation()
    }
}