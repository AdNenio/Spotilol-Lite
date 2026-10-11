package com.project.lol.bridge

import android.app.Activity
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.project.lol.R
import com.project.lol.service.MediaNotificationService
import com.project.lol.webview.helpers.AdIdStore
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import com.project.lol.offline.DownloadManager
import com.project.lol.spicy.SpicyLyricsClient
import com.project.lol.util.Logger

class SpotifyBridge(activityRef: WeakReference<Activity>) {

    companion object {
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36"

        private val FILTERED_HEADERS = setOf(
            "x-requested-with",
            "sec-ch-ua-full-version-list",
            "sec-ch-ua-platform-version",
            "sec-ch-ua-arch",
            "sec-ch-ua-bitness",
            "sec-ch-ua-model"
        )

        private const val TAG = "bridge"
        private const val CALL = "bridge.call"
    }

    private var activityRef = activityRef

    fun rebind(ref: WeakReference<Activity>) {
        activityRef = ref
    }
    var onLoginDetected: (() -> Unit)? = null
    var onPlayLoaded: (() -> Unit)? = null
    var onMediaStatus: ((String) -> Unit)? = null
    var onMediaPosition: ((Long) -> Unit)? = null
    var onTimerDialogRequest: (() -> Unit)? = null
    var onEnterPipRequest: (() -> Unit)? = null
    var onEnterPipVideoRequest: ((Int, Int) -> Unit)? = null
    var onEnterVideoFullscreen: (() -> Unit)? = null
    var onExitVideoFullscreen: (() -> Unit)? = null
    var onEnterLyricsPipRequest: (() -> Unit)? = null
    var onDownloadTrack: ((String) -> Unit)? = null
    var onDownloadCollection: ((String) -> Unit)? = null
    var onPlayerExpanded: ((Boolean) -> Unit)? = null
    var onWideVideo: ((Boolean) -> Unit)? = null
    var onCtxMenuState: ((Boolean) -> Unit)? = null

    @JavascriptInterface
    fun loginDetected() {
        val activity = activityRef.get() ?: return
        Logger.i(TAG, "login detected")
        activity.getSharedPreferences("spotilol_prefs", Activity.MODE_PRIVATE)
            .edit()
            .putBoolean("LoggedIn", true)
            .apply()
        activity.runOnUiThread {
            onLoginDetected?.invoke()
        }
    }

