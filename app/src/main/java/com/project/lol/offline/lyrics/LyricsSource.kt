package com.project.lol.offline.lyrics

/** Where the download-time lyrics come from. */
enum class LyricsSource(val id: String) {
    /** Try YouTube Music (timed) first, fall back to Spotify. */
    AUTO("auto"),
    /** YouTube Music only - resolves the video the audio was downloaded from. */
    YOUTUBE("youtube"),
    /** Spotify only - SpicyLyrics with the LRCLIB fallback. */
    SPOTIFY("spotify");

    companion object {
        fun from(value: String?): LyricsSource =
            entries.firstOrNull { it.id == value } ?: AUTO
    }
}
