package com.walkmark.app.presentation.journal

expect fun formatWalkDateTime(epochMillis: Long): String

fun formatWalkDuration(seconds: Long): String {
    val minutes = seconds.coerceAtLeast(0) / 60
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
}

fun formatWalkDistance(meters: Double): String =
    if (meters >= 1000.0) "${(meters / 100.0).toInt() / 10.0} km" else "${meters.toInt()} m"
