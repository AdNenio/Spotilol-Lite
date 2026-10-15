package com.project.lol.widget

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as material3ColorProviders
import androidx.glance.unit.ColorProvider
import com.project.lol.ui.theme.ThemeHarmony
import com.project.lol.ui.theme.ThemePresets
import com.project.lol.ui.theme.amoledScheme
import com.project.lol.ui.theme.harmonized
import com.project.lol.ui.theme.toColorScheme

internal const val WIDGET_SCRIM_ALPHA = 0.80f
internal const val WIDGET_BUTTON_ALPHA = 0.14f
internal const val WIDGET_TRACK_ALPHA = 0.20f
internal const val WIDGET_DISABLED_ALPHA = 0.38f

object WidgetTheme {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY_PRESET = "ThemePreset"
    private const val KEY_DYNAMIC = "MaterialYou"
    private const val KEY_AMOLED = "AmoledTheme"
    private const val KEY_SECONDARY = "ThemeSecondary"

    fun colorScheme(context: Context): ColorScheme {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val presetId = prefs.getString(KEY_PRESET, ThemePresets.DEFAULT_ID) ?: ThemePresets.DEFAULT_ID
        val dynamic = prefs.getBoolean(KEY_DYNAMIC, false)
        val amoled = prefs.getBoolean(KEY_AMOLED, false)
        val base = if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(app)
        } else {
            ThemePresets.resolve(presetId)
                .harmonized(ThemeHarmony.parseHex(prefs.getString(KEY_SECONDARY, null)))
                .toColorScheme()
        }
        return if (amoled) base.amoledScheme() else base
    }

    fun colorProviders(context: Context): ColorProviders =
        material3ColorProviders(colorScheme(context))
}

@Composable
internal fun withAlpha(provider: ColorProvider, alpha: Float): ColorProvider {
    val color = provider.getColor(LocalContext.current)
    return ColorProvider(color.copy(alpha = alpha))
}
