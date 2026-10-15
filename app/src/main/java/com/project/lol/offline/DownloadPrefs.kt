package com.project.lol.offline

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.project.lol.offline.lyrics.LyricsSource

enum class DownloadFormat(val ext: String, val mime: String) {
    M4A("m4a", "audio/mp4"),
    MP3("mp3", "audio/mpeg");

    companion object {
        fun from(value: String?): DownloadFormat =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: M4A
    }
}

object DownloadPrefs {
    const val PREFS_NAME = "spotilol_prefs"
    private const val KEY_FORMAT = "DlFormat"
    private const val KEY_SUBFOLDER = "DlSubfolder"
    private const val KEY_FOLDER = "DlFolderUri"
    private const val KEY_FOLDER_HISTORY = "DlFolderHistory"
    private const val KEY_TAGS = "DlTags"
    private const val KEY_LYRICS = "DlLyrics"
    private const val KEY_LYRICS_SOURCE = "DlLyricsSource"
    private const val KEY_LYRICS_EMBED = "DlLyricsEmbed"
    private const val KEY_LYRICS_SIDECAR = "DlLyricsSidecar"

    const val DEFAULT_SUBFOLDER = "Spotilol"

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @JvmStatic
    fun format(context: Context): DownloadFormat =
        DownloadFormat.from(prefs(context).getString(KEY_FORMAT, DownloadFormat.M4A.name))

    @JvmStatic
    fun setFormat(context: Context, format: DownloadFormat) {
        prefs(context).edit().putString(KEY_FORMAT, format.name).apply()
    }

    /** Legacy location, still the fallback when no folder has been picked. */
    @JvmStatic
    fun subfolder(context: Context): String =
        prefs(context).getString(KEY_SUBFOLDER, DEFAULT_SUBFOLDER)
            ?.trim()
            ?.replace(Regex("""[/\\]+"""), "-")
            ?.replace(Regex("""[{}^`!#\[\]&$<>=;:"'?,*|]"""), "_")
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_SUBFOLDER

    /** Folder picked through SAF, or null to keep saving under Music/<subfolder>. */
    @JvmStatic
    fun folder(context: Context): Uri? =
        prefs(context).getString(KEY_FOLDER, null)?.takeIf { it.isNotBlank() }?.let(Uri::parse)

    @JvmStatic
    fun setFolder(context: Context, uri: Uri?) {
        val editor = prefs(context).edit()
        if (uri == null) {
            editor.remove(KEY_FOLDER).apply()
            return
        }
        val previous = history(context)
        val history = (listOf(uri.toString()) + previous.filterNot { it == uri.toString() })
        editor.putString(KEY_FOLDER, uri.toString())
            .putString(KEY_FOLDER_HISTORY, history.joinToString("\n"))
            .apply()
    }

    /**
     * Every folder downloads ever went to, newest first. The offline library scans all of them so
     * switching folders never hides the tracks that were already saved.
     */
    @JvmStatic
    fun history(context: Context): List<String> =
        prefs(context).getString(KEY_FOLDER_HISTORY, null)
            ?.split("\n")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    @JvmStatic
    fun knownFolders(context: Context): List<Uri> =
        history(context).mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }

    /** Where downloads go right now, in a form worth showing to the user. */
    @JvmStatic
    fun folderLabel(context: Context): String =
        folder(context)?.let { DownloadFolder.label(it) } ?: "Music/${subfolder(context)}"

    @JvmStatic
    fun folderDisplayPath(context: Context): String =
        folder(context)?.let { DownloadFolder.displayPath(it) }
            ?: "Music/${subfolder(context)}"

    @JvmStatic
    fun writeTags(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TAGS, true)

    @JvmStatic
    fun setWriteTags(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_TAGS, value).apply()
    }

    /** Master switch: fetch lyrics while a track is being downloaded. */
    @JvmStatic
    fun lyrics(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LYRICS, false)

    @JvmStatic
    fun setLyrics(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_LYRICS, value).apply()
    }

    /** Which provider lyrics come from. */
    @JvmStatic
    fun lyricsSource(context: Context): LyricsSource =
        LyricsSource.from(prefs(context).getString(KEY_LYRICS_SOURCE, null))

    @JvmStatic
    fun setLyricsSource(context: Context, value: LyricsSource) {
        prefs(context).edit().putString(KEY_LYRICS_SOURCE, value.id).apply()
    }

    /** Embed the lyrics into the ID3/M4A tags, so any player outside the app can read them. */
    @JvmStatic
    fun lyricsEmbed(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LYRICS_EMBED, true)

    @JvmStatic
    fun setLyricsEmbed(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_LYRICS_EMBED, value).apply()
    }

    /** Also drop a `.lrc` file next to the audio. */
    @JvmStatic
    fun lyricsSidecar(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LYRICS_SIDECAR, false)

    @JvmStatic
    fun setLyricsSidecar(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_LYRICS_SIDECAR, value).apply()
    }
}
