package com.walkmark.app.presentation.journal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.walkmark.app.presentation.map.MapRouteUiModel

/** Offline, platform-neutral preview of persisted points; no tile or GPS requests. */
@Composable
fun WalkRoutePreview(route: MapRouteUiModel, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier.fillMaxWidth().height(180.dp).testTag("walk_route_preview")) {
        val bounds = route.bounds ?: return@Canvas
        val inset = 16.dp.toPx()
        val width = (size.width - inset * 2).coerceAtLeast(1f)
        val height = (size.height - inset * 2).coerceAtLeast(1f)
        fun point(latitude: Double, longitude: Double): Offset = Offset(
            x = inset + ((longitude - bounds.west) / bounds.longitudeSpan.coerceAtLeast(0.000001)).toFloat() * width,
            y = inset + ((bounds.north - latitude) / bounds.latitudeSpan.coerceAtLeast(0.000001)).toFloat() * height
        )
        val pixels = route.coordinates.map { point(it.latitude, it.longitude) }
        pixels.zipWithNext { a, b -> drawLine(color, a, b, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round) }
        pixels.firstOrNull()?.let { drawCircle(Color(0xFF2E7D32), radius = 6.dp.toPx(), center = it) }
    }
}
