package com.project.lol.offline.lyrics

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.project.lol.offline.DownloadFolder
import com.project.lol.offline.DownloadPrefs
import com.project.lol.util.Logger
import java.io.File

/**
 * Local lyrics cache, shaped like the cover cache: `filesDir/lyrics/<trackId>.lrc`.
 *
 * The cache is what Offline Mode reads for its native viewer, so it is always written when the
 * download lyrics toggle is on. The sidecar next to the audio file is an explicit opt-in export
 * and is best effort - losing it never fails a download.
 */
object LyricsStore {
    private const val TAG = "Spl-DL"
    private const val DIR = "lyrics"
    private const val MIME = "text/plain"

    private fun dir(context: Context): File = File(context.filesDir, DIR).apply { mkdirs() }

    private fun cacheFile(context: Context, trackId: String): File =
        File(dir(context), "$trackId.lrc")

    fun write(context: Context, trackId: String, lrc: String): Boolean = runCatching {
        if (lrc.isBlank()) return@runCatching false
        cacheFile(context, trackId).writeText(lrc)
        true
    }.getOrElse {
        Logger.w(TAG, "lyrics: cache write failed for $trackId: ${it.message}")
        false
    }

    fun read(context: Context, trackId: String): String? =
        runCatching { cacheFile(context, trackId) }
            .getOrNull()
            ?.takeIf { it.exists() && it.length() > 0 }
            ?.let { runCatching { it.readText() }.getOrNull() }

    fun delete(context: Context, trackId: String) {
        runCatching { cacheFile(context, trackId).delete() }
    }

    fun parsed(context: Context, trackId: String): ParsedLrc? {
        val text = read(context, trackId) ?: return null
        return runCatching { Lrc.parse(text) }.getOrNull()
    }

    /** `<artist> - <title> [<trackId>].lrc`, sanitised the same way the audio file name is. */
    fun sidecarName(artist: String, title: String, trackId: String): String =
        "$artist - $title [$trackId]"
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .let { if (it.length > 200) it.take(200) else it } + ".lrc"

    /**
     * Writes the sidecar next to the audio. Mirrors the destination logic of the audio save:
     * the picked SAF folder when present, MediaStore on Q+, and the public Music folder below.
     */
    fun writeSidecar(context: Context, fileName: String, lrc: String): Boolean {
        if (lrc.isBlank()) return false

        val picked = DownloadPrefs.folder(context)
        if (picked != null && DownloadFolder.hasAccess(context, picked)) {
            val tmp = File(context.cacheDir, "lyrics_sidecar.lrc")
            runCatching {
                tmp.writeText(lrc)
                val uri = DownloadFolder.create(context, picked, fileName, MIME, tmp)
                if (uri != null) return true
                Logger.w(TAG, "lyrics: sidecar create failed in the picked folder")
            }.onFailure {
                Logger.w(TAG, "lyrics: sidecar write failed: ${it.message}")
            }
            runCatching { tmp.delete() }
        } else if (picked != null) {
            Logger.w(TAG, "lyrics: no access to the picked folder, skipping sidecar")
        }

        val relative = "${Environment.DIRECTORY_MUSIC}/${DownloadPrefs.subfolder(context)}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, MIME)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relative)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = runCatching { context.contentResolver.insert(collection, values) }.getOrNull()
                ?: run {
                    Logger.w(TAG, "lyrics: MediaStore insert returned null for $fileName")
                    return false
                }
            return try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(lrc.toByteArray(Charsets.UTF_8))
                } ?: run {
                    Logger.w(TAG, "lyrics: openOutputStream returned null for $fileName")
                    context.contentResolver.delete(uri, null, null)
                    return false
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                true
            } catch (e: Exception) {
                Logger.w(TAG, "lyrics: sidecar MediaStore write failed: ${e.message}")
                runCatching { context.contentResolver.delete(uri, null, null) }
                false
            }
        }

        return runCatching {
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                DownloadPrefs.subfolder(context)).apply { mkdirs() }
            File(folder, fileName).writeText(lrc)
            true
        }.getOrElse {
            Logger.w(TAG, "lyrics: sidecar write to $relative failed: ${it.message}")
            false
        }
    }

    /** Removes a sidecar, if one was written alongside the audio. Best effort. */
    fun deleteSidecar(context: Context, fileName: String) {
        val relative = "${Environment.DIRECTORY_MUSIC}/${DownloadPrefs.subfolder(context)}/"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                context.contentResolver.delete(
                    collection,
                    "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                    arrayOf(fileName, relative),
                )
            }
            return
        }
        runCatching {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "$relative$fileName",
            ).delete()
        }
    }
}
