package com.walkmark.app.domain.export

import com.walkmark.app.domain.location.LocationPoint
import com.walkmark.app.domain.walk.Walk

/**
 * Serialises a persisted [Walk] and its persisted [LocationPoint]s into GPX 1.1.
 *
 * The generator is pure and provider-neutral: no platform types, no clock, no I/O.
 * It never manufactures data. Point ordering is exactly the order supplied by the
 * caller, which is the persisted repository order. Missing optional source values
 * cause the corresponding GPX element to be omitted rather than defaulted.
 */
object GpxDocumentGenerator {

    private const val GPX_VERSION = "1.1"
    private const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"
    private const val GPX_SCHEMA_LOCATION =
        "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"
    private const val CREATOR = "WalkMark"

    /**
     * A non-positive epoch value is not a usable fix time, so [time] is omitted
     * rather than serialised as 1970 or converted to a local timezone string.
     */
    fun toUtcTimestamp(epochMillis: Long): String? =
        if (epochMillis <= 0L) null else Iso8601.formatUtc(epochMillis)

    fun generate(walk: Walk, points: List<LocationPoint>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        append("<gpx version=\"").append(GPX_VERSION)
        append("\" creator=\"").append(escape(CREATOR))
        append("\" xmlns=\"").append(GPX_NAMESPACE)
        append("\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
        append(" xsi:schemaLocation=\"").append(GPX_SCHEMA_LOCATION)
        append("\">\n")

        append("  <metadata>\n")
        append("    <name>").append(escape(walk.title)).append("</name>\n")
        walk.summary?.let { summary ->
            append("    <desc>").append(escape(summary)).append("</desc>\n")
        }
        toUtcTimestamp(walk.startTimeEpochMs)?.let { time ->
            append("    <time>").append(time).append("</time>\n")
        }
        append("  </metadata>\n")

        append("  <trk>\n")
        append("    <name>").append(escape(walk.title)).append("</name>\n")
        append("    <trkseg>\n")
        for (point in points) {
            append("      <trkpt lat=\"").append(formatCoordinate(point.latitude))
            append("\" lon=\"").append(formatCoordinate(point.longitude))
            append("\">\n")
            point.altitude?.let { altitude ->
                append("        <ele>").append(formatCoordinate(altitude)).append("</ele>\n")
            }
            toUtcTimestamp(point.timestamp)?.let { time ->
                append("        <time>").append(time).append("</time>\n")
            }
            append("      </trkpt>\n")
        }
        append("    </trkseg>\n")
        append("  </trk>\n")
        append("</gpx>\n")
    }

    /**
     * Locale-independent numeric serialisation. [Double.toString] is not affected by
     * the default locale, which avoids a decimal-comma defect in generated files.
     * No escaping is applied: coordinates are numeric and never need it.
     */
    private fun formatCoordinate(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0.0"
        return value.toString()
    }

    private fun escape(value: String): String = buildString(value.length) {
        for (character in value) {
            when (character) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(character)
            }
        }
    }
}

/**
 * Minimal UTC ISO-8601 formatting for GPX `<time>` values.
 *
 * Written against `java.time` on the JVM and `kotlinx.datetime`-free arithmetic so it
 * stays in commonMain without adding a dependency. Epoch millis are converted with
 * civil-calendar arithmetic; the result is always UTC and never a local timezone string.
 */
internal object Iso8601 {

    fun formatUtc(epochMillis: Long): String {
        val totalSeconds = Math.floorDiv(epochMillis, 1000L)
        val millis = Math.floorMod(epochMillis, 1000L)
        val epochDay = Math.floorDiv(totalSeconds, 86_400L)
        val secondOfDay = Math.floorMod(totalSeconds, 86_400L)

        val civil = civilFromEpochDay(epochDay)
        val hour = secondOfDay / 3600L
        val minute = (secondOfDay % 3600L) / 60L
        val second = secondOfDay % 60L

        return buildString {
            append(civil.year.toString().padStart(4, '0'))
            append('-')
            append(civil.month.toString().padStart(2, '0'))
            append('-')
            append(civil.day.toString().padStart(2, '0'))
            append('T')
            append(hour.toString().padStart(2, '0'))
            append(':')
            append(minute.toString().padStart(2, '0'))
            append(':')
            append(second.toString().padStart(2, '0'))
            append('.')
            append(millis.toString().padStart(3, '0'))
            append('Z')
        }
    }

    private data class CivilDate(val year: Long, val month: Long, val day: Long)

    /**
     * Howard Hinnant's `civil_from_days`, epoch-shifted to 1970-01-01.
     * Pure integer arithmetic, valid for negative epoch days, no dependency needed.
     */
    private fun civilFromEpochDay(epochDay: Long): CivilDate {
        val shifted = epochDay + 719_468L
        val era = Math.floorDiv(shifted, 146_097L)
        val dayOfEra = Math.floorMod(shifted, 146_097L)
        val yearOfEra =
            (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
        val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
        val monthPrime = (5 * dayOfYear + 2) / 153
        val isShiftedMonth = monthPrime >= 10L
        val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1 + if (isShiftedMonth) 0 else 3
        val month = monthPrime + if (isShiftedMonth) 3 else -9
        val year = yearOfEra + era * 400L + if (month <= 2L) 1 else 0
        return CivilDate(year = year, month = month, day = day)
    }
}
