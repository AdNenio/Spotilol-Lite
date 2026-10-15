package com.project.lol.widget

import android.graphics.Bitmap
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.project.lol.R

data class WidgetState(
    val title: String = "",
    val artist: String = "",
    val playing: Boolean = false,
    val favorite: Boolean = false,
    val shuffle: String = "off",
    val repeat: String = "false",
    val position: Long = 0L,
    val duration: Long = 0L,
    val source: String = SOURCE_WEB,
    val cover: Bitmap? = null
) {
    val hasTrack: Boolean get() = title.isNotEmpty()

    val offline: Boolean get() = source == SOURCE_OFFLINE
    val supportsRepeat: Boolean get() = !offline
    val supportsFavorite: Boolean get() = !offline

    val shuffleOn: Boolean get() = shuffle == "shuffle" || shuffle == "smart"
    val smartShuffle: Boolean get() = shuffle == "smart"
    val shuffleAvailable: Boolean get() = shuffle != "disabled"
    val repeatOn: Boolean get() = repeat == "true" || repeat == "mixed"

    val progress: Float
        get() = if (duration > 0L) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    val playPauseIcon: Int get() = if (playing) R.drawable.ic_pause else R.drawable.ic_play

    val shuffleIcon: Int
        get() = when {
            smartShuffle -> R.drawable.ic_shuffle_smart_active
            shuffleOn -> R.drawable.ic_shuffle_active
            else -> R.drawable.ic_shuffle
        }

    val repeatIcon: Int
        get() = when (repeat) {
            "true" -> R.drawable.ic_repeat
            "mixed" -> R.drawable.ic_repeat_one
            else -> R.drawable.ic_repeat_off
        }

    val favoriteIcon: Int
        get() = if (favorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite

    fun writeTo(prefs: MutablePreferences, coverVersion: Long) {
        prefs[WidgetKeys.TITLE] = title
        prefs[WidgetKeys.ARTIST] = artist
        prefs[WidgetKeys.PLAYING] = playing
        prefs[WidgetKeys.FAVORITE] = favorite
        prefs[WidgetKeys.SHUFFLE] = shuffle
        prefs[WidgetKeys.REPEAT] = repeat
        prefs[WidgetKeys.POSITION] = position
        prefs[WidgetKeys.DURATION] = duration
        prefs[WidgetKeys.SOURCE] = source
        prefs[WidgetKeys.COVER_VERSION] = coverVersion
    }

    companion object {
        const val SOURCE_WEB = "web"
        const val SOURCE_OFFLINE = "offline"

        fun from(prefs: Preferences): WidgetState = WidgetState(
            title = prefs[WidgetKeys.TITLE] ?: "",
            artist = prefs[WidgetKeys.ARTIST] ?: "",
            playing = prefs[WidgetKeys.PLAYING] ?: false,
            favorite = prefs[WidgetKeys.FAVORITE] ?: false,
            shuffle = prefs[WidgetKeys.SHUFFLE] ?: "off",
            repeat = prefs[WidgetKeys.REPEAT] ?: "false",
            position = prefs[WidgetKeys.POSITION] ?: 0L,
            duration = prefs[WidgetKeys.DURATION] ?: 0L,
            source = prefs[WidgetKeys.SOURCE] ?: SOURCE_WEB
        )

        fun coverVersionOf(prefs: Preferences): Long = prefs[WidgetKeys.COVER_VERSION] ?: 0L
    }
}

internal object WidgetKeys {
    val TITLE = stringPreferencesKey("w_title")
    val ARTIST = stringPreferencesKey("w_artist")
    val PLAYING = booleanPreferencesKey("w_playing")
    val FAVORITE = booleanPreferencesKey("w_favorite")
    val SHUFFLE = stringPreferencesKey("w_shuffle")
    val REPEAT = stringPreferencesKey("w_repeat")
    val POSITION = longPreferencesKey("w_position")
    val DURATION = longPreferencesKey("w_duration")
    val SOURCE = stringPreferencesKey("w_source")
    val COVER_VERSION = longPreferencesKey("w_cover_version")
}
