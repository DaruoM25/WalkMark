package com.walkmark.app.domain.export

import com.walkmark.app.domain.walk.Walk
import kotlin.math.roundToLong

/** Formats persisted walk fields only; no route, media path, or database identifier is exported. */
class WalkSummaryGenerator {
    fun generate(walk: Walk, noteCount: Int, photoCount: Int): String = buildString {
        appendLine(walk.title.ifBlank { "Walk" })
        appendLine("Started: ${formatUtc(walk.startTimeEpochMs)}")
        appendLine("Duration: ${formatDuration(walk.durationSeconds)}")
        appendLine("Distance: ${formatDistance(walk.totalDistanceMeters)}")
        walk.summary?.takeIf { it.isNotBlank() }?.let { appendLine("Summary: $it") }
        appendLine("Notes: $noteCount")
        append("Photos: $photoCount")
    }

    private fun formatDuration(seconds: Long): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        val hours = safeSeconds / 3600
        val minutes = safeSeconds % 3600 / 60
        val remainder = safeSeconds % 60
        return if (hours > 0) "${hours}h ${minutes}m ${remainder}s" else "${minutes}m ${remainder}s"
    }

    private fun formatDistance(meters: Double): String =
        "${meters.coerceAtLeast(0.0).roundToLong()} m"

    /** UTC, independent of device timezone and locale. */
    internal fun formatUtc(epochMillis: Long): String {
        val day = floorDiv(epochMillis, 86_400_000L)
        val millisOfDay = epochMillis - day * 86_400_000L
        val hour = millisOfDay / 3_600_000L
        val minute = millisOfDay % 3_600_000L / 60_000L
        val second = millisOfDay % 60_000L / 1_000L
        val (year, month, date) = civilFromDays(day)
        return "${year.toString().padStart(4, '0')}-${month.pad2()}-${date.pad2()} " +
            "${hour.pad2()}:${minute.pad2()}:${second.pad2()} UTC"
    }

    private fun Long.pad2(): String = toString().padStart(2, '0')

    private fun floorDiv(value: Long, divisor: Long): Long {
        val quotient = value / divisor
        return if (value % divisor < 0) quotient - 1 else quotient
    }

    private fun civilFromDays(daysSinceEpoch: Long): Triple<Long, Long, Long> {
        val shifted = daysSinceEpoch + 719468
        val era = floorDiv(shifted, 146097)
        val dayOfEra = shifted - era * 146097
        val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
        var year = yearOfEra + era * 400
        val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
        val monthPrime = (5 * dayOfYear + 2) / 153
        val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
        val month = monthPrime + if (monthPrime < 10) 3 else -9
        if (month <= 2) year++
        return Triple(year, month, day)
    }
}
