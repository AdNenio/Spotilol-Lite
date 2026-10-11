package com.project.lol.webview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.project.lol.bridge.SpotifyBridge
import com.project.lol.util.Logger
import java.lang.ref.WeakReference

object WebViewHost {

    interface UiHost {
        fun onProgress(progress: Int)
        fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)
        fun onHideCustomView()
        fun onWebViewError(code: Int, description: String)
        fun onRenderProcessGone()
        fun onPlayerReady(view: WebView)
    }

    private const val TAG = "webviewHost"
    private const val PLAYER_URL = "https://open.spotify.com/"
    private const val LOGIN_URL = "https://accounts.spotify.com/login"
    private const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36"

    @Volatile
    var webView: WebView? = null
        private set

    @Volatile
    var bridge: SpotifyBridge? = null
        private set

    @Volatile
    var uiHost: UiHost? = null

    fun bridgeFor(context: Context): SpotifyBridge {
        bridge?.let { return it }
        val created = SpotifyBridge(WeakReference(null))
        bridge = created
        return created
    }

    fun ensure(context: Context): WebView {
        webView?.let { return it }
        val app = context.applicationContext
        val bridgeRef = bridgeFor(app)
        val view = WebView(app)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        view.settings.apply {
            userAgentString = DESKTOP_UA
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            setGeolocationEnabled(false)
            @Suppress("DEPRECATION")
            saveFormData = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        view.setInitialScale(100)
        view.setBackgroundColor(0xFF000000.toInt())
        if (WebViewFeature.isFeatureSupported(WebViewFeature.BACK_FORWARD_CACHE)) {
            WebSettingsCompat.setBackForwardCacheEnabled(view.settings, true)
        }
        view.addJavascriptInterface(bridgeRef, "AndBridge")
        view.webChromeClient = SpotifyWebChromeClient(
            onProgressChanged = { progress -> uiHost?.onProgress(progress) },
            onShowCustomView = { custom, callback -> uiHost?.onShowCustomView(custom, callback) },
            onHideCustomView = { uiHost?.onHideCustomView() }
        )
        val client = SpotifyWebViewClient(
            onPlayerReady = { ready -> uiHost?.onPlayerReady(ready) },
            onLoginRequired = { view.loadUrl(LOGIN_URL) },
            onRenderProcessGone = { onRendererGone() },
            onWebViewError = { code, description -> uiHost?.onWebViewError(code, description) }
        )
        view.webViewClient = client
        client.installDocumentStartScripts(view)
        bridge = bridgeRef
        webView = view
        val loggedIn = app.getSharedPreferences("spotilol_prefs", Context.MODE_PRIVATE)
            .getBoolean("LoggedIn", false)
        val target = if (loggedIn) PLAYER_URL else LOGIN_URL
        Logger.i(TAG, "webview created: target=$target")
        view.loadUrl(target)
        return view
    }

    fun attach(host: UiHost) {
        uiHost = host
    }

    fun detach(host: UiHost) {
        if (uiHost === host) uiHost = null
    }

    private fun onRendererGone() {
        Logger.w(TAG, "renderer gone, releasing webview")
        val host = uiHost
        release()
        host?.onRenderProcessGone()
    }

    fun release() {
        val view = webView ?: return
        webView = null
        bridge = null
        try {
            view.stopLoading()
            view.removeJavascriptInterface("AndBridge")
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_TERMINATE)) {
                try { WebViewCompat.getWebViewRenderProcess(view)?.terminate() } catch (_: Exception) {}
            }
            (view.parent as? ViewGroup)?.removeView(view)
            view.removeAllViews()
            view.destroy()
        } catch (_: Exception) {
        }
        Logger.i(TAG, "webview released")
    }
}
