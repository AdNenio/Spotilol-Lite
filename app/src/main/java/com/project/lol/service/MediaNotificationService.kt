package com.project.lol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.webkit.WebView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.project.lol.R
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import com.project.lol.util.AppLocale
import com.project.lol.util.Logger
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private fun jsString(value: String?): String = org.json.JSONObject.quote(value ?: "")

class MediaNotificationService : Service() {

    companion object {
        private const val TAG = "MediaNotifService"
        private const val CHANNEL_ID = "spotilol_media_playback"
        private const val NOTIFICATION_ID = 1
        private val mainHandler = Handler(Looper.getMainLooper())
        private const val WAKE_LOCK_RENEW_MS = 10 * 60 * 1000L

        const val ACTION_PLAY_PAUSE = "com.project.lol.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.project.lol.ACTION_NEXT"
        const val ACTION_PREV = "com.project.lol.ACTION_PREV"
        const val ACTION_SHUFFLE = "com.project.lol.ACTION_SHUFFLE"
        const val ACTION_REPEAT = "com.project.lol.ACTION_REPEAT"
        const val ACTION_FAVORITE = "com.project.lol.ACTION_FAVORITE"

        private const val CUSTOM_ACTION_TOGGLE_FAV = "toggle_fav"
        private const val CUSTOM_ACTION_TOGGLE_SHUFFLE = "toggle_shuffle"
        private const val CUSTOM_ACTION_REPEAT = "toggle_repeat"

        private val PLAYBACK_ACTIONS: Long =
            PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SEEK_TO

        private const val NOTIF_COLOR = 0xFFE0E0E0.toInt()

        val webView: WebView?
            get() = com.project.lol.webview.WebViewHost.webView
        var instance: MediaNotificationService? = null
        private var appContext: Context? = null

        @Volatile
        private var taskRemoved = false

    }

