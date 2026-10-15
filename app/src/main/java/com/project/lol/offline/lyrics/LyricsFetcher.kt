package com.project.lol.offline.lyrics

import com.project.lol.innertube.YouTube
import com.project.lol.innertube.models.response.LyricsData
import com.project.lol.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.project.lol.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.project.lol.spicy.SpicyLyricsClient
import com.project.lol.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Resolves lyrics for a downloaded track and returns them as LRC.
 *
 * Two sources, keyed by the two ids the download already knows:
 *  - YouTube Music: `next` -> Lyrics tab browseId -> `browse`. The ANDROID_MUSIC client returns
 *    per-line millisecond cues; WEB_REMIX only plain text.
 *  - Spotify: [SpicyLyricsClient] (api.spicylyrics.org, LRCLIB fallback), keyed by the trackId.
 *
 * A miss is never an error - lyrics are optional and must not fail a download.
 */
object LyricsFetcher {
    private const val TAG = "Spl-DL"

    data class Result(
        val lrc: String,
        val source: String,
        val synced: Boolean,
        val lineCount: Int,
    )

    suspend fun fetch(
        videoId: String?,
        trackId: String?,
        title: String,
        artist: String,
        durationSec: Int,
        source: LyricsSource,
    ): Result? = withContext(Dispatchers.IO) {
        when (source) {
            LyricsSource.YOUTUBE -> youtube(videoId)
            LyricsSource.SPOTIFY -> spotify(trackId, title, artist, durationSec)
            LyricsSource.AUTO -> youtube(videoId) ?: spotify(trackId, title, artist, durationSec)
        }
    }

    private suspend fun youtube(videoId: String?): Result? {
        if (videoId.isNullOrBlank()) return null
        val browseId = YouTube.lyricsBrowseId(videoId).getOrNull() ?: run {
            Logger.d(TAG, "lyrics: no Lyrics tab for videoId=$videoId")
            return null
        }
        val timed = YouTube.timedLyrics(browseId).getOrNull()
        if (timed != null) {
            val lines = fromTimed(timed)
            if (lines.isNotEmpty()) {
                return Result(Lrc.format(lines), "YouTube Music", synced = true, lineCount = lines.size)
            }
        }
        val plain = YouTube.staticLyrics(browseId).getOrNull() ?: return null
        val lines = fromStatic(plain)
        if (lines.isEmpty()) return null
        Logger.d(TAG, "lyrics: static fallback for videoId=$videoId (${lines.size} lines)")
        return Result(Lrc.format(lines), "YouTube Music", synced = false, lineCount = lines.size)
    }

    private fun spotify(
        trackId: String?,
        title: String,
        artist: String,
        durationSec: Int,
    ): Result? {
        if (trackId.isNullOrBlank() || trackId.length != 22) return null
        val payload = runCatching {
            SpicyLyricsClient.fetch(trackId, title.trim(), artist.trim(), durationSec)
        }.getOrNull() ?: run {
            Logger.d(TAG, "lyrics: no Spotify payload for trackId=$trackId")
            return null
        }
        val parsed = fromSpicy(payload) ?: return null
        return Result(
            Lrc.format(parsed.first, durationSec.takeIf { it > 0 }?.times(1000L)),
            "Spotify",
            synced = parsed.second,
            lineCount = parsed.first.size,
        )
    }

    private fun fromTimed(lines: List<LyricsData.TimedLine>): List<LrcLine> {
        val out = ArrayList<LrcLine>(lines.size)
        for (line in lines) {
            val text = line.lyricLine?.trim().orEmpty()
            if (text.isEmpty()) continue
            val cue = line.cueRange ?: continue
            val start = cue.startTimeMilliseconds?.toLongOrNull() ?: continue
            val end = cue.endTimeMilliseconds?.toLongOrNull() ?: start
            if (end < start) continue
            out.add(LrcLine(start, end, text))
        }
        return out
    }

    private fun fromStatic(text: String): List<LrcLine> =
        text.split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { LrcLine(0L, 0L, it) }

    /** @return lines + whether the payload carries usable timing. */
    private fun fromSpicy(payload: String): Pair<List<LrcLine>, Boolean>? {
        val root = runCatching { JSONObject(payload) }.getOrNull() ?: return null
        val lyrics = root.optJSONObject("lyrics") ?: return null
        val arr = lyrics.optJSONArray("lines") ?: return null
        val syncType = lyrics.optString("syncType", "")
        val out = ArrayList<LrcLine>(arr.length())
        for (i in 0 until arr.length()) {
            val line = arr.optJSONObject(i) ?: continue
            val text = line.optString("words", "").trim()
            if (text.isEmpty()) continue
            val start = line.optString("startTimeMs", "0").toLongOrNull() ?: 0L
            val end = line.optString("endTimeMs", "0").toLongOrNull() ?: 0L
            out.add(LrcLine(start, end, text))
        }
        if (out.isEmpty()) return null
        return out to (syncType != "UNSYNCED" && out.any { it.startMs > 0 })
    }
}
