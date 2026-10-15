package com.project.lol.ui

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.project.lol.R
import com.project.lol.profile.ProfileManager
import com.project.lol.service.OfflineMediaService
import com.project.lol.timer.SleepTimerManager
import com.project.lol.ui.screens.OfflineScreen
import com.project.lol.ui.theme.SpotifyTheme
import com.project.lol.ui.theme.ThemeController
import com.project.lol.ui.theme.ThemeHarmony
import com.project.lol.ui.theme.ThemePresets
import com.project.lol.util.AppLocale
import com.project.lol.widget.WidgetActions

class OfflineActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    private val materialYouState = mutableStateOf(false)
    private val amoledState = mutableStateOf(false)
    private val hideTopBarState = mutableStateOf(false)
    private val landscapeState = mutableStateOf(false)
    private val keepScreenOnState = mutableStateOf(false)
    private val themePresetState = mutableStateOf(ThemePresets.DEFAULT_ID)
    private val themeSecondaryState = mutableStateOf<Color?>(null)
    private val pipActiveState = mutableStateOf(false)

    private var pipPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)

        materialYouState.value = prefs.getBoolean("MaterialYou", false)
        amoledState.value = prefs.getBoolean("AmoledTheme", false)
        hideTopBarState.value = prefs.getBoolean("HideTopBar", false)
        landscapeState.value = prefs.getBoolean("LandscapeMode", false)
        keepScreenOnState.value = prefs.getBoolean("KeepScreenOn", false)
        themePresetState.value = prefs.getString("ThemePreset", ThemePresets.DEFAULT_ID) ?: ThemePresets.DEFAULT_ID
        themeSecondaryState.value = ThemeHarmony.parseHex(prefs.getString("ThemeSecondary", null))

        applyOrientation()
        applyKeepScreenOn()

        setContent {
            SpotifyTheme {
                OfflineScreen(
                    prefs = prefs,
                    materialYou = materialYouState.value,
                    onMaterialYouChange = { enabled ->
                        materialYouState.value = enabled
                        ThemeController.updateDynamicColor(enabled)
                    },
                    amoledTheme = amoledState.value,
                    onAmoledThemeChange = { enabled ->
                        amoledState.value = enabled
                        ThemeController.updateAmoled(enabled)
                    },
                    hideTopBar = hideTopBarState.value,
                    onHideTopBarChange = { enabled ->
                        hideTopBarState.value = enabled
                        prefs.edit().putBoolean("HideTopBar", enabled).apply()
                    },
                    landscapeMode = landscapeState.value,
                    onLandscapeModeChange = { enabled ->
                        landscapeState.value = enabled
                        prefs.edit().putBoolean("LandscapeMode", enabled).apply()
                        applyOrientation()
                    },
                    keepScreenOn = keepScreenOnState.value,
                    onKeepScreenOnChange = { enabled ->
                        keepScreenOnState.value = enabled
                        prefs.edit().putBoolean("KeepScreenOn", enabled).apply()
                        applyKeepScreenOn()
                    },
                    themePreset = themePresetState.value,
                    onThemePresetChange = { id ->
                        themePresetState.value = id
                        ThemeController.updatePreset(id)
                    },
                    themeSecondary = themeSecondaryState.value,
                    onThemeSecondaryChange = { color ->
                        themeSecondaryState.value = color
                        ThemeController.updateSecondary(color)
                    },
                    onOfflineModeChange = { enabled ->
                        prefs.edit().putBoolean("OfflineMode", enabled).apply()
                        WidgetActions.setOfflineMode(this, enabled)
                        restartToSplash()
                    },
                    onSaveProfile = { name, cookies ->
                        ProfileManager.saveProfile(this, name, cookies)
                        Toast.makeText(this, getString(R.string.offline_act_toast_account_saved), Toast.LENGTH_SHORT).show()
                    },
                    onLoadProfile = { cookies ->
                        if (!ProfileManager.applyProfile(this, cookies)) {
                            Toast.makeText(this, getString(R.string.offline_act_toast_profile_load_failed), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.offline_act_toast_profile_loaded), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteProfile = { name ->
                        ProfileManager.deleteProfile(this, name)
                        Toast.makeText(this, getString(R.string.offline_act_toast_profile_deleted), Toast.LENGTH_SHORT).show()
                    },
                    onClearCache = { clearWebViewCache() },
                    onClearData = { clearAllData() },
                    onClearThirdParty = { clearThirdPartyLogins() },
                    pipActive = pipActiveState.value,
                    onEnterPip = { enterPipMode() },
                    onPlaybackStateChange = { playing ->
                        pipPlaying = playing
                        updatePipParams()
                    },
                    onExit = { exitOfflineMode() }
                )
            }
        }
    }

    private fun applyOrientation() {
        requestedOrientation = if (landscapeState.value) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun applyKeepScreenOn() {
        if (keepScreenOnState.value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipActiveState.value = isInPictureInPictureMode
        updatePipParams()
    }

    private fun enterPipMode() {
        if (isInPictureInPictureMode) return
        runCatching { enterPictureInPictureMode(buildPipParams()) }
    }

    private fun updatePipParams() {
        if (!isInPictureInPictureMode) return
        runCatching { setPictureInPictureParams(buildPipParams()) }
    }

    private fun buildPipParams(): PictureInPictureParams =
        PictureInPictureParams.Builder()
            .setAspectRatio(Rational(1, 1))
            .setActions(buildPipActions())
            .build()

    private fun buildPipActions(): List<RemoteAction> {
        val previous = RemoteAction(
            Icon.createWithResource(this, R.drawable.ic_skip_prev),
            getString(R.string.offline_desc_previous),
            getString(R.string.offline_desc_previous),
            pipActionIntent(OfflineMediaService.ACTION_PREV)
        )
        val playPause = RemoteAction(
            Icon.createWithResource(this, if (pipPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            getString(if (pipPlaying) R.string.offline_desc_pause else R.string.offline_desc_play),
            getString(if (pipPlaying) R.string.offline_desc_pause else R.string.offline_desc_play),
            pipActionIntent(OfflineMediaService.ACTION_PLAY_PAUSE)
        )
        val next = RemoteAction(
            Icon.createWithResource(this, R.drawable.ic_skip_next),
            getString(R.string.offline_desc_next),
            getString(R.string.offline_desc_next),
            pipActionIntent(OfflineMediaService.ACTION_NEXT)
        )
        return listOf(previous, playPause, next)
    }

    private fun pipActionIntent(action: String): PendingIntent {
        val intent = Intent(action).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun clearWebViewCache() {
        val wv = WebView(applicationContext)
        wv.clearCache(true)
        wv.clearHistory()
        wv.destroy()
        Toast.makeText(this, getString(R.string.offline_act_toast_cache_cleared), Toast.LENGTH_SHORT).show()
    }

    private fun clearAllData() {
        val wv = WebView(applicationContext)
        wv.clearCache(true)
        wv.clearHistory()
        wv.clearFormData()
        wv.destroy()
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        prefs.edit().putBoolean("LoggedIn", false).apply()
        Toast.makeText(this, getString(R.string.offline_act_toast_all_data_cleared), Toast.LENGTH_SHORT).show()
    }

    private fun clearThirdPartyLogins() {
        val session = ProfileManager.captureSession(this)
        if (session != null) {
            ProfileManager.applyProfile(this, session)
        } else {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
        Toast.makeText(this, getString(R.string.offline_act_toast_third_party_cleared), Toast.LENGTH_SHORT).show()
    }

    private fun restartToSplash() {
        SleepTimerManager.cancel()
        startActivity(
            Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun exitOfflineMode() {
        prefs.edit().putBoolean("OfflineMode", false).apply()
        WidgetActions.setOfflineMode(this, false)
        restartToSplash()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }
}