    private lateinit var mediaSession: MediaSessionCompat
    private var isPlaying = false
    private var isShuffle = false
    private var isSmartShuffle = false
    private var isShuffleAvailable = true
    private var isFavorite = false
    private var coverBitmap: Bitmap? = null
    private var currentTitle = ""
    private var currentArtist = ""
    private var currentAlbum = ""
    private var currentPosition: Long = 0L
    private var currentDuration: Long = 0L
    private var lastCoverUrl = ""
    private var isRepeat = "false"
    private var wakeLock: PowerManager.WakeLock? = null

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAY_PAUSE -> {
                    webView?.evaluateJavascript("actPlayPause(${!isPlaying})", null)
                }
                ACTION_NEXT -> webView?.evaluateJavascript("actSkipForward()", null)
                ACTION_PREV -> webView?.evaluateJavascript("actSkipBack()", null)
                ACTION_SHUFFLE -> webView?.evaluateJavascript("actToggleShuffle()", null)
                ACTION_REPEAT -> webView?.evaluateJavascript("actRepeat()", null)
                ACTION_FAVORITE -> webView?.evaluateJavascript("actAddToFav()", null)
            }
        }
    }

    private val audioBecomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
                if (prefs.getBoolean("BtAutoPause", false)) autoPauseOnce("audio becoming noisy")
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (prefs.getBoolean("BtAutoPause", false)) autoPauseOnce("acl disconnected")
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    if (prefs.getBoolean("BtAutoResume", false)) autoResumeOnce("acl connected")
                }
            }
        }
    }

    private val remoteOutputTypes = intArrayOf(
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
        AudioDeviceInfo.TYPE_BLE_SPEAKER,
        AudioDeviceInfo.TYPE_HEARING_AID,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_ACCESSORY
    )

    private val knownRouteIds = mutableSetOf<Int>()
    private var lastAutoPauseAt = 0L
    private var lastAutoResumeAt = 0L

    private val audioRouteCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            val fresh = addedDevices.filter { knownRouteIds.add(it.id) }
            if (fresh.isEmpty()) return
            val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean("BtAutoResume", false)) return
            if (fresh.none { isRemoteOutput(it.type) }) return
            autoResumeOnce("route added: " + fresh.joinToString("/") { it.type.toString() })
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            knownRouteIds.removeAll(removedDevices.map { it.id }.toSet())
            if (!isPlaying) return
            val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean("BtAutoPause", false)) return
            if (removedDevices.none { isRemoteOutput(it.type) }) return
            val stillRemote = getSystemService(AudioManager::class.java)
                .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .any { isRemoteOutput(it.type) }
            if (stillRemote) return
            autoPauseOnce("route removed: " + removedDevices.joinToString("/") { it.type.toString() })
        }
    }

    private fun isRemoteOutput(type: Int): Boolean = remoteOutputTypes.contains(type)

    private fun btLog(msg: String) {
        Logger.s("bt", msg)
    }

    private fun autoPauseOnce(source: String) {
        val now = System.currentTimeMillis()
        if (now - lastAutoPauseAt < 1500L) {
            btLog("pause skipped, duplicate trigger ($source)")
            return
        }
        lastAutoPauseAt = now
        btLog("pause trigger: $source (webView=${if (webView != null) "bound" else "null"})")
        pausePlayback()
    }

    private fun autoResumeOnce(source: String) {
        val now = System.currentTimeMillis()
        if (now - lastAutoResumeAt < 1500L) {
            btLog("resume skipped, duplicate trigger ($source)")
            return
        }
        lastAutoResumeAt = now
        btLog("resume trigger: $source")
        resumePlayback()
    }

    private var firstHeadsetCallback = true

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                if (firstHeadsetCallback) {
                    firstHeadsetCallback = false
                    return
                }
                if (state == 1) {
                    val prefs = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
                    if (prefs.getBoolean("HpAutoResume", false)) autoResumeOnce("headset plugged")
                }
            }
        }
    }

    private fun accent(): Int = 0xFF7FC79A.toInt()

    private fun tintedIcon(resId: Int): IconCompat {
        val d = AppCompatResources.getDrawable(this, resId)!!.mutate()
        d.setTint(accent())
        val bmp = createBitmap(d.intrinsicWidth, d.intrinsicHeight)
        Canvas(bmp).also { canvas ->
            d.setBounds(0, 0, bmp.width, bmp.height)
            d.draw(canvas)
        }
        return IconCompat.createWithBitmap(bmp)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContext = applicationContext
        taskRemoved = false

        try {
            createNotificationChannel()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create notification channel", e)
        }

        // Media session first: the very first foreground notification is then a
        // proper MediaStyle notification carrying the session token, and OEM
        // media routing / battery logic keys off an active session immediately.
        try {
            setupMediaSession()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to setup media session", e)
        }

        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotificationSafe(), getStartForegroundServiceType())
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to start foreground", e)
        }

        try {
            registerReceivers()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register receivers", e)
        }
        try {
            registerDisconnectReceivers()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register disconnect receivers", e)
        }
        try {
            getSystemService(AudioManager::class.java)
                .registerAudioPlaybackCallback(navPromptCallback, mainHandler)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register nav prompt callback", e)
        }
        Logger.i(TAG, "media service ready: session, notification and receivers up")
    }

    @Suppress("DEPRECATION")
    private fun getStartForegroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand startId=$startId action=${intent?.action ?: "none"} taskRemoved=$taskRemoved")
        if (taskRemoved) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent == null) {
            Logger.i(TAG, "service restarted by the system, rebuilding web player")
            runCatching { com.project.lol.webview.WebViewHost.ensure(this) }
        }
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotificationSafe(), getStartForegroundServiceType())
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to re-assert foreground", e)
        }
        try {
            MediaButtonReceiver.handleIntent(mediaSession, intent)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to handle media button intent", e)
        }
        return START_STICKY
    }

    private fun wakeAndRun(js: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                val wv = webView ?: com.project.lol.webview.WebViewHost.ensure(this)
                wv.resumeTimers()
                wv.onResume()
                wv.dispatchWindowVisibilityChanged(android.view.View.VISIBLE)
                wv.evaluateJavascript(js, null)
            } catch (e: Exception) {
                Logger.e(TAG, "Error waking WebView in wakeAndRun", e)
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            coverBitmap = null
            lastCoverUrl = ""
        }
    }

    override fun onDestroy() {
        releaseWakeLock()
        instance = null
        try { unregisterReceiver(actionReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(bluetoothReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(audioBecomingNoisyReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(headsetReceiver) } catch (_: Exception) {}
        try { getSystemService(AudioManager::class.java).unregisterAudioDeviceCallback(audioRouteCallback) } catch (_: Exception) {}
        try { getSystemService(AudioManager::class.java).unregisterAudioPlaybackCallback(navPromptCallback) } catch (_: Exception) {}
        mainHandler.removeCallbacks(navUnduckRunnable)
        if (::mediaSession.isInitialized) {
            try { mediaSession.isActive = false } catch (_: Exception) {}
            try { mediaSession.release() } catch (_: Exception) {}
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            getSystemService(NotificationManager::class.java)
                .cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_media_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_media_description)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "SpotilolSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(MediaSessionCallback())
            isActive = true
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPrepare() {
            wakeAndRun("actPlayPause(true);")
        }

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            onPlayFromMediaId(mediaId, extras)
        }

        override fun onPlay() {
            wakeAndRun("actPlayPause(true);")
        }

        override fun onPause() {
            wakeAndRun("actPlayPause(false);")
        }

        override fun onSkipToNext() {
            wakeAndRun("actSkipForward();")
        }

        override fun onSkipToPrevious() {
            wakeAndRun("actSkipBack();")
        }

        override fun onStop() {
            wakeAndRun("actPlayPause(false);")
        }

        override fun onSeekTo(pos: Long) {
            wakeAndRun("actSeek($pos);")
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                CUSTOM_ACTION_TOGGLE_FAV, "ADDTOFAV_ACTION" -> wakeAndRun("actAddToFav();")
                CUSTOM_ACTION_TOGGLE_SHUFFLE, "SHUFFLE_ACTION" -> wakeAndRun("actToggleShuffle();")
                CUSTOM_ACTION_REPEAT, "REPEAT_ACTION" -> wakeAndRun("actRepeat();")
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            wakeAndRun("playFromUri(${jsString(mediaId)});")
        }
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
            addAction(ACTION_SHUFFLE)
            addAction(ACTION_REPEAT)
            addAction(ACTION_FAVORITE)
            addAction(Intent.ACTION_MEDIA_BUTTON)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(actionReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(actionReceiver, filter)
        }
    }

    private fun registerDisconnectReceivers() {
        val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(audioBecomingNoisyReceiver, noisyFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(audioBecomingNoisyReceiver, noisyFilter)
        }

        val btFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bluetoothReceiver, btFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(bluetoothReceiver, btFilter)
        }

        val hsFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        ContextCompat.registerReceiver(this, headsetReceiver, hsFilter, ContextCompat.RECEIVER_EXPORTED)

        try {
            val am = getSystemService(AudioManager::class.java)
            knownRouteIds.clear()
            knownRouteIds.addAll(am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.id })
            am.registerAudioDeviceCallback(audioRouteCallback, mainHandler)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register audio route callback", e)
        }
    }

    private var navDucked = false
    private val navUnduckRunnable = Runnable { setWebDuck(false) }

    private val navPromptCallback = object : AudioManager.AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
            val promptActive = configs?.any { isPromptUsage(it.audioAttributes?.usage) } == true
            if (promptActive) {
                mainHandler.removeCallbacks(navUnduckRunnable)
                if (!navDucked) {
                    navDucked = true
                    setWebDuck(true)
                }
            } else if (navDucked) {
                navDucked = false
                mainHandler.removeCallbacks(navUnduckRunnable)
                mainHandler.postDelayed(navUnduckRunnable, 700L)
            }
        }
    }

    private fun isPromptUsage(usage: Int?): Boolean =
        usage == AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE ||
            usage == AudioAttributes.USAGE_ASSISTANT

    private fun setWebDuck(on: Boolean) {
        val wv = webView ?: return
        try {
            wv.evaluateJavascript("window.__splDuck&&window.__splDuck($on);", null)
        } catch (e: Exception) {
            Logger.e(TAG, "setWebDuck failed", e)
        }
    }

    private fun pausePlayback() {
        isPlaying = false
        updatePlaybackState()
        showNotification()
        if (::mediaSession.isInitialized) {
            try {
                mediaSession.controller.transportControls.pause()
            } catch (_: Exception) {}
        }
        webView?.evaluateJavascript("actPlayPause(false)", null)
    }

    private fun resumePlayback() {
        isPlaying = true
        updatePlaybackState()
        showNotification()
        if (::mediaSession.isInitialized) {
            try {
                mediaSession.controller.transportControls.play()
            } catch (_: Exception) {}
        }
        webView?.evaluateJavascript("actPlayPause(true)", null)
    }

    fun updateFromMediaStatus(json: String) {
        try {
            val obj = org.json.JSONObject(json)
            currentTitle = obj.optString("track", "")
            currentArtist = obj.optString("artist", "")
            currentAlbum = obj.optString("album", "")
            val coverUrl = obj.optString("cover", "")

            if (coverUrl.isNotEmpty() && coverUrl != "null" && coverUrl != lastCoverUrl) {
                lastCoverUrl = coverUrl
                loadCoverArt(coverUrl)
            } else if (coverUrl.isEmpty() || coverUrl == "null") {
                lastCoverUrl = ""
                coverBitmap = null
            }

            isPlaying = obj.optBoolean("playing", false)
            isFavorite = obj.optBoolean("fav", false)
            isRepeat = obj.optString("repeat", "false")
            val shuffleVal = obj.optString("shuffle", "off")
            isShuffle = shuffleVal == "shuffle" || shuffleVal == "smart"
            isSmartShuffle = shuffleVal == "smart"
            isShuffleAvailable = shuffleVal != "disabled"
            currentDuration = obj.optLong("duration", 0L)
            currentPosition = obj.optLong("position", 0L)

            if (isPlaying) acquireWakeLock() else releaseWakeLock()

            updatePlaybackState()
            updateMetadata()
            showNotification()
        } catch (_: Exception) {}
    }

    fun updatePlaybackPosition(position: Long) {
        currentPosition = position
        if (isPlaying) acquireWakeLock()
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        val favIcon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
        val shuffleIcon = when {
            isSmartShuffle -> R.drawable.ic_shuffle_smart_active
            isShuffle -> R.drawable.ic_shuffle_active
            else -> R.drawable.ic_shuffle
        }
        val repeatIcon = when (isRepeat) {
            "true" -> R.drawable.ic_repeat
            "mixed" -> R.drawable.ic_repeat_one
            else -> R.drawable.ic_repeat_off
        }
        val state = PlaybackStateCompat.Builder()
            .setActions(PLAYBACK_ACTIONS)
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED,
                currentPosition, if (isPlaying) 1f else 0f
            )
            .addCustomAction(
                CUSTOM_ACTION_TOGGLE_FAV,
                if (isFavorite) getString(R.string.notif_action_unlike) else getString(R.string.notif_action_like),
                favIcon
            )
            .addCustomAction(
                CUSTOM_ACTION_TOGGLE_SHUFFLE,
                when {
                    isSmartShuffle -> getString(R.string.notif_shuffle_disable_smart)
                    isShuffle -> getString(R.string.notif_shuffle_disable)
                    else -> getString(R.string.notif_shuffle_enable)
                },
                shuffleIcon
            )
            .addCustomAction(
                CUSTOM_ACTION_REPEAT,
                when (isRepeat) {
                    "true" -> getString(R.string.notif_repeat_disable)
                    "mixed" -> getString(R.string.notif_repeat_disable_one)
                    else -> getString(R.string.notif_repeat_enable)
                },
                repeatIcon
            )
            .build()
        if (::mediaSession.isInitialized) {
            try { mediaSession.setPlaybackState(state) } catch (_: Exception) {}
        }
    }

    private fun updateMetadata() {
        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentAlbum)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentDuration)
        coverBitmap?.let { bmp ->
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmp)
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bmp)
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bmp)
        }
        if (::mediaSession.isInitialized) {
            try { mediaSession.setMetadata(builder.build()) } catch (_: Exception) {}
        }
    }

    private fun loadCoverArt(url: String) {
        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.connect()
                val stream = conn.inputStream
                val raw = BitmapFactory.decodeStream(stream)
                stream.close()
                conn.disconnect()
                if (raw != null) {
                    val target = 512
                    val scale = min(target.toFloat() / raw.width, target.toFloat() / raw.height)
                    val w = (raw.width * scale).toInt()
                    val h = (raw.height * scale).toInt()
                    val scaled = Bitmap.createScaledBitmap(raw, w, h, true)
                    if (scaled != raw) raw.recycle()
                    coverBitmap = scaled
                    updateMetadata()
                    showNotification()
                }
            } catch (_: Exception) {}
        }.start()
    }

    private fun showNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotificationSafe(): Notification {
        return try {
            buildNotification()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to build notification", e)
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build()
        }
    }

    private fun buildMediaStyle(showShuffle: Boolean): MediaStyle {
        val compact = if (showShuffle) intArrayOf(0, 1, 2, 3) else intArrayOf(0, 1, 2)
        val style = MediaStyle()
            .setShowActionsInCompactView(*compact)
            .setShowCancelButton(true)
            .setCancelButtonIntent(getActionPendingIntent(ACTION_PLAY_PAUSE))
        if (::mediaSession.isInitialized) {
            style.setMediaSession(mediaSession.sessionToken)
        }
        return style
    }

    private fun buildNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevAction = NotificationCompat.Action.Builder(
            tintedIcon(R.drawable.ic_skip_prev), getString(R.string.notif_action_previous), getActionPendingIntent(ACTION_PREV)
        ).build()

        val playPauseAction = NotificationCompat.Action.Builder(
            tintedIcon(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            if (isPlaying) getString(R.string.notif_action_pause) else getString(R.string.notif_action_play),
            getActionPendingIntent(ACTION_PLAY_PAUSE)
        ).build()

        val nextAction = NotificationCompat.Action.Builder(
            tintedIcon(R.drawable.ic_skip_next), getString(R.string.notif_action_next), getActionPendingIntent(ACTION_NEXT)
        ).build()

        val shuffleAction = NotificationCompat.Action.Builder(
            tintedIcon(
                when {
                    isSmartShuffle -> R.drawable.ic_shuffle_smart_active
                    isShuffle -> R.drawable.ic_shuffle_active
                    else -> R.drawable.ic_shuffle
                }
            ),
            when {
                isSmartShuffle -> getString(R.string.notif_shuffle_disable_smart)
                isShuffle -> getString(R.string.notif_shuffle_disable)
                else -> getString(R.string.notif_shuffle_enable)
            },
            getActionPendingIntent(ACTION_SHUFFLE)
        ).build()

        val favAction = NotificationCompat.Action.Builder(
            tintedIcon(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite),
            if (isFavorite) getString(R.string.notif_action_unlike) else getString(R.string.notif_action_like),
            getActionPendingIntent(ACTION_FAVORITE)
        ).build()

        val actions = mutableListOf<NotificationCompat.Action>()
        actions.add(prevAction)
        actions.add(playPauseAction)
        actions.add(nextAction)
        if (isShuffleAvailable) actions.add(shuffleAction)
        actions.add(favAction)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle.ifEmpty { getString(R.string.app_name) })
            .setContentText(currentArtist)
            .setSubText(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(accent())
            .setStyle(buildMediaStyle(isShuffleAvailable))
        actions.forEach { builder.addAction(it) }

        coverBitmap?.let { builder.setLargeIcon(it) }

        return builder.build()
    }

    private fun getActionPendingIntent(action: String): PendingIntent {
        val mediaButtonAction = when (action) {
            ACTION_PLAY_PAUSE -> PlaybackStateCompat.ACTION_PLAY_PAUSE
            ACTION_NEXT -> PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            ACTION_PREV -> PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            else -> null
        }
        if (mediaButtonAction != null) {
            return MediaButtonReceiver.buildMediaButtonPendingIntent(this, mediaButtonAction)
        }
        val intent = Intent(action).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "spotilol:media_playback"
            )
        }
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(WAKE_LOCK_RENEW_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
            wakeLock = null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val stopOnSwipe = getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
            .getBoolean("SwipeStop", true)
        if (stopOnSwipe) {
            taskRemoved = true
            if (::mediaSession.isInitialized) {
                try { mediaSession.isActive = false } catch (_: Exception) {}
            }
            releaseWakeLock()
            try {
                getSystemService(NotificationManager::class.java)
                    .cancel(NOTIFICATION_ID)
            } catch (_: Exception) {}
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            mainHandler.postDelayed({ releaseWebViewAfterTaskRemoved() }, 500)
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun releaseWebViewAfterTaskRemoved() {
        if (com.project.lol.webview.WebViewHost.uiHost != null) return
        Logger.i(TAG, "task removed, releasing webview")
        com.project.lol.webview.WebViewHost.release()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }
}
