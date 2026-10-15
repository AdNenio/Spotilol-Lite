package com.project.lol.offline.lyrics

import java.util.Locale

/** One lyric line and the playback window it owns. */
data class LrcLine(
    val startMs: Long,
    val endMs: Long,
    val text: String,
)

/** Result of parsing an LRC (or plain text) payload. */
data class ParsedLrc(
    val lines: List<LrcLine>,
    val synced: Boolean,
    val totalMs: Long?,
)

/**
 * LRC codec.
 *
 * Written form is the standard `[mm:ss.xx]text` (start time only) plus a `[length:]` metadata tag,
 * so the last line's window survives a round trip. Reading derives every end from the next start -
 * that is exact for the InnerTube cues, which are contiguous.
 *
 * A file with no timestamps at all is still accepted and reported as unsynced, which is what the
 * static (plain text) lyrics source produces.
 */
object Lrc {

    private val TIMESTAMP = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val LENGTH_TAG =
        Regex("""\[length:\s*(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""", RegexOption.IGNORE_CASE)

    /** Fallback window for the final line when no `[length:]` and no track duration is known. */
    private const val TAIL_MS = 6_000L

    fun parse(text: String): ParsedLrc {
        if (text.isBlank()) return ParsedLrc(emptyList(), synced = false, totalMs = null)

        val total = LENGTH_TAG.find(text)?.let { match ->
            toMs(match.groupValues[1], match.groupValues[2], match.groupValues[3])
        }

        val timed = ArrayList<Pair<Long, String>>()
        val plain = ArrayList<String>()
        var sawTimestamp = false

        for (raw in text.split('\n')) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            val stamps = TIMESTAMP.findAll(line).toList()
            if (stamps.isEmpty()) {
                // Metadata tags ([ar:], [ti:], ...) start with '[' and are dropped; anything else
                // is treated as an unsynced lyric line.
                if (!line.startsWith("[")) plain.add(line)
                continue
            }
            sawTimestamp = true
            val content = line.substring(stamps.last().range.last + 1).trim()
            for (stamp in stamps) {
                timed.add(
                    toMs(stamp.groupValues[1], stamp.groupValues[2], stamp.groupValues[3]) to content
                )
            }
        }

        if (!sawTimestamp) {
            return ParsedLrc(
                lines = plain.map { LrcLine(0L, 0L, it) },
                synced = false,
                totalMs = total,
            )
        }

        timed.sortBy { it.first }
        val lines = ArrayList<LrcLine>(timed.size)
        for (i in timed.indices) {
            val (start, content) = timed[i]
            val end = when {
                i + 1 < timed.size -> timed[i + 1].first
                total != null && total > start -> total
                else -> start + TAIL_MS
            }
            lines.add(LrcLine(start, end, content))
        }
        return ParsedLrc(lines, synced = true, totalMs = total)
    }

    /**
     * @param durationMs track length, used for the `[length:]` tag and the last line's window.
     */
    fun format(lines: List<LrcLine>, durationMs: Long? = null): String {
        if (lines.isEmpty()) return ""
        val sorted = lines.sortedBy { it.startMs }
        if (sorted.none { it.startMs > 0 }) {
            // Unsynced payload: emit bare text. Timestamping every line 00:00 would make them
            // all claim to be current and the viewer could not pick one.
            return sorted.joinToString("\n") { it.text } + "\n"
        }
        val total = durationMs ?: sorted.last().endMs.takeIf { it > sorted.last().startMs }

        return buildString {
            append("[re:Spotilol]\n")
            if (total != null && total > 0) {
                append("[length:").append(stamp(total)).append("]\n")
            }
            for (line in sorted) {
                append('[').append(stamp(line.startMs)).append(']')
                append(line.text)
                append('\n')
            }
        }
    }

    /** Index of the line owning [positionMs], or -1 when the payload is unsynced/empty. */
    fun activeIndex(parsed: ParsedLrc, positionMs: Long): Int {
        if (!parsed.synced || parsed.lines.isEmpty()) return -1
        val lines = parsed.lines
        var low = 0
        var high = lines.size - 1
        var found = -1
        while (low <= high) {
            val mid = (low + high) / 2
            if (lines[mid].startMs <= positionMs) {
                found = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return found
    }

    private fun stamp(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        val minutes = safe / 60_000L
        val seconds = (safe % 60_000L) / 1_000L
        val hundredths = (safe % 1_000L) / 10L
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
    }

    private fun toMs(minutes: String, seconds: String, fraction: String): Long {
        val min = minutes.toLongOrNull() ?: 0L
        val sec = seconds.toLongOrNull() ?: 0L
        // Two digits are hundredths, three are milliseconds - both appear in the wild.
        val frac = when (fraction.length) {
            1 -> fraction.toLongOrNull()?.times(100L) ?: 0L
            2 -> fraction.toLongOrNull()?.times(10L) ?: 0L
            else -> fraction.take(3).toLongOrNull() ?: 0L
        }
        return min * 60_000L + sec * 1_000L + frac
    }
}
