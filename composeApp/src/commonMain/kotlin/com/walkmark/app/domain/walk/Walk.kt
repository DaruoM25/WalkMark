package com.walkmark.app.domain.walk

enum class WalkStatus {
    ACTIVE,
    COMPLETED
}

data class Walk(
    val id: String,
    val title: String,
    val summary: String?,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long?,
    val totalDistanceMeters: Double,
    val durationSeconds: Long,
    val status: WalkStatus
)
