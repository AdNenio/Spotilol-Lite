package com.project.lol

import android.app.Application
import android.content.Context
import com.project.lol.innertube.YouTube
import com.project.lol.ui.lyrics.CustomLyricsPresets
import com.project.lol.ui.theme.ThemeController
import com.project.lol.util.AppLocale
import com.project.lol.util.CrashHandler
import com.project.lol.util.InnertubeCachePrefs
import com.project.lol.util.Logger
import com.project.lol.util.PlaybackDiagnostics
import com.project.lol.yt.cipher.CipherDeobfuscator
import java.io.File

class SpotilolApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        ThemeController.init(this)
        CustomLyricsPresets.init(this)
        migrateProxyEraPrefs()
        CrashHandler.install(this)
        CipherDeobfuscator.initialize(this)
        YouTube.setCacheDir(File(cacheDir, "innertube_http"), InnertubeCachePrefs.maxBytes(this))
        Thread { runCatching { PlaybackDiagnostics.log(this@SpotilolApp) } }.start()
        Logger.s("app", "started")
    }

    private fun migrateProxyEraPrefs() {
        val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("ProxyEraMigrated", false)) return
        val fromProxyEra = prefs.contains("ConnectionMode")
        val edit = prefs.edit()
        edit.remove("ConnectionMode")
        if (fromProxyEra) edit.putBoolean("ServiceOn", true)
        edit.putBoolean("ProxyEraMigrated", true)
        edit.apply()
        if (fromProxyEra) Logger.i("app", "proxy era prefs migrated")
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }
}
