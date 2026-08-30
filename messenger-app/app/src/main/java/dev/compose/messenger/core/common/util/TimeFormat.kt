package dev.compose.messenger.core.common.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Carries its own offset (e.g. "...Z" or "...+02:00") - trust it, no UTC assumption needed.
private val zonedPatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
)

// No offset in the string - messages are stored in UTC, so assume UTC.
private val naivePatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
)

/** Formats a server timestamp into a short "HH:mm" label in the device's local time zone. */
fun formatMessageTimestamp(rawTimestamp: String): String {
    zonedPatterns.forEach { pattern ->
        parseStrict(pattern, rawTimestamp, zone = null)?.let { return formatLocal(it) }
    }
    naivePatterns.forEach { pattern ->
        parseStrict(pattern, rawTimestamp, zone = TimeZone.getTimeZone("UTC"))?.let { return formatLocal(it) }
    }
    return rawTimestamp
}

private fun parseStrict(pattern: String, raw: String, zone: TimeZone?): Date? {
    val format = SimpleDateFormat(pattern, Locale.US)
    if (zone != null) format.timeZone = zone
    val position = ParsePosition(0)
    val parsed = format.parse(raw, position) ?: return null
    // SimpleDateFormat.parse ignores trailing unmatched text by default; require a full match
    // so an earlier, looser pattern can't silently swallow a zone suffix it doesn't understand.
    return if (position.index == raw.length) parsed else null
}

private fun formatLocal(date: Date): String {
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault()
    return outputFormat.format(date)
}
