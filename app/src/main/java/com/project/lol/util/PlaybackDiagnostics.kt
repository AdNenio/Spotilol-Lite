package com.project.lol.util

import android.content.Context
import android.media.MediaDrm
import androidx.webkit.WebViewCompat
import java.util.UUID

object PlaybackDiagnostics {
    private const val TAG = "playback.diag"
    private const val SECURITY_LEVEL = "securityLevel"
    private val WIDEVINE_UUID: UUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")

    data class DrmProbe(
        val webviewVersion: String,
        val widevineSupported: Boolean,
        val summary: String,
    )

    fun probe(context: Context): DrmProbe {
        val app = context.applicationContext
        val pkg = runCatching { WebViewCompat.getCurrentWebViewPackage(app) }.getOrNull()
        val webviewVersion = pkg?.versionName ?: "unknown"
        val supported = runCatching { MediaDrm.isCryptoSchemeSupported(WIDEVINE_UUID) }.getOrDefault(false)

        var drm: MediaDrm? = null
        var level = "unknown"
        var vendor = "unknown"
        var drmVersion = "unknown"
        try {
            drm = MediaDrm(WIDEVINE_UUID)
            level = runCatching { drm.getPropertyString(SECURITY_LEVEL) }.getOrNull() ?: "unknown"
            vendor = runCatching { drm.getPropertyString(MediaDrm.PROPERTY_VENDOR) }.getOrNull() ?: "unknown"
            drmVersion = runCatching { drm.getPropertyString(MediaDrm.PROPERTY_VERSION) }.getOrNull() ?: "unknown"
        } catch (e: Exception) {
            Logger.w(TAG, "widevine probe failed: ${e.javaClass.simpleName} ${e.message}")
        } finally {
            runCatching { drm?.close() }
        }

        val summary =
            "webview=$webviewVersion widevineSupported=$supported widevineLevel=$level vendor=$vendor widevineVersion=$drmVersion"
        return DrmProbe(webviewVersion, supported, summary)
    }

    fun log(context: Context) {
        val probe = probe(context)
        if (probe.webviewVersion == "unknown" || !probe.widevineSupported) {
            Logger.w(TAG, probe.summary)
        } else {
            Logger.i(TAG, probe.summary)
        }
    }
}