package com.project.lol.offline

import android.content.Context
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class IntegrityIssue(val damaged: Boolean) {
    MISSING_FILE(true),
    EMPTY_FILE(true),
    UNREADABLE(true),
    MISSING_TITLE(false),
    MISSING_ARTIST(false),
    MISSING_ALBUM(false),
    MISSING_YEAR(false),
    MISSING_COVER(false)
}

data class SongIntegrity(val song: OfflineSong, val issues: Set<IntegrityIssue>) {
    val damaged: Boolean get() = issues.any { it.damaged }
    val incompleteTags: Boolean get() = issues.any { !it.damaged }
}

data class IntegrityReport(val checked: Int, val entries: List<SongIntegrity>) {
    val damaged: List<SongIntegrity> get() = entries.filter { it.damaged }
    val incompleteTags: List<SongIntegrity> get() = entries.filter { !it.damaged && it.incompleteTags }
    val clean: Boolean get() = entries.isEmpty()
}

object LibraryIntegrity {

    suspend fun scan(
        context: Context,
        songs: List<OfflineSong>,
        onProgress: ((done: Int, total: Int) -> Unit)? = null,
    ): IntegrityReport = withContext(Dispatchers.IO) {
        val entries = ArrayList<SongIntegrity>()
        songs.forEachIndexed { index, song ->
            val issues = inspect(context, song)
            if (issues.isNotEmpty()) entries.add(SongIntegrity(song, issues))
            onProgress?.invoke(index + 1, songs.size)
        }
        IntegrityReport(songs.size, entries)
    }

    private fun inspect(context: Context, song: OfflineSong): Set<IntegrityIssue> {
        val issues = linkedSetOf<IntegrityIssue>()
        if (song.title.isBlank()) issues.add(IntegrityIssue.MISSING_TITLE)
        if (song.artist.isBlank() && song.ytArtist.isBlank()) issues.add(IntegrityIssue.MISSING_ARTIST)
        if (song.album.isBlank() && song.ytAlbum.isBlank()) issues.add(IntegrityIssue.MISSING_ALBUM)
        if (song.year == null) issues.add(IntegrityIssue.MISSING_YEAR)
        if (song.coverFile == null) issues.add(IntegrityIssue.MISSING_COVER)
        when (fileState(context, song)) {
            FileState.MISSING -> issues.add(IntegrityIssue.MISSING_FILE)
            FileState.EMPTY -> issues.add(IntegrityIssue.EMPTY_FILE)
            FileState.UNREADABLE -> issues.add(IntegrityIssue.UNREADABLE)
            FileState.OK -> Unit
        }
        return issues
    }

    private enum class FileState { OK, MISSING, EMPTY, UNREADABLE }

    private fun fileState(context: Context, song: OfflineSong): FileState {
        if (song.uri.scheme == "file") {
            val file = song.uri.path?.let { File(it) } ?: return FileState.MISSING
            if (!file.exists()) return FileState.MISSING
            if (file.length() == 0L) return FileState.EMPTY
            return if (isDecodable(context, song)) FileState.OK else FileState.UNREADABLE
        }
        val length = runCatching {
            context.contentResolver.openAssetFileDescriptor(song.uri, "r")?.use { it.length }
        }.getOrNull()
        if (length != null && length > 0L) {
            return if (isDecodable(context, song)) FileState.OK else FileState.UNREADABLE
        }
        if (length == 0L) return FileState.EMPTY
        val readable = runCatching {
            context.contentResolver.openInputStream(song.uri)?.use { it.read() != -1 } ?: false
        }.getOrDefault(false)
        if (!readable) return FileState.MISSING
        return if (isDecodable(context, song)) FileState.OK else FileState.UNREADABLE
    }

    private fun isDecodable(context: Context, song: OfflineSong): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            if (song.uri.scheme == "file") {
                val path = song.uri.path ?: return false
                retriever.setDataSource(path)
            } else {
                val opened = context.contentResolver.openFileDescriptor(song.uri, "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)
                    true
                } ?: false
                if (!opened) return false
            }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            duration > 0L
        } catch (_: Exception) {
            false
        } finally {
            runCatching { retriever.release() }
        }
    }
}