    @JavascriptInterface
    fun deferMessage(msg: String?) {
        val activity = activityRef.get() ?: return
        if (msg == "adblock") return
        val display = when (msg) {
            "unlock" -> activity.getString(R.string.bridge_player_unlocked)
            "reload" -> activity.getString(R.string.bridge_reloading)
            else -> msg
        }
        Logger.d(CALL, "deferMessage: $display")
        activity.runOnUiThread {
            Toast.makeText(activity, display, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun isWoke(): Boolean {
        val activity = activityRef.get() ?: return true
        val visible = activity.window?.decorView?.visibility == View.VISIBLE
        Logger.v(CALL, "isWoke -> $visible")
        return visible
    }

    @JavascriptInterface
    fun wakeUp() {
        Logger.v(CALL, "wakeUp")
    }

    @JavascriptInterface
    fun wakeOff() {
        Logger.v(CALL, "wakeOff")
    }

    @JavascriptInterface
    fun cssInjected() {
        Logger.v(CALL, "cssInjected")
    }

    @JavascriptInterface
    fun dbg(level: String?, msg: String?) {
        if (!Logger.isEnabled()) return
        Logger.js(level, msg)
    }

    @JavascriptInterface
    fun clearDebugLog() {
        Logger.clear()
        Logger.d(CALL, "logger buffer cleared from js")
    }

    @JavascriptInterface
    fun recAdContentIds(json: String?) {
        val payload = json ?: return
        val arr = try { JSONArray(payload) } catch (e: Exception) { return }
        val ids = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val v = arr.optString(i, "")
            if (v.isNotEmpty()) ids.add(v)
        }
        if (ids.isNotEmpty()) {
            AdIdStore.addAll(ids)
            Logger.d(CALL, "recAdContentIds: ${ids.size}")
        }
    }

    @JavascriptInterface
    fun playLoaded() {
        Logger.i(TAG, "play loaded, web player ready")
        MediaNotificationService.onPlayerLoaded()
        activityRef.get()?.runOnUiThread {
            onPlayLoaded?.invoke()
        }
    }

    @JavascriptInterface
    fun recMediaPosition(position: Long) {
        Logger.v(CALL, "position=$position")
        onMediaPosition?.invoke(position)
        MediaNotificationService.instance?.updatePlaybackPosition(position)
    }

    @JavascriptInterface
    fun recMediaStatus(json: String?) {
        json?.let {
            Logger.d(CALL, "media status (${it.length} chars): ${it.take(180)}")
            onMediaStatus?.invoke(it)
            MediaNotificationService.instance?.updateFromMediaStatus(it)
        }
    }

    @JavascriptInterface
    fun onMediaItemsLoaded(parentId: String?, json: String?) {
        Logger.d(CALL, "media items parent=$parentId size=${json?.length ?: 0}")
        parentId?.let { MediaNotificationService.onMediaItemsLoaded(it, json ?: "[]") }
    }

    @JavascriptInterface
    fun onSearchCompleted(query: String?, json: String?) {
        Logger.d(CALL, "search completed query=$query size=${json?.length ?: 0}")
        query?.let { MediaNotificationService.onSearchCompleted(it, json ?: "[]") }
    }

    @JavascriptInterface
    fun manageTShut(enabled: Boolean) {
        Logger.v(CALL, "manageTShut=$enabled")
    }

    @JavascriptInterface
    fun manageTSleep(enabled: Boolean) {
        Logger.v(CALL, "manageTSleep=$enabled")
    }

    @JavascriptInterface
    fun recAccountName(name: String) {
        val activity = activityRef.get() ?: return
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            Logger.i(TAG, "account name: $trimmed")
            activity.getSharedPreferences("spotilol_prefs", Activity.MODE_PRIVATE)
                .edit()
                .putString("CurrentAccountName", trimmed)
                .apply()
        }
    }

    @JavascriptInterface
    fun openTimerDialog() {
        val activity = activityRef.get() ?: return
        Logger.d(CALL, "openTimerDialog")
        activity.runOnUiThread {
            onTimerDialogRequest?.invoke()
        }
    }

    @JavascriptInterface
    fun playerExpanded(expanded: Boolean) {
        val activity = activityRef.get() ?: return
        Logger.v(CALL, "playerExpanded=$expanded")
        activity.runOnUiThread {
            onPlayerExpanded?.invoke(expanded)
        }
    }

    @JavascriptInterface
    fun wideVideo(showing: Boolean) {
        val activity = activityRef.get() ?: return
        Logger.v(CALL, "wideVideo=$showing")
        activity.runOnUiThread {
            onWideVideo?.invoke(showing)
        }
    }

    @JavascriptInterface
    fun ctxMenuState(open: Boolean) {
        val activity = activityRef.get() ?: return
        Logger.v(CALL, "ctxMenuState=$open")
        activity.runOnUiThread {
            onCtxMenuState?.invoke(open)
        }
    }

    @JavascriptInterface
    fun enterPip() {
        val activity = activityRef.get() ?: return
        Logger.i(CALL, "enterPip")
        activity.runOnUiThread {
            onEnterPipRequest?.invoke()
        }
    }

    @JavascriptInterface
    fun enterPipVideo(w: Int, h: Int) {
        val activity = activityRef.get() ?: return
        Logger.i(CALL, "enterPipVideo ${w}x$h")
        activity.runOnUiThread {
            onEnterPipVideoRequest?.invoke(w, h)
        }
    }

    @JavascriptInterface
    fun enterVideoFullscreen() {
        val activity = activityRef.get() ?: return
        Logger.i(CALL, "enterVideoFullscreen")
        activity.runOnUiThread {
            onEnterVideoFullscreen?.invoke()
        }
    }

    @JavascriptInterface
    fun exitVideoFullscreen() {
        val activity = activityRef.get() ?: return
        Logger.i(CALL, "exitVideoFullscreen")
        activity.runOnUiThread {
            onExitVideoFullscreen?.invoke()
        }
    }

    @JavascriptInterface
    fun enterLyricsPip() {
        val activity = activityRef.get() ?: return
        Logger.i(CALL, "enterLyricsPip")
        activity.runOnUiThread {
            onEnterLyricsPipRequest?.invoke()
        }
    }

    @JavascriptInterface
    fun downloadTrack(json: String?) {
        Logger.i(CALL, "downloadTrack (${json?.length ?: 0} chars)")
        json?.let { onDownloadTrack?.invoke(it) }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun downloadCollection(json: String?) {
        Logger.i(CALL, "downloadCollection (${json?.length ?: 0} chars)")
        json?.let { onDownloadCollection?.invoke(it) }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun skipDownload() {
        Logger.i(CALL, "skipDownload")
        DownloadManager.skipCurrent()
    }

    @Suppress("unused")
    @JavascriptInterface
    fun cancelDownload() {
        Logger.i(CALL, "cancelDownload")
        DownloadManager.cancelAll()
    }

    @Suppress("unused")
    @JavascriptInterface
    fun nFetch(url: String, optsJson: String?): String {
        val errorResult = { e: Exception ->
            try {
                JSONObject().apply {
                    put("status", 0)
                    put("body", e.toString())
                    put("headers", JSONObject())
                }.toString()
            } catch (_: Exception) {
                "{\"status\":0,\"body\":\"error\",\"headers\":{}}"
            }
        }

        var conn: HttpURLConnection? = null
        return try {
            val opts = if (optsJson.isNullOrBlank()) JSONObject() else JSONObject(optsJson)
            val method = opts.optString("method", "GET")
            val body = if (opts.has("body") && !opts.isNull("body")) opts.getString("body") else null
            val headersJson =
                if (opts.has("headers") && !opts.isNull("headers")) opts.getJSONObject("headers") else JSONObject()

            Logger.d(TAG, "nFetch $method ${url.take(140)} body=${body?.length ?: 0} headers=${headersJson.length()}")

            conn = URL(url).openConnection() as HttpURLConnection
            conn.apply {
                requestMethod = method
                connectTimeout = 10000
                readTimeout = 10000
                val keys = headersJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (!FILTERED_HEADERS.contains(key.lowercase(Locale.ROOT))) {
                        setRequestProperty(key, headersJson.getString(key))
                    }
                }
                setRequestProperty("User-Agent", DESKTOP_UA)
                setRequestProperty("sec-ch-ua-platform", "\"Windows\"")
                setRequestProperty("sec-ch-ua-mobile", "?0")
                setRequestProperty("sec-ch-ua", "\"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"150\", \"Google Chrome\";v=\"150\"")
                if (url.contains("spclient.spotify.com") || url.contains("scdn.co") || url.contains("spotify.com")) {
                    setRequestProperty("Origin", "https://open.spotify.com")
                    setRequestProperty("Referer", "https://open.spotify.com/")
                }
                val cookie = CookieManager.getInstance().getCookie(url)
                if (!cookie.isNullOrEmpty()) setRequestProperty("Cookie", cookie)
                if (!body.isNullOrEmpty()) {
                    doOutput = true
                    outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                }
            }

            val code = conn.responseCode
            val headerFields = conn.headerFields
            headerFields.forEach { (key, values) ->
                if (key != null && key.equals("Set-Cookie", ignoreCase = true)) {
                    values.forEach { CookieManager.getInstance().setCookie(url, it) }
                }
            }
            CookieManager.getInstance().flush()

            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val responseBody = stream?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
            Logger.d(TAG, "nFetch <- $code ${responseBody.length} bytes ${url.take(100)}")

            val responseHeaders = JSONObject()
            headerFields.forEach { (key, values) ->
                if (key != null && values.isNotEmpty()) responseHeaders.put(key, values.first())
            }
            JSONObject().apply {
                put("status", code)
                put("body", responseBody)
                put("headers", responseHeaders)
            }.toString()
        } catch (e: Exception) {
            Logger.e(TAG, "nFetch failed ${url.take(140)}", e)
            errorResult(e)
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun spicyLyrics(trackId: String?, title: String?, artist: String?, duration: Int): String {
        val id = trackId?.trim().orEmpty()
        if (id.length != 22) return "null"
        return try {
            val payload = SpicyLyricsClient.fetch(id, title?.trim(), artist?.trim(), duration)
            Logger.d(
                TAG,
                "spicyLyrics $id [${title ?: "-"}] dur=$duration -> ${if (payload == null) "miss" else "${payload.length} bytes"}"
            )
            payload ?: "null"
        } catch (e: Exception) {
            Logger.e(TAG, "spicyLyrics failed $id", e)
            "null"
        }
    }
}
