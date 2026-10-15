package com.project.lol.offline

import android.content.Context
import com.project.lol.util.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class OfflinePlaylist(
    val id: String,
    val name: String,
    val createdAt: Long,
    val songIds: List<String>,
)

object OfflinePlaylistStore {
    private const val TAG = "Spl-DL"
    private const val FILE_NAME = "offline_playlists.json"

    fun load(context: Context): List<OfflinePlaylist> {
        val root = read(context)
        val out = ArrayList<OfflinePlaylist>(root.length())
        val ids = root.keys()
        while (ids.hasNext()) {
            val id = ids.next()
            val o = root.optJSONObject(id) ?: continue
            val songIds = ArrayList<String>()
            val arr = o.optJSONArray("songIds") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val songId = arr.optString(i, "")
                if (songId.isNotBlank() && !songIds.contains(songId)) songIds.add(songId)
            }
            out.add(
                OfflinePlaylist(
                    id = id,
                    name = o.optString("name").ifBlank { "Playlist" },
                    createdAt = o.optLong("createdAt", 0L),
                    songIds = songIds,
                )
            )
        }
        out.sortByDescending { it.createdAt }
        return out
    }

    fun create(context: Context, name: String, firstSongId: String? = null): OfflinePlaylist {
        val root = read(context)
        val playlist = OfflinePlaylist(
            id = "pl_" + UUID.randomUUID().toString().replace("-", "").take(12),
            name = name.trim().ifBlank { "Playlist" },
            createdAt = System.currentTimeMillis(),
            songIds = listOfNotNull(firstSongId?.takeIf { it.isNotBlank() }),
        )
        root.put(playlist.id, toJson(playlist))
        write(context, root)
        return playlist
    }

    fun rename(context: Context, playlistId: String, newName: String): Boolean {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return false
        val root = read(context)
        val o = root.optJSONObject(playlistId) ?: return false
        o.put("name", trimmed)
        root.put(playlistId, o)
        write(context, root)
        return true
    }

    fun delete(context: Context, playlistId: String) {
        val root = read(context)
        root.remove(playlistId)
        write(context, root)
    }

    fun addSong(context: Context, playlistId: String, songId: String): Boolean {
        val root = read(context)
        val o = root.optJSONObject(playlistId) ?: return false
        val ids = o.optJSONArray("songIds") ?: JSONArray()
        for (i in 0 until ids.length()) {
            if (ids.optString(i) == songId) return false
        }
        ids.put(songId)
        o.put("songIds", ids)
        root.put(playlistId, o)
        write(context, root)
        return true
    }

    fun removeSong(context: Context, playlistId: String, songId: String) {
        val root = read(context)
        val o = root.optJSONObject(playlistId) ?: return
        val ids = o.optJSONArray("songIds") ?: return
        val next = JSONArray()
        for (i in 0 until ids.length()) {
            val current = ids.optString(i, "")
            if (current.isNotBlank() && current != songId) next.put(current)
        }
        o.put("songIds", next)
        root.put(playlistId, o)
        write(context, root)
    }

    private fun toJson(playlist: OfflinePlaylist): JSONObject = JSONObject().apply {
        put("name", playlist.name)
        put("createdAt", playlist.createdAt)
        put("songIds", JSONArray(playlist.songIds))
    }

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    private fun read(context: Context): JSONObject = runCatching {
        val f = file(context)
        if (f.exists()) JSONObject(f.readText()) else JSONObject()
    }.getOrDefault(JSONObject())

    private fun write(context: Context, root: JSONObject) {
        runCatching { file(context).writeText(root.toString()) }
            .onFailure { Logger.w(TAG, "playlists: write failed: ${it.message}") }
    }
}