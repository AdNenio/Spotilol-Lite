package com.project.lol.webview.helpers

import android.content.SharedPreferences
import com.project.lol.R
import java.util.Locale
import org.json.JSONObject

/**
 * Spotilol - Lyrics Style Engine.
 *
 * Emits a dedicated <style id="spotilol-lyrics-style"> element. It never
 * touches 'spotilol-custom-css' (owned by the CustomCss setting).
 *
 * The engine no longer relies on Spotify's obfuscated class hashes - those
 * rotate every build. It anchors only on stable hooks:
 *   - [data-testid="lyrics-line"] and its single text child
 *   - [data-spl-lyr="a|p|u"] / [data-spl-lyr-root], written at runtime by
 *     LyricsEngine (the dynamic resolver)
 *   - the live CSS vars --lyrics-color-* that Spotify keeps on the lyrics tree
 *
 * Presets define the flavour (size curve, weight, glow, state dimming); the
 * size / spacing / alignment / animation controls are user overrides on top.
 */

data class LyricsStyle(
    val preset: String = LyricsTheme.DEFAULT_STYLE,
    val scale: Float = 1f,
    val spacing: Float = 1f,
    val align: String = LyricsTheme.ALIGN_START,
    val animate: Boolean = true
) {
    companion object {
        const val PREF_PRESET = "LyricsStyle"
        const val PREF_SCALE = "LyricsScale"
        const val PREF_SPACING = "LyricsSpacing"
        const val PREF_ALIGN = "LyricsAlign"
        const val PREF_ANIMATE = "LyricsAnimate"

        /** Selected preset id (built-in or custom) - UI state only, never affects the CSS. */
        const val PREF_PRESET_ID = "LyricsPresetId"

        val PREF_KEYS = listOf(PREF_PRESET, PREF_SCALE, PREF_SPACING, PREF_ALIGN, PREF_ANIMATE)

        fun from(prefs: SharedPreferences): LyricsStyle = LyricsStyle(
            preset = prefs.getString(PREF_PRESET, LyricsTheme.DEFAULT_STYLE) ?: LyricsTheme.DEFAULT_STYLE,
            scale = prefs.getFloat(PREF_SCALE, 1f),
            spacing = prefs.getFloat(PREF_SPACING, 1f),
            align = prefs.getString(PREF_ALIGN, LyricsTheme.ALIGN_START) ?: LyricsTheme.ALIGN_START,
            animate = prefs.getBoolean(PREF_ANIMATE, true)
        )
    }
}

object LyricsTheme {

    /** (prefValue, settingsLabelRes) - feeds the preset list. */
    val STYLE_OPTIONS: List<Pair<String, Int>> = listOf(
        "default" to R.string.settings_lyrics_style_spotify_default,
        "fullscreen" to R.string.settings_lyrics_style_fullscreen,
        "compact" to R.string.settings_lyrics_style_compact,
        "karaoke" to R.string.settings_lyrics_style_karaoke,
        "bold" to R.string.settings_lyrics_style_bold
    )

    const val DEFAULT_STYLE = "fullscreen"
    private const val STYLE_ID = "spotilol-lyrics-style"

    const val ALIGN_START = "start"
    const val ALIGN_CENTER = "center"
    const val ALIGN_END = "end"
    val ALIGN_OPTIONS = listOf(ALIGN_START, ALIGN_CENTER, ALIGN_END)

    const val SCALE_MIN = 0.75f
    const val SCALE_MAX = 1.6f
    const val SPACING_MIN = 0.8f
    const val SPACING_MAX = 2.2f

    class Preset internal constructor(
        val base: String,
        val baseWide: String,
        val lineHeight: Float,
        val weight: Int,
        val activeWeight: Int,
        val activeScale: Float,
        val activeShadow: String,
        val passedOpacity: Float,
        val upcomingOpacity: Float,
        val paintBackground: Boolean,
        val previewSp: Float
    )

    private val PRESETS: Map<String, Preset> = mapOf(
        "default" to Preset(
            "clamp(1.05rem, 4.6vw, 1.45rem)", "clamp(1.1rem, 2.1vw, 1.9rem)",
            1.5f, 600, 700, 1.0f, "none", 0.45f, 0.85f, false, 15f
        ),
        "fullscreen" to Preset(
            "clamp(1.15rem, 5.5vw, 1.6rem)", "clamp(1.25rem, 2.4vw, 2.25rem)",
            1.45f, 700, 800, 1.06f, "0 0 18px rgba(255,255,255,.28), 0 2px 6px rgba(0,0,0,.5)",
            0.30f, 0.55f, true, 17f
        ),
        "compact" to Preset(
            "clamp(.9rem, 4.2vw, 1.15rem)", "clamp(.95rem, 1.9vw, 1.35rem)",
            1.35f, 600, 700, 1.02f, "none", 0.28f, 0.50f, false, 14f
        ),
        "karaoke" to Preset(
            "clamp(1.1rem, 5vw, 1.6rem)", "clamp(1.2rem, 2.6vw, 2rem)",
            1.45f, 700, 900, 1.18f, "0 0 30px rgba(255,255,255,.45), 0 2px 8px rgba(0,0,0,.55)",
            0.16f, 0.30f, true, 18f
        ),
        "bold" to Preset(
            "clamp(1.2rem, 5.8vw, 1.75rem)", "clamp(1.3rem, 2.8vw, 2.4rem)",
            1.5f, 800, 800, 1.04f, "0 2px 10px rgba(0,0,0,.6)", 0.42f, 0.72f, true, 18f
        )
    )

    fun presetFor(id: String?): Preset = PRESETS[id] ?: PRESETS.getValue(DEFAULT_STYLE)

