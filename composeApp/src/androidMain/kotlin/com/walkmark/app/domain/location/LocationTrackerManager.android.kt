package com.walkmark.app.domain.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.walkmark.app.service.WalkLocationService
import kotlinx.coroutines.flow.Flow

actual class LocationTrackerManager(
    private val context: Context
) {
    actual val rawLocationUpdates: Flow<LocationPoint> = WalkLocationService.rawLocationUpdates

    actual fun start() {
        val intent = Intent(context, WalkLocationService::class.java).apply {
            action = WalkLocationService.ACTION_START
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stop() {
        val intent = Intent(context, WalkLocationService::class.java).apply {
            action = WalkLocationService.ACTION_STOP
        }
        context.startService(intent)
    }
}