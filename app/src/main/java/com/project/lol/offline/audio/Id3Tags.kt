package com.project.lol.offline.audio

import com.project.lol.util.Logger
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Minimal ID3v2.4 writer. LAME writes bare MPEG frames, so the tag is prepended to the audio;
 * an existing ID3v2 header and an ID3v1 trailer are dropped while copying, which keeps
 * re-tagging idempotent.
 */
internal object Id3Tags {
    private const val TAG = "Spl-DL"
    private const val HEADER_SIZE = 10
    private const val ID3V1_SIZE = 128
    private const val COPY_BUFFER_SIZE = 64 * 1024
    private const val UTF8 = 3
    private const val PICTURE_TYPE_COVER_FRONT = 3

    fun writeTags(
        file: File,
        title: String,
        artist: String,
        album: String,
        cover: ByteArray?,
        trackNumber: Int? = null,
        discNumber: Int? = null,
        year: Int? = null,
    ): Boolean {
        val audioStart = existingTagSize(file)
        val audioEnd = file.length() - id3v1Size(file)
        if (audioEnd <= audioStart) return false

        val temp = File(file.parentFile, "${file.name}.tagged")
        try {
            BufferedOutputStream(FileOutputStream(temp)).use { out ->
                out.write(buildTag(title, artist, album, cover, trackNumber, discNumber, year))
                BufferedInputStream(FileInputStream(file)).use { input ->
                    var toSkip = audioStart.toLong()
                    while (toSkip > 0) {
                        val skipped = input.skip(toSkip)
                        if (skipped <= 0) break
                        toSkip -= skipped
                    }
                    var remaining = audioEnd - audioStart
                    val buffer = ByteArray(COPY_BUFFER_SIZE)
                    while (remaining > 0) {
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                        if (read <= 0) break
                        out.write(buffer, 0, read)
                        remaining -= read
                    }
                }
            }
            if (!temp.renameTo(file)) {
                if (file.delete() && temp.renameTo(file)) return true
                temp.delete()
                return false
            }
            return true
        } catch (t: Throwable) {
            Logger.w(TAG, "id3: failed for ${file.name}: ${t.message}")
            runCatching { temp.delete() }
            return false
        }
    }

    private fun buildTag(
        title: String,
        artist: String,
        album: String,
        cover: ByteArray?,
        trackNumber: Int?,
        discNumber: Int?,
        year: Int?,
    ): ByteArray {
        val frames = ByteArrayOutputStream()
        writeTextFrame(frames, "TIT2", title)
        writeTextFrame(frames, "TPE1", artist)
        writeTextFrame(frames, "TALB", album)
        if (trackNumber != null && trackNumber > 0) writeTextFrame(frames, "TRCK", trackNumber.toString())
        if (discNumber != null && discNumber > 0) writeTextFrame(frames, "TPOS", discNumber.toString())
        if (year != null && year > 0) writeTextFrame(frames, "TDRC", year.toString())
        if (cover != null && cover.isNotEmpty()) writePictureFrame(frames, cover)

        val body = frames.toByteArray()
        val tag = ByteArray(HEADER_SIZE + body.size)
        tag[0] = 'I'.code.toByte()
        tag[1] = 'D'.code.toByte()
        tag[2] = '3'.code.toByte()
        tag[3] = 4
        tag[4] = 0
        tag[5] = 0
        writeSyncSafe(tag, 6, body.size)
        System.arraycopy(body, 0, tag, HEADER_SIZE, body.size)
        return tag
    }

    private fun writeTextFrame(out: ByteArrayOutputStream, id: String, value: String) {
        if (value.isBlank()) return
        val text = value.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(text.size + 1)
        payload[0] = UTF8.toByte()
        System.arraycopy(text, 0, payload, 1, text.size)
        writeFrame(out, id, payload)
    }

    private fun writePictureFrame(out: ByteArrayOutputStream, cover: ByteArray) {
        val payload = ByteArrayOutputStream()
        payload.write(UTF8)
        payload.write(coverMime(cover).toByteArray(Charsets.US_ASCII))
        payload.write(0)
        payload.write(PICTURE_TYPE_COVER_FRONT)
        payload.write(0)
        payload.write(cover)
        writeFrame(out, "APIC", payload.toByteArray())
    }

    private fun writeFrame(out: ByteArrayOutputStream, id: String, payload: ByteArray) {
        out.write(id.toByteArray(Charsets.US_ASCII))
        val size = ByteArray(4)
        writeSyncSafe(size, 0, payload.size)
        out.write(size)
        out.write(0)
        out.write(0)
        out.write(payload)
    }

    private fun writeSyncSafe(target: ByteArray, offset: Int, value: Int) {
        target[offset] = ((value ushr 21) and 0x7F).toByte()
        target[offset + 1] = ((value ushr 14) and 0x7F).toByte()
        target[offset + 2] = ((value ushr 7) and 0x7F).toByte()
        target[offset + 3] = (value and 0x7F).toByte()
    }

    private fun coverMime(cover: ByteArray): String = when {
        cover.size >= 8 && cover[0] == 0x89.toByte() && cover[1] == 0x50.toByte() -> "image/png"
        cover.size >= 12 && cover[8] == 'W'.code.toByte() && cover[9] == 'E'.code.toByte() ->
            "image/webp"
        else -> "image/jpeg"
    }

    private fun readSyncSafe(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)

    private fun existingTagSize(file: File): Int {
        if (file.length() < HEADER_SIZE) return 0
        val header = ByteArray(HEADER_SIZE)
        FileInputStream(file).use { input ->
            if (input.read(header) != HEADER_SIZE) return 0
        }
        if (header[0] != 'I'.code.toByte() ||
            header[1] != 'D'.code.toByte() ||
            header[2] != '3'.code.toByte()
        ) {
            return 0
        }
        if ((header[3].toInt() and 0xFF) >= 0xFF) return 0
        return HEADER_SIZE + readSyncSafe(header, 6)
    }

    private fun id3v1Size(file: File): Int {
        if (file.length() < ID3V1_SIZE) return 0
        val trailer = ByteArray(3)
        runCatching {
            java.io.RandomAccessFile(file, "r").use { raf ->
                raf.seek(file.length() - ID3V1_SIZE)
                if (raf.read(trailer) != 3) return 0
            }
        }.getOrElse { return 0 }
        val isTag = trailer[0] == 'T'.code.toByte() &&
            trailer[1] == 'A'.code.toByte() &&
            trailer[2] == 'G'.code.toByte()
        return if (isTag) ID3V1_SIZE else 0
    }
}