    fun isBuiltIn(id: String?): Boolean = id != null && PRESETS.containsKey(id)

    /** Clamps an untrusted style (imported file, stale prefs) into a usable range. */
    fun sanitize(style: LyricsStyle): LyricsStyle = LyricsStyle(
        preset = if (isBuiltIn(style.preset)) style.preset else DEFAULT_STYLE,
        scale = style.scale.coerceIn(SCALE_MIN, SCALE_MAX),
        spacing = style.spacing.coerceIn(SPACING_MIN, SPACING_MAX),
        align = if (style.align in ALIGN_OPTIONS) style.align else ALIGN_START,
        animate = style.animate
    )

    /** Approximate on-device preview params (dp-ish) for the settings preview. */
    class PreviewStyle(
        val baseSp: Float,
        val lineHeight: Float,
        val weight: Int,
        val activeWeight: Int,
        val activeScale: Float,
        val passedOpacity: Float,
        val upcomingOpacity: Float
    )

    fun previewStyle(style: LyricsStyle): PreviewStyle {
        val p = presetFor(style.preset)
        return PreviewStyle(
            baseSp = p.previewSp * style.scale.coerceIn(SCALE_MIN, SCALE_MAX),
            lineHeight = p.lineHeight * style.spacing.coerceIn(SPACING_MIN, SPACING_MAX),
            weight = p.weight,
            activeWeight = p.activeWeight,
            activeScale = p.activeScale,
            passedOpacity = p.passedOpacity,
            upcomingOpacity = p.upcomingOpacity
        )
    }

    fun buildLyricsStyleJs(style: LyricsStyle): String {
        val css = cssFor(style)
        return if (css.isEmpty()) {
            """
            (function(){
                var st = document.getElementById('$STYLE_ID');
                if (st) st.remove();
            })();
            """.trimIndent()
        } else {
            val jsonCss = JSONObject.quote(css)
            """
            (function(){
                var st = document.getElementById('$STYLE_ID');
                if (!st) {
                    st = document.createElement('style');
                    st.id = '$STYLE_ID';
                }
                st.textContent = $jsonCss;
                var target = document.head || document.documentElement;
                if (target && !st.parentNode) target.appendChild(st);
                if (window.__splLyricsResolve) window.__splLyricsResolve();
            })();
            """.trimIndent()
        }
    }

    private fun cssFor(style: LyricsStyle): String {
        val preset = PRESETS[style.preset] ?: return ""
        val align = when (style.align) {
            ALIGN_CENTER -> "center"
            ALIGN_END -> "end"
            else -> "start"
        }
        val origin = when (align) {
            "center" -> "center center"
            "end" -> "right center"
            else -> "left center"
        }
        val scale = style.scale.coerceIn(SCALE_MIN, SCALE_MAX)
        val spacing = style.spacing.coerceIn(SPACING_MIN, SPACING_MAX)
        val line = "[data-testid=\"lyrics-line\"]"

        return buildString {
            append("/* --- Spotilol Lyrics Engine --- */\n")
            append(":root{")
            append("--spl-lyr-scale:").append(num(scale)).append(";")
            append("--spl-lyr-space:").append(num(spacing)).append(";")
            append("--spl-lyr-base:").append(preset.base).append(";")
            append("--spl-lyr-lh:").append(num(preset.lineHeight)).append(";")
            append("}\n")
            append("@media (min-width:769px){:root{--spl-lyr-base:").append(preset.baseWide).append(";}}\n")

            if (preset.paintBackground) {
                append("[data-spl-lyr-root]{")
                append("background-color:var(--spl-lyr-bg,transparent)!important;")
                append("background-image:none!important;")
                append("transition:background-color .6s ease!important;")
                append("}\n")
            }

            append(line).append("{")
            append("margin:0!important;")
            append("padding:1px 0!important;")
            append("font-size:calc(var(--spl-lyr-base) * var(--spl-lyr-scale))!important;")
            append("line-height:calc(var(--spl-lyr-lh) * var(--spl-lyr-space))!important;")
            append("text-align:").append(align).append("!important;")
            append("overflow-wrap:anywhere!important;")
            append("user-select:text!important;")
            append("}\n")

            append(line).append(">div{")
            append("font-size:1em!important;")
            append("line-height:inherit!important;")
            append("font-weight:").append(preset.weight).append("!important;")
            append("transform-origin:").append(origin).append(";")
            if (style.animate) {
                append("transition:opacity .35s ease,font-weight .35s cubic-bezier(.3,.7,.3,1),")
                append("transform .35s cubic-bezier(.3,.7,.3,1),text-shadow .35s ease!important;")
            } else {
                append("transition:none!important;")
            }
            append("}\n")

            append(line).append("[data-spl-lyr=\"p\"]>div{opacity:").append(num(preset.passedOpacity)).append("!important;}\n")
            append(line).append("[data-spl-lyr=\"u\"]>div{opacity:").append(num(preset.upcomingOpacity)).append("!important;}\n")

            append(line).append("[data-spl-lyr=\"a\"]>div{")
            append("opacity:1!important;")
            append("font-weight:").append(preset.activeWeight).append("!important;")
            if (preset.activeScale != 1f) append("transform:scale(").append(num(preset.activeScale)).append(");")
            if (preset.activeShadow != "none") append("text-shadow:").append(preset.activeShadow).append("!important;")
            append("}\n")

            append("@media (prefers-reduced-motion: reduce){")
            append(line).append(">div{transition:none!important;}")
            append("}\n")
        }
    }

    private fun num(value: Float): String = String.format(Locale.US, "%.3f", value)
}
