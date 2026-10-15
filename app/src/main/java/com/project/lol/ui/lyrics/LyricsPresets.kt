package com.project.lol.ui.lyrics

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import com.project.lol.webview.helpers.LyricsStyle
import com.project.lol.webview.helpers.LyricsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class LyricsPreset(
    val id: String,
    val name: String,
    val style: LyricsStyle
)

@Serializable
internal data class LyricsStylePack(
    val preset: String,
    val scale: Float,
    val spacing: Float,
    val align: String,
    val animate: Boolean
)

@Serializable
internal data class LyricsPresetPack(
    val id: String,
    val name: String,
    val style: LyricsStylePack
)

@Serializable
internal data class LyricsFilePack(
    val format: String,
    val version: Int,
    val app: String,
    val exportedAt: String,
    val presets: List<LyricsPresetPack>
)

object LyricsPresetShare {

    const val FORMAT = "spotilol.lyrics"
    const val VERSION = 1
    const val MIME_TYPE = "application/json"
    const val APP_NAME = "Spotilol"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(presets: List<LyricsPreset>): String = json.encodeToString(
        LyricsFilePack(
            format = FORMAT,
            version = VERSION,
            app = APP_NAME,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            presets = presets.map { it.toPack() }
        )
    )

    fun decode(payload: String): List<LyricsPreset> {
        val file = runCatching { json.decodeFromString<LyricsFilePack>(payload) }.getOrNull()
            ?: throw IllegalArgumentException("not a lyrics preset file")
        if (file.format != FORMAT) throw IllegalArgumentException("unsupported lyrics preset file")
        if (file.version > VERSION) throw IllegalArgumentException("lyrics preset file is newer than this build")
        if (file.presets.isEmpty()) throw IllegalArgumentException("lyrics preset file has no presets")
        return file.presets.map { it.toPreset() }
    }

    fun fileName(): String =
        "spotilol-lyrics_" + SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date()) + ".spotilol.json"

    fun write(context: Context, uri: Uri, payload: String) {
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("cannot open the destination file")
        stream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
    }

    fun read(context: Context, uri: Uri): String {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("cannot open the lyrics preset file")
        return stream.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}

private fun LyricsPreset.toPack(): LyricsPresetPack = LyricsPresetPack(
    id = id,
    name = name,
    style = LyricsStylePack(
        preset = style.preset,
        scale = style.scale,
        spacing = style.spacing,
        align = style.align,
        animate = style.animate
    )
)

private fun LyricsPresetPack.toPreset(): LyricsPreset = LyricsPreset(
    id = id,
    name = name.ifBlank { "Preset" },
    style = LyricsTheme.sanitize(
        LyricsStyle(
            preset = style.preset,
            scale = style.scale,
            spacing = style.spacing,
            align = style.align,
            animate = style.animate
        )
    )
)

object CustomLyricsPresets {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY = "CustomLyricsPresets"
    private const val PREFIX = "custom_"

    val list = mutableStateListOf<LyricsPreset>()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        list.clear()
        list.addAll(load())
    }

    fun add(name: String, style: LyricsStyle): LyricsPreset {
        val stored = LyricsPreset(uniqueId(name), name.trim().ifBlank { "Preset" }, LyricsTheme.sanitize(style))
        list.add(stored)
        persist()
        return stored
    }

    fun addAll(presets: List<LyricsPreset>): List<LyricsPreset> =
        presets.map { add(it.name, it.style) }

    fun remove(id: String) {
        if (list.removeAll { it.id == id }) persist()
    }

    fun find(id: String?): LyricsPreset? = list.firstOrNull { it.id == id }

    private fun load(): List<LyricsPreset> =
        prefs?.getString(KEY, null)
            ?.let { payload -> runCatching { LyricsPresetShare.decode(payload) }.getOrDefault(emptyList()) }
            ?: emptyList()

    private fun persist() {
        prefs?.edit()?.putString(KEY, LyricsPresetShare.encode(list.toList()))?.apply()
    }

    private fun uniqueId(name: String): String {
        val slug = name.lowercase(Locale.US)
            .map { if (it.isLetterOrDigit()) it else '_' }
            .joinToString("")
            .trim('_')
            .ifBlank { "preset" }
        val base = PREFIX + slug
        val taken = LyricsTheme.STYLE_OPTIONS.mapTo(mutableSetOf()) { it.first } + list.map { it.id }
        if (base !in taken) return base
        var index = 2
        while ("${base}_$index" in taken) index++
        return "${base}_$index"
    }
}
