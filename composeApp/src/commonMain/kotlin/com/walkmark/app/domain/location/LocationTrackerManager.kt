package com.walkmark.app.domain.location

import kotlinx.coroutines.flow.Flow

expect class LocationTrackerManager {
    val rawLocationUpdates: Flow<LocationPoint>
    fun start()
    fun stop()
}
