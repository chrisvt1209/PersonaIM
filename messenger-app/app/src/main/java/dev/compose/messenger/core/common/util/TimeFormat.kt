package dev.compose.messenger.core.common.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// Accepts "yyyy-MM-dd'T'HH:mm:ss", an optional fractional-seconds part of any digit count,
// and an optional "Z" / "+HH:mm" / "+HHmm" offset. Fraction digit count varies by backend
// (millis vs. micros), which is exactly what tripped up matching a fixed-width pattern before.
private val TIMESTAMP_PATTERN = Regex(
    """^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2})(?:\.\d+)?\s*(Z|[+-]\d{2}:?\d{2})?$"""
)

/** Formats a server timestamp (stored in UTC) into a short "HH:mm" label in the device's local time zone. */
fun formatMessageTimestamp(rawTimestamp: String): String {
    val groups = TIMESTAMP_PATTERN.find(rawTimestamp.trim())?.groupValues ?: return rawTimestamp

    val sourceZone = when (val offset = groups[7]) {
        "", "Z" -> TimeZone.getTimeZone("UTC")
        else -> {
            val normalized = if (offset.contains(':')) offset else "${offset.take(3)}:${offset.substring(3)}"
            TimeZone.getTimeZone("GMT$normalized")
        }
    }

    val instantMillis = Calendar.getInstance(sourceZone).apply {
        clear()
        set(groups[1].toInt(), groups[2].toInt() - 1, groups[3].toInt(), groups[4].toInt(), groups[5].toInt(), groups[6].toInt())
    }.timeInMillis

    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()
    return outputFormat.format(instantMillis)
}
