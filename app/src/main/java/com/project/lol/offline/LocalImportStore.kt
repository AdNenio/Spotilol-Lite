package com.project.lol.offline

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.project.lol.util.Logger
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

object LocalImportStore {
    private const val TAG = "Spl-DL"
    private const val DIR_NAME = "imported"
    private const val MANIFEST = "imported_meta.json"
    private const val MAX_FILE_NAME = 120
    private val HEX = "0123456789abcdef".toCharArray()

    const val ID_PREFIX = "imp-spotilol-"

    data class Summary(val imported: Int, val duplicates: Int, val failed: Int)

    private class Meta(
        val title: String,
        val artist: String,
        val album: String,
        val durationSec: Int,
        val cover: ByteArray?,
        val year: Int?,
    )

    fun isImported(song: OfflineSong): Boolean = song.id.startsWith(ID_PREFIX)
    fun importFiles(
        context: Context,
        uris: List<Uri>,
        onProgress: ((done: Int, total: Int, name: String) -> Unit)? = null,
    ): Summary {
        val manifest = readManifest(context)
        val knownKeys = HashSet<String>(manifest.length() * 2)
        val keyIter = manifest.keys()
        while (keyIter.hasNext()) {
            val id = keyIter.next()
            manifest.optJSONObject(id)?.optString("key")?.takeIf { it.isNotBlank() }?.let { knownKeys.add(it) }
        }

        val dir = importDir(context)
        val usedNames = dir.list()?.toHashSet() ?: HashSet()
        var imported = 0
        var duplicates = 0
        var failed = 0

        uris.forEachIndexed { index, uri ->
            val displayName = queryDisplayName(context, uri)
                ?: "import_${System.currentTimeMillis()}_$index"
            onProgress?.invoke(index + 1, uris.size, displayName)
            try {
                val size = querySize(context, uri)
                val key = "$displayName:$size"
                if (size >= 0 && knownKeys.contains(key)) {
                    duplicates++
                    return@forEachIndexed
                }

                val target = uniqueTarget(dir, displayName, usedNames)
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw IllegalStateException("openInputStream returned null")
                }.getOrElse {
                    Logger.w(TAG, "import: copy failed for $displayName: ${it.message}")
                    runCatching { target.delete() }
                    failed++
                    return@forEachIndexed
                }

                val meta = probe(target)
                if (meta == null) {
                    Logger.w(TAG, "import: not playable audio, dropping $displayName")
                    runCatching { target.delete() }
                    failed++
                    return@forEachIndexed
                }

                val id = ID_PREFIX + hash(key)
                manifest.put(
                    id,
                    JSONObject().apply {
                        put("file", target.name)
                        put("title", meta.title)
                        put("artist", meta.artist)
                        put("album", meta.album)
                        put("durationSec", meta.durationSec)
                        meta.year?.let { put("year", it) }
                        put("key", key)
                        put("addedAt", System.currentTimeMillis())
                    }
                )
                writeManifest(context, manifest)
                knownKeys.add(key)
                usedNames.add(target.name)
                saveCover(context, id, meta.cover)
                imported++
            } catch (t: Throwable) {
                Logger.w(TAG, "import: failed for $displayName: ${t.message}")
                failed++
            }
        }
        onProgress?.invoke(uris.size, uris.size, "")
        return Summary(imported, duplicates, failed)
    }
    fun loadSongs(context: Context): List<OfflineSong> {
        val manifest = readManifest(context)
        val dir = importDir(context)
        val entries = ArrayList<Triple<Long, String, JSONObject>>(manifest.length())
        val ids = manifest.keys()
        while (ids.hasNext()) {
            val id = ids.next()
            val o = manifest.optJSONObject(id) ?: continue
            entries.add(Triple(o.optLong("addedAt", 0L), id, o))
        }
        entries.sortByDescending { it.first }
        val songs = ArrayList<OfflineSong>(entries.size)
        for ((_, id, o) in entries) {
            val file = File(dir, o.optString("file"))
            if (!file.isFile || file.length() == 0L) continue
            songs.add(
                OfflineSong(
                    id = id,
                    title = o.optString("title").ifBlank { file.nameWithoutExtension },
                    artist = o.optString("artist"),
                    uri = Uri.fromFile(file),
                    coverFile = coverFile(context, id),
                    album = o.optString("album"),
                    durationSec = o.optInt("durationSec", 0).takeIf { it > 0 },
                    year = o.optInt("year", 0).takeIf { it > 0 },
                    format = file.extension.lowercase(),
                )
            )
        }
        return songs
    }

    fun deleteSong(context: Context, songId: String): Boolean {
        val manifest = readManifest(context)
        val entry = manifest.optJSONObject(songId) ?: return true
        var ok = true
        val file = File(importDir(context), entry.optString("file"))
        if (file.exists()) ok = runCatching { file.delete() }.isSuccess
        manifest.remove(songId)
        writeManifest(context, manifest)
        runCatching { File(coverDir(context), "$songId.jpg").delete() }
        return ok
    }

    private fun probe(file: File): Meta? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: -1L
            if (durationMs <= 0) null
            else Meta(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()
                    .orEmpty().ifBlank { file.nameWithoutExtension },
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.trim().orEmpty(),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.trim().orEmpty(),
                durationSec = ((durationMs + 500) / 1000).toInt(),
                cover = retriever.embeddedPicture,
                year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?.toIntOrNull()?.takeIf { it > 0 },
            )
        } catch (e: Exception) {
            Logger.w(TAG, "import: probe failed for ${file.name}: ${e.message}")
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0)?.takeIf { it.isNotBlank() } else null }
        }.getOrNull()

    private fun querySize(context: Context, uri: Uri): Long =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else -1L }
                ?: -1L
        }.getOrDefault(-1L)

    private fun uniqueTarget(dir: File, displayName: String, used: Set<String>): File {
        val clean = sanitize(displayName)
        val dot = clean.lastIndexOf('.')
        val base = if (dot > 0) clean.substring(0, dot) else clean
        val ext = if (dot > 0) clean.substring(dot) else ""
        var candidate = clean
        var n = 1
        while (used.contains(candidate) || File(dir, candidate).exists()) {
            n++
            candidate = "$base ($n)$ext"
        }
        return File(dir, candidate)
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("""[\\/:*?"<>|]"""), "_")
            .trim()
            .take(MAX_FILE_NAME)
            .ifBlank { "import_${System.currentTimeMillis()}" }

    private fun saveCover(context: Context, id: String, cover: ByteArray?) {
        if (cover == null || cover.isEmpty()) return
        val target = File(coverDir(context), "$id.jpg")
        if (target.exists()) return
        runCatching { target.writeBytes(cover) }
            .onFailure { runCatching { target.delete() } }
    }

    private fun coverFile(context: Context, id: String): File? =
        File(coverDir(context), "$id.jpg").takeIf { it.exists() && it.length() > 0 }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) {
            val v = b.toInt() and 0xFF
            sb.append(HEX[v ushr 4]).append(HEX[v and 0x0F])
        }
        return sb.toString().take(16)
    }

    private fun importDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { mkdirs() }

    private fun coverDir(context: Context): File =
        File(context.filesDir, "covers").apply { mkdirs() }

    private fun readManifest(context: Context): JSONObject =
        runCatching {
            val f = File(context.filesDir, MANIFEST)
            if (f.exists()) JSONObject(f.readText()) else JSONObject()
        }.getOrDefault(JSONObject())

    private fun writeManifest(context: Context, root: JSONObject) {
        runCatching { File(context.filesDir, MANIFEST).writeText(root.toString()) }
            .onFailure { Logger.w(TAG, "import: manifest write failed: ${it.message}") }
    }
}