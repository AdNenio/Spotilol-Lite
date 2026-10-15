package com.project.lol.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class WidgetLayout { COMPACT, STANDARD, ART }

data class WidgetConfig(
    val layout: WidgetLayout = WidgetLayout.STANDARD,
    val showCover: Boolean = true,
    val showTitle: Boolean = true,
    val showArtist: Boolean = true,
    val showProgress: Boolean = true,
    val showPlayPause: Boolean = true,
    val showSkip: Boolean = true,
    val showShuffleRepeat: Boolean = false,
    val showFavorite: Boolean = false,
    val coverBackground: Boolean = true
) {

    val useCoverArt: Boolean get() = showCover && (coverBackground || layout == WidgetLayout.ART)
    val showThumbnail: Boolean get() = showCover && !useCoverArt
    val hasInfo: Boolean get() = showTitle || showArtist || showProgress
    val hasControls: Boolean get() = showPlayPause || showSkip || showShuffleRepeat || showFavorite

    fun writeTo(prefs: MutablePreferences) {
        prefs[WidgetConfigKeys.LAYOUT] = layout.name
        prefs[WidgetConfigKeys.COVER] = showCover
        prefs[WidgetConfigKeys.TITLE] = showTitle
        prefs[WidgetConfigKeys.ARTIST] = showArtist
        prefs[WidgetConfigKeys.PROGRESS] = showProgress
        prefs[WidgetConfigKeys.PLAY_PAUSE] = showPlayPause
        prefs[WidgetConfigKeys.SKIP] = showSkip
        prefs[WidgetConfigKeys.SHUFFLE_REPEAT] = showShuffleRepeat
        prefs[WidgetConfigKeys.FAVORITE] = showFavorite
        prefs[WidgetConfigKeys.COVER_BG] = coverBackground
    }

    companion object {

        val COMPACT = WidgetConfig(
            layout = WidgetLayout.COMPACT,
            showCover = true,
            showTitle = true,
            showArtist = false,
            showProgress = false,
            showPlayPause = true,
            showSkip = false,
            showShuffleRepeat = false,
            showFavorite = false,
            coverBackground = false
        )

        val STANDARD = WidgetConfig(
            layout = WidgetLayout.STANDARD,
            showCover = true,
            showTitle = true,
            showArtist = true,
            showProgress = true,
            showPlayPause = true,
            showSkip = true,
            showShuffleRepeat = false,
            showFavorite = false,
            coverBackground = true
        )

        val ART = WidgetConfig(
            layout = WidgetLayout.ART,
            showCover = true,
            showTitle = true,
            showArtist = false,
            showProgress = false,
            showPlayPause = true,
            showSkip = false,
            showShuffleRepeat = false,
            showFavorite = false,
            coverBackground = true
        )

        fun preset(layout: WidgetLayout): WidgetConfig = when (layout) {
            WidgetLayout.COMPACT -> COMPACT
            WidgetLayout.STANDARD -> STANDARD
            WidgetLayout.ART -> ART
        }

        fun from(prefs: Preferences): WidgetConfig = WidgetConfig(
            layout = prefs[WidgetConfigKeys.LAYOUT]
                ?.let { name -> WidgetLayout.entries.firstOrNull { it.name == name } }
                ?: WidgetLayout.STANDARD,
            showCover = prefs[WidgetConfigKeys.COVER] ?: true,
            showTitle = prefs[WidgetConfigKeys.TITLE] ?: true,
            showArtist = prefs[WidgetConfigKeys.ARTIST] ?: true,
            showProgress = prefs[WidgetConfigKeys.PROGRESS] ?: true,
            showPlayPause = prefs[WidgetConfigKeys.PLAY_PAUSE] ?: true,
            showSkip = prefs[WidgetConfigKeys.SKIP] ?: true,
            showShuffleRepeat = prefs[WidgetConfigKeys.SHUFFLE_REPEAT] ?: false,
            showFavorite = prefs[WidgetConfigKeys.FAVORITE] ?: false,
            coverBackground = prefs[WidgetConfigKeys.COVER_BG] ?: true
        )
    }
}

internal object WidgetConfigKeys {
    val LAYOUT = stringPreferencesKey("cfg_layout")
    val COVER = booleanPreferencesKey("cfg_cover")
    val TITLE = booleanPreferencesKey("cfg_title")
    val ARTIST = booleanPreferencesKey("cfg_artist")
    val PROGRESS = booleanPreferencesKey("cfg_progress")
    val PLAY_PAUSE = booleanPreferencesKey("cfg_play_pause")
    val SKIP = booleanPreferencesKey("cfg_skip")
    val SHUFFLE_REPEAT = booleanPreferencesKey("cfg_shuffle_repeat")
    val FAVORITE = booleanPreferencesKey("cfg_favorite")
    val COVER_BG = booleanPreferencesKey("cfg_cover_bg")
}
