package com.project.lol.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

object ThemeHarmony {

    private const val SATURATION_FLOOR = 0.55f
    private const val SATURATION_RANGE = 0.65f
    private const val VALUE_FLOOR = 0.85f
    private const val VALUE_RANGE = 0.25f
    private const val CONTAINER_MIX = 0.3f

    fun toHex(color: Color): String =
        String.format(Locale.US, "#%06X", 0xFFFFFF and color.toArgb())

    fun parseHex(value: String?): Color? {
        val raw = value?.trim()?.removePrefix("#") ?: return null
        if (raw.length != 6 && raw.length != 8) return null
        val parsed = raw.toLongOrNull(16) ?: return null
        return if (raw.length == 8) Color(parsed.toInt()) else Color(0xFF000000L or parsed)
    }

    fun hueOf(color: Color): Float = hsv(color)[0]

    fun harmonize(reference: Color, hue: Float, strength: Float): Color {
        val ref = hsv(reference)
        val amount = strength.coerceIn(0f, 1f)
        val saturation = ref[1] * (SATURATION_FLOOR + SATURATION_RANGE * amount)
        val value = ref[2] * (VALUE_FLOOR + VALUE_RANGE * amount)
        return fromHsv(hue, saturation, value)
    }

    fun suggestedHues(palette: ThemePalette): List<Float> {
        val accent = hsv(palette.accent)[0]
        val base = hsv(palette.alt)[0]
        return listOf(
            base,
            accent + 30f,
            accent - 30f,
            accent + 150f,
            accent + 180f,
            accent + 210f
        ).map { (it % 360f + 360f) % 360f }
            .distinctBy { (it / 15f).toInt() }
    }

    fun contrastOn(color: Color): Color =
        if (color.luminance() > 0.45f) Color(0xFF141414) else Color(0xFFF4F4F4)

    fun harmonizedContainer(palette: ThemePalette, secondary: Color): Color =
        lerp(palette.panelHigh, secondary, CONTAINER_MIX)

    private fun hsv(color: Color): FloatArray {
        val out = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), out)
        return out
    }

    private fun fromHsv(hue: Float, saturation: Float, value: Float): Color =
        Color(
            android.graphics.Color.HSVToColor(
                floatArrayOf(
                    (hue % 360f + 360f) % 360f,
                    saturation.coerceIn(0f, 1f),
                    value.coerceIn(0f, 1f)
                )
            )
        )
}

fun ThemePalette.harmonized(secondary: Color?): ThemePalette {
    if (secondary == null) return this
    val container = ThemeHarmony.harmonizedContainer(this, secondary)
    return copy(
        alt = secondary,
        onAlt = ThemeHarmony.contrastOn(secondary),
        altContainer = container,
        onAltContainer = ThemeHarmony.contrastOn(container)
    )
}
