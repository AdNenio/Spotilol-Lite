package com.project.lol.offline.audio

import android.graphics.BitmapFactory
import com.project.lol.util.Logger
import com.project.lol.offline.DownloadFormat
import java.io.File

object Tags {
    private const val TAG = "Spl-DL"

    fun writeTags(
        file: File,
        format: DownloadFormat,
        title: String,
        artist: String,
        album: String,
        coverFile: File?,
        trackNumber: Int? = null,
        discNumber: Int? = null,
        year: Int? = null,
    ) {
        runCatching {
            when (format) {
                DownloadFormat.M4A -> writeMp4Tags(file, title, artist, album, coverFile, trackNumber, discNumber, year)
                DownloadFormat.MP3 -> writeMp3Tags(file, title, artist, album, coverFile, trackNumber, discNumber, year)
            }
        }.onFailure {
            Logger.w(TAG, "writeTags: failed for ${file.name}: ${it.message}")
        }
    }

    private fun writeMp4Tags(
        file: File,
        title: String,
        artist: String,
        album: String,
        coverFile: File?,
        trackNumber: Int?,
        discNumber: Int?,
        year: Int?,
    ) {
        val cover = readCoverBytes(coverFile)
        if (Mp4Tags.writeTags(file, title, artist, album, cover, trackNumber, discNumber, year)) return
        Logger.w(TAG, "writeMp4Tags: tag write failed for ${file.name}")
    }

    private fun writeMp3Tags(
        file: File,
        title: String,
        artist: String,
        album: String,
        coverFile: File?,
        trackNumber: Int?,
        discNumber: Int?,
        year: Int?,
    ) {
        if (Id3Tags.writeTags(file, title, artist, album, readCoverBytes(coverFile), trackNumber, discNumber, year)) return
        Logger.w(TAG, "writeMp3Tags: tag write failed for ${file.name}")
    }

    private fun readCoverBytes(coverFile: File?): ByteArray? {
        if (coverFile == null || !coverFile.exists() || coverFile.length() == 0L) return null
        return runCatching {
            val bytes = coverFile.readBytes()
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            if (opts.outWidth > 0) bytes else null
        }.getOrNull()
    }
}
