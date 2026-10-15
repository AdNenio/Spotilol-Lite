package com.project.lol.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

object AppLocale {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY_LANGUAGE = "AppLanguage"

    const val SYSTEM = "system"

    val AVAILABLE: List<String> = listOf(SYSTEM, "en", "fr", "de", "es", "it", "pt", "ru", "id", "tr", "zh", "zh-TW", "zh-HK", "zh-MO", "zh-CN")

    private val NATIVE_NAMES = mapOf(
        "en" to "English",
        "fr" to "Français",
        "de" to "Deutsch",
        "es" to "Español",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "id" to "Bahasa Indonesia",
        "tr" to "Türkçe",
        "zh" to "简体中文",
        "zh-TW" to "繁體中文",
        "zh-HK" to "繁體中文（香港）",
        "zh-MO" to "繁體中文（澳門）",
        "zh-CN" to "简体中文（中国）"
    )

    private val LEGACY_LANGUAGES = mapOf(
        "id" to "in",
        "he" to "iw",
        "yi" to "ji"
    )

    fun current(context: Context): String {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
        val tag = if (stored == "in") "id" else stored
        return if (tag != null && tag in AVAILABLE) tag else SYSTEM
    }

    fun set(context: Context, tag: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_LANGUAGE, tag) }
    }

    fun nativeName(tag: String): String? = NATIVE_NAMES[tag]

    fun wrap(base: Context): Context {
        val tag = current(base)
        val config = base.resources.configuration
        if (tag == SYSTEM) {
            val system = config.locales[0]
            if (system != null && Locale.getDefault() != system) Locale.setDefault(system)
            return base
        }
        val locale = localeFor(tag) ?: return base
        Locale.setDefault(locale)
        val wrapped = Configuration(config)
        wrapped.setLocale(locale)
        wrapped.setLocales(LocaleList(locale))
        return base.createConfigurationContext(wrapped)
    }

    private fun localeFor(tag: String): Locale? {
        val parsed = runCatching { Locale.forLanguageTag(tag) }.getOrNull()
            ?.takeIf { it.language.isNotBlank() } ?: return null
        val legacy = LEGACY_LANGUAGES[parsed.language] ?: return parsed
        return runCatching {
            Locale.Builder()
                .setLanguage(legacy)
                .apply {
                    if (parsed.script.isNotBlank()) setScript(parsed.script)
                    if (parsed.country.isNotBlank()) setRegion(parsed.country)
                    if (parsed.variant.isNotBlank()) setVariant(parsed.variant)
                }
                .build()
        }.getOrNull() ?: parsed
    }
}