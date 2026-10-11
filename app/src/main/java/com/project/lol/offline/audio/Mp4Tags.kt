package com.project.lol.offline.audio

import com.project.lol.util.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Mp4Tags {
    private const val TAG = "Spl-DL"
    private const val DATA_TYPE_BINARY = 0
    private const val DATA_TYPE_TEXT = 1
    private const val DATA_TYPE_JPEG = 13
    private const val DATA_TYPE_PNG = 14

    private val OWNED_ITEM_IDS = setOf("©nam", "©ART", "©alb", "covr", "©day", "trkn", "disk")

    private data class Box(val id: String, val offset: Long, val headerSize: Int, val size: Long)

    fun isTaggable(file: File): Boolean = findMoovAtEnd(file) != null

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
        val moov = findMoovAtEnd(file) ?: run {
            Logger.w(TAG, "mp4TagWriter: no trailing moov atom in ${file.name}")
            return false
        }
        val moovChildren = walk(file, moov.offset + moov.headerSize, moov.offset + moov.size) ?: run {
            Logger.w(TAG, "mp4TagWriter: cannot walk moov atoms in ${file.name}")
            return false
        }
        val existingUdta = moovChildren.lastOrNull { it.id == "udta" }
        val existingItems = existingUdta?.let { readIlstItems(file, it) } ?: emptyList()
        val udta = buildUdta(existingItems, title, artist, album, cover, trackNumber, discNumber, year)
        if (udta.isEmpty()) {
            Logger.w(TAG, "mp4TagWriter: nothing to write for ${file.name}")
            return false
        }

        val regionStart = existingUdta?.offset ?: (moov.offset + moov.size)
        val regionEnd = existingUdta?.let { it.offset + it.size } ?: regionStart
        val newMoovSize = moov.size - (regionEnd - regionStart) + udta.size
        if (newMoovSize > 0xFFFFFFFFL) {
            Logger.w(TAG, "mp4TagWriter: moov too large for 32 bit size")
            return false
        }

        val tmp = File(file.parentFile, file.name + ".tagtmp")
        try {
            RandomAccessFile(file, "r").use { input ->
                FileOutputStream(tmp).use { out ->
                    copyRange(input, out, 0, moov.offset)
                    val header = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
                    header.putInt(newMoovSize.toInt())
                    header.put("moov".toByteArray(Charsets.ISO_8859_1))
                    out.write(header.array())
                    val payloadStart = moov.offset + moov.headerSize
                    copyRange(input, out, payloadStart, regionStart - payloadStart)
                    out.write(udta)
                    val moovEnd = moov.offset + moov.size
                    copyRange(input, out, regionEnd, moovEnd - regionEnd)
                }
            }
            if (!tmp.renameTo(file)) {
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
            return true
        } catch (e: Exception) {
            Logger.w(TAG, "mp4TagWriter: failed for ${file.name}: ${e.message}")
            runCatching { tmp.delete() }
            return false
        }
    }

    private fun readIlstItems(file: File, udta: Box): List<Pair<String, ByteArray>> {
        val udtaChildren = walk(file, udta.offset + udta.headerSize, udta.offset + udta.size) ?: return emptyList()
        val meta = udtaChildren.lastOrNull { it.id == "meta" } ?: return emptyList()
        val metaChildren = walk(file, meta.offset + meta.headerSize + 4, meta.offset + meta.size) ?: return emptyList()
        val ilst = metaChildren.lastOrNull { it.id == "ilst" } ?: return emptyList()
        val items = walk(file, ilst.offset + ilst.headerSize, ilst.offset + ilst.size) ?: return emptyList()
        val result = ArrayList<Pair<String, ByteArray>>(items.size)
        for (item in items) {
            if (item.size > 0x7FFFFFFFL) continue
            val bytes = ByteArray(item.size.toInt())
            if (readFully(file, item.offset, bytes)) {
                result += item.id to bytes
            }
        }
        return result
    }

    private fun findMoovAtEnd(file: File): Box? {
        val length = file.length()
        if (length < 16) return null
        val walked = walk(file, 0, length)
        if (walked != null) {
            val moov = walked.lastOrNull { it.id == "moov" }
            if (moov != null && moov.offset + moov.size == length) return moov
        }
        return scanForTrailingMoov(file, length)
    }

    private fun walk(file: File, start: Long, end: Long): List<Box>? {
        val boxes = ArrayList<Box>(8)
        try {
            RandomAccessFile(file, "r").use { raf ->
                var offset = start
                while (offset < end) {
                    if (offset + 8 > end) return null
                    raf.seek(offset)
                    val head = ByteArray(8)
                    raf.readFully(head)
                    val sizeField = ByteBuffer.wrap(head, 0, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xFFFFFFFFL
                    val id = String(head, 4, 4, Charsets.ISO_8859_1)
                    var headerSize = 8
                    var size = sizeField
                    if (sizeField == 1L) {
                        if (offset + 16 > end) return null
                        val large = ByteArray(8)
                        raf.readFully(large)
                        size = ByteBuffer.wrap(large).order(ByteOrder.BIG_ENDIAN).long
                        headerSize = 16
                    } else if (sizeField == 0L) {
                        size = end - offset
                    }
                    if (size < headerSize || offset + size > end) return null
                    boxes += Box(id, offset, headerSize, size)
                    offset += size
                }
                if (offset != end) return null
            }
        } catch (e: Exception) {
            return null
        }
        return boxes
    }

    private fun readFully(file: File, offset: Long, target: ByteArray): Boolean {
        return try {
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(offset)
                raf.readFully(target)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun scanForTrailingMoov(file: File, length: Long): Box? {
        val chunkSize = 64 * 1024
        try {
            RandomAccessFile(file, "r").use { raf ->
                val chunk = ByteArray(chunkSize)
                var position = 0L
                while (position < length) {
                    raf.seek(position)
                    val read = raf.read(chunk, 0, minOf(chunkSize.toLong(), length - position).toInt())
                    if (read < 4) break
                    var i = 0
                    while (i + 4 <= read) {
                        if (chunk[i] == 'm'.code.toByte() && chunk[i + 1] == 'o'.code.toByte() &&
                            chunk[i + 2] == 'o'.code.toByte() && chunk[i + 3] == 'v'.code.toByte()
                        ) {
                            val sizeOffset = position + i - 4
                            if (sizeOffset >= 0) {
                                raf.seek(sizeOffset)
                                val sizeBytes = ByteArray(4)
                                if (raf.read(sizeBytes) == 4) {
                                    val size = ByteBuffer.wrap(sizeBytes).order(ByteOrder.BIG_ENDIAN)
                                        .int.toLong() and 0xFFFFFFFFL
                                    if (size >= 8 && sizeOffset + size == length) {
                                        return Box("moov", sizeOffset, 8, size)
                                    }
                                }
                            }
                        }
                        i++
                    }
                    if (read < chunkSize) break
                    position += read - 3
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun buildUdta(
        existingItems: List<Pair<String, ByteArray>>,
        title: String,
        artist: String,
        album: String,
        cover: ByteArray?,
        trackNumber: Int?,
        discNumber: Int?,
        year: Int?,
    ): ByteArray {
        val items = ArrayList<ByteArray>(existingItems.size + 6)
        for ((id, bytes) in existingItems) {
            if (id !in OWNED_ITEM_IDS) items += bytes
        }
        if (title.isNotBlank()) items += item("©nam", DATA_TYPE_TEXT, title.toByteArray(Charsets.UTF_8))
        if (artist.isNotBlank()) items += item("©ART", DATA_TYPE_TEXT, artist.toByteArray(Charsets.UTF_8))
        if (album.isNotBlank()) items += item("©alb", DATA_TYPE_TEXT, album.toByteArray(Charsets.UTF_8))
        if (trackNumber != null && trackNumber > 0) items += item("trkn", DATA_TYPE_BINARY, numberPayload(trackNumber))
        if (discNumber != null && discNumber > 0) items += item("disk", DATA_TYPE_BINARY, numberPayload(discNumber))
        if (year != null && year > 0) items += item("©day", DATA_TYPE_TEXT, year.toString().toByteArray(Charsets.UTF_8))
        if (cover != null && cover.isNotEmpty()) {
            items += item("covr", if (isPng(cover)) DATA_TYPE_PNG else DATA_TYPE_JPEG, cover)
        }
        if (items.isEmpty()) return ByteArray(0)

        val ilstPayload = items.reduce { acc, bytes -> acc + bytes }
        val hdlrPayload = ByteArray(4 + 4 + 4 + 12 + 1)
        "mdir".toByteArray(Charsets.ISO_8859_1).copyInto(hdlrPayload, 8)
        val metaPayload = ByteArray(4) + box("hdlr", hdlrPayload) + box("ilst", ilstPayload)
        return box("udta", box("meta", metaPayload))
    }

    private fun item(name: String, dataType: Int, payload: ByteArray): ByteArray {
        val data = ByteBuffer.allocate(16 + payload.size).order(ByteOrder.BIG_ENDIAN)
        data.putInt(16 + payload.size)
        data.put("data".toByteArray(Charsets.ISO_8859_1))
        data.putInt(dataType)
        data.putInt(0)
        data.put(payload)
        return box(name, data.array())
    }

    private fun numberPayload(number: Int): ByteArray {
        val value = number.coerceIn(0, 0xFFFF)
        return byteArrayOf(0, 0, (value shr 8).toByte(), value.toByte(), 0, 0, 0, 0)
    }

    private fun box(id: String, payload: ByteArray): ByteArray {
        val out = ByteArray(8 + payload.size)
        ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN).putInt(out.size)
        id.toByteArray(Charsets.ISO_8859_1).copyInto(out, 4)
        payload.copyInto(out, 8)
        return out
    }

    private fun isPng(data: ByteArray): Boolean =
        data.size > 8 && data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte()

    private fun copyRange(input: RandomAccessFile, out: FileOutputStream, offset: Long, length: Long) {
        if (length <= 0) return
        input.channel.position(offset)
        val buffer = ByteBuffer.allocate(256 * 1024)
        var remaining = length
        while (remaining > 0) {
            buffer.clear()
            buffer.limit(minOf(buffer.capacity().toLong(), remaining).toInt())
            val read = input.channel.read(buffer)
            if (read <= 0) break
            out.write(buffer.array(), 0, read)
            remaining -= read
        }
    }
}
