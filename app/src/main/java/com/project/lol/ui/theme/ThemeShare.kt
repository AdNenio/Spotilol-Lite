package com.project.lol.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class ThemePalettePack(
    val accent: String,
    val onAccent: String,
    val accentSoft: String,
    val onAccentSoft: String,
    val alt: String,
    val bg: String,
    val panel: String,
    val panelHigh: String,
    val fg: String,
    val fgMuted: String,
    val outline: String,
    val onAlt: String? = null,
    val altContainer: String? = null,
    val onAltContainer: String? = null
)

@Serializable
internal data class ThemePresetPack(
    val id: String,
    val family: String,
    val name: String,
    val palette: ThemePalettePack
)

@Serializable
internal data class ThemeFilePack(
    val format: String,
    val version: Int,
    val app: String,
    val exportedAt: String,
    val themes: List<ThemePresetPack>
)

object ThemeShare {

    const val FORMAT = "spotilol.theme"
    const val VERSION = 1
    const val MIME_TYPE = "application/json"
    const val APP_NAME = "Spotilol"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(themes: List<ThemePreset>): String = json.encodeToString(
        ThemeFilePack(
            format = FORMAT,
            version = VERSION,
            app = APP_NAME,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            themes = themes.map { it.toPack() }
        )
    )

    fun decode(payload: String): List<ThemePreset> {
        val file = runCatching { json.decodeFromString<ThemeFilePack>(payload) }.getOrNull()
            ?: throw IllegalArgumentException("not a theme file")
        if (file.format != FORMAT) throw IllegalArgumentException("unsupported theme file")
        if (file.version > VERSION) throw IllegalArgumentException("theme file is newer than this build")
        if (file.themes.isEmpty()) throw IllegalArgumentException("theme file has no themes")
        return file.themes.map { it.toPreset() }
    }

    fun fileName(family: String, name: String): String {
        val slug = "$family-$name"
            .lowercase(Locale.US)
            .map { if (it.isLetterOrDigit()) it else '-' }
            .joinToString("")
            .split('-')
            .filter { it.isNotBlank() }
            .joinToString("-")
            .ifBlank { "spotilol-theme" }
        return "$slug.spotilol.json"
    }

    fun write(context: Context, uri: Uri, payload: String) {
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("cannot open the destination file")
        stream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
    }

    fun read(context: Context, uri: Uri): String {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("cannot open the theme file")
        return stream.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}

private fun ThemePreset.toPack(): ThemePresetPack = ThemePresetPack(
    id = id,
    family = family,
    name = name,
    palette = palette.toPack()
)

private fun ThemePalette.toPack(): ThemePalettePack = ThemePalettePack(
    accent = ThemeHarmony.toHex(accent),
    onAccent = ThemeHarmony.toHex(onAccent),
    accentSoft = ThemeHarmony.toHex(accentSoft),
    onAccentSoft = ThemeHarmony.toHex(onAccentSoft),
    alt = ThemeHarmony.toHex(alt),
    bg = ThemeHarmony.toHex(bg),
    panel = ThemeHarmony.toHex(panel),
    panelHigh = ThemeHarmony.toHex(panelHigh),
    fg = ThemeHarmony.toHex(fg),
    fgMuted = ThemeHarmony.toHex(fgMuted),
    outline = ThemeHarmony.toHex(outline),
    onAlt = ThemeHarmony.toHex(onAlt),
    altContainer = ThemeHarmony.toHex(altContainer),
    onAltContainer = ThemeHarmony.toHex(onAltContainer)
)

private fun ThemePresetPack.toPreset(): ThemePreset = ThemePreset(
    id = id,
    family = family.ifBlank { name },
    name = name.ifBlank { family },
    palette = palette.toPalette()
)

private fun ThemePalettePack.toPalette(): ThemePalette {
    val background = requiredColor(bg)
    val foreground = requiredColor(fg)
    val high = requiredColor(panelHigh)
    return ThemePalette(
        accent = requiredColor(accent),
        onAccent = requiredColor(onAccent),
        accentSoft = requiredColor(accentSoft),
        onAccentSoft = requiredColor(onAccentSoft),
        alt = requiredColor(alt),
        bg = background,
        panel = requiredColor(panel),
        panelHigh = high,
        fg = foreground,
        fgMuted = requiredColor(fgMuted),
        outline = requiredColor(outline),
        onAlt = onAlt?.let { requiredColor(it) } ?: background,
        altContainer = altContainer?.let { requiredColor(it) } ?: high,
        onAltContainer = onAltContainer?.let { requiredColor(it) } ?: foreground
    )
}

private fun requiredColor(value: String): Color =
    ThemeHarmony.parseHex(value) ?: throw IllegalArgumentException("invalid color $value")

object CustomThemes {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY = "CustomThemePresets"
    private const val PREFIX = "custom_"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ThemePresets.custom.clear()
        ThemePresets.custom.addAll(load())
    }

    fun add(preset: ThemePreset): ThemePreset {
        val stored = preset.copy(id = uniqueId(preset.id))
        ThemePresets.custom.add(stored)
        persist()
        return stored
    }

    fun remove(id: String) {
        if (ThemePresets.custom.removeAll { it.id == id }) persist()
    }

    fun rename(id: String, family: String, name: String) {
        val index = ThemePresets.custom.indexOfFirst { it.id == id }
        if (index < 0) return
        ThemePresets.custom[index] = ThemePresets.custom[index].copy(family = family, name = name)
        persist()
    }

    private fun load(): List<ThemePreset> =
        prefs?.getString(KEY, null)
            ?.let { payload -> runCatching { ThemeShare.decode(payload) }.getOrDefault(emptyList()) }
            ?: emptyList()

    private fun persist() {
        prefs?.edit()?.putString(KEY, ThemeShare.encode(ThemePresets.custom.toList()))?.apply()
    }

    private fun uniqueId(raw: String): String {
        val slug = raw.removePrefix(PREFIX)
            .lowercase(Locale.US)
            .map { if (it.isLetterOrDigit()) it else '_' }
            .joinToString("")
            .trim('_')
            .ifBlank { "theme" }
        val base = PREFIX + slug
        val taken = ThemePresets.all.mapTo(mutableSetOf()) { it.id }
        if (base !in taken) return base
        var index = 2
        while ("${base}_$index" in taken) index++
        return "${base}_$index"
    }
}
