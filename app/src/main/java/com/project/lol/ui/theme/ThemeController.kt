package com.project.lol.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.project.lol.widget.WidgetActions
import java.util.Locale

object ThemeController {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY_PRESET = "ThemePreset"
    private const val KEY_DYNAMIC = "MaterialYou"
    private const val KEY_AMOLED = "AmoledTheme"
    private const val KEY_SECONDARY = "ThemeSecondary"
    private const val BRIGHTEN_FACTOR = 0.12f

    var presetId by mutableStateOf(ThemePresets.DEFAULT_ID)
        private set
    var dynamicColor by mutableStateOf(false)
        private set
    var amoled by mutableStateOf(false)
        private set
    var secondary by mutableStateOf<Color?>(null)
        private set

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        if (prefs != null) return
        appContext = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        CustomThemes.init(context)
        val p = prefs!!
        presetId = p.getString(KEY_PRESET, ThemePresets.DEFAULT_ID)
            ?.takeIf { ThemePresets.byId.containsKey(it) }
            ?: ThemePresets.DEFAULT_ID
        secondary = ThemeHarmony.parseHex(p.getString(KEY_SECONDARY, null))
        dynamicColor = p.getBoolean(KEY_DYNAMIC, false)
        amoled = p.getBoolean(KEY_AMOLED, false)
    }

    fun updatePreset(id: String) {
        presetId = if (ThemePresets.byId.containsKey(id)) id else ThemePresets.DEFAULT_ID
        save()
        refreshWidgets()
    }

    fun updateSecondary(color: Color?) {
        secondary = color
        save()
        refreshWidgets()
    }

    fun importThemes(themes: List<ThemePreset>): List<ThemePreset> = themes.map { CustomThemes.add(it) }

    fun removeCustomTheme(id: String) {
        CustomThemes.remove(id)
        if (presetId == id) updatePreset(ThemePresets.DEFAULT_ID)
    }

    fun updateDynamicColor(value: Boolean) {
        dynamicColor = value
        save()
        refreshWidgets()
    }

    fun updateAmoled(value: Boolean) {
        amoled = value
        save()
        refreshWidgets()
    }

    fun resolvePalette(): ThemePalette = ThemePresets.resolve(presetId).harmonized(secondary)

    fun accentColor(context: Context): Color =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context.applicationContext).primary
        } else {
            resolvePalette().accent
        }

    fun accentHex(context: Context): String =
        String.format(Locale.US, "#%06X", 0xFFFFFF and accentColor(context).toArgb())

    fun buildAccentJs(context: Context): String {
        val c = accentColor(context).toArgb() or 0xFF000000.toInt()
        fun lift(v: Int) = v + ((255 - v) * BRIGHTEN_FACTOR).toInt()
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        val hex = String.format(Locale.US, "#%06X", 0xFFFFFF and c)
        val bright = String.format(Locale.US, "#%02X%02X%02X", lift(r), lift(g), lift(b))
        return """
            (function(){
                var r=document.documentElement;
                r.style.setProperty('--spl-accent','$hex');
                r.style.setProperty('--spl-accent-bright','$bright');
                r.style.setProperty('--spl-accent-rgb','$r,$g,$b');
            })();
        """.trimIndent()
    }

    private fun save() {
        prefs?.edit()
            ?.putString(KEY_PRESET, presetId)
            ?.putBoolean(KEY_DYNAMIC, dynamicColor)
            ?.putBoolean(KEY_AMOLED, amoled)
            ?.putString(KEY_SECONDARY, secondary?.let { ThemeHarmony.toHex(it) })
            ?.apply()
    }

    private fun refreshWidgets() {
        appContext?.let { WidgetActions.refreshAll(it) }
    }
}
