package com.walkmark.app.domain.location

import kotlinx.serialization.Serializable

@Serializable
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Long,
    val accuracy: Float
)
