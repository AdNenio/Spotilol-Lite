package com.project.lol.timer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.project.lol.service.MediaNotificationService
import com.project.lol.util.Logger
import kotlin.system.exitProcess
import androidx.core.content.edit

enum class SleepTimerAction(val prefValue: String) {
    PAUSE("pause"),
    QUIT("quit");

    companion object {
        fun from(value: String?): SleepTimerAction =
            entries.firstOrNull { it.prefValue == value } ?: PAUSE
    }
}

enum class SleepTimerMode { COUNTDOWN, END_OF_SONG }

data class SleepTimerState(
    val active: Boolean = false,
    val mode: SleepTimerMode = SleepTimerMode.COUNTDOWN,
    val remainingMs: Long = 0L,
    val action: SleepTimerAction = SleepTimerAction.PAUSE,
)

object SleepTimerManager {
    private const val TAG = "SleepTimer"
    private const val PREFS = "spotilol_prefs"
    private const val KEY_ACTION = "SleepTimerAction"
    private const val TICK_MS = 1000L

    private const val END_OF_SONG_THRESHOLD_MS = 2500L

    @Volatile private var onStateChange: (() -> Unit)? = null

    @Volatile private var onExpire: (() -> Unit)? = null

    private val hostLock = Any()
    private var hostHandle: Any? = null

    fun registerHost(stateChange: () -> Unit, expire: () -> Unit): () -> Unit {
        val handle = Any()
        synchronized(hostLock) {
            hostHandle = handle
            onStateChange = stateChange
            onExpire = expire
        }
        stateChange()
        return {
            val isCurrentHost = synchronized(hostLock) {
                if (hostHandle === handle) {
                    onStateChange = null
                    onExpire = null
                    true
                } else false
            }
            if (isCurrentHost) cancel()
        }
    }

    private var timer: CountDownTimer? = null

    @Volatile private var active = false
    @Volatile private var mode = SleepTimerMode.COUNTDOWN
    @Volatile private var remainingMs = 0L
    @Volatile var action = SleepTimerAction.PAUSE
        private set
    @Volatile private var actionLoaded = false

    @Volatile private var armedTrackTitle: String? = null
    @Volatile private var lastTrackTitle: String? = null
    @Volatile private var lastDurationRaw = 0L
    @Volatile private var lastPositionRaw = 0L
    @Volatile private var lastPlaying = false

    @Volatile private var webUnitsAreMs: Boolean? = null
    @Volatile private var unitSamplePos = -1L
    @Volatile private var unitSampleAt = 0L

    val isActive: Boolean get() = active
    val isEndOfSongArmed: Boolean get() = active && mode == SleepTimerMode.END_OF_SONG

    fun state(): SleepTimerState = SleepTimerState(active, mode, remainingMs, action)

    fun loadAction(context: Context) {
        if (actionLoaded) return
        action = SleepTimerAction.from(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACTION, null)
        )
        actionLoaded = true
        Logger.i(TAG, "action loaded: $action")
    }

    fun setAction(context: Context, value: SleepTimerAction) {
        action = value
        actionLoaded = true
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putString(KEY_ACTION, value.prefValue) }
        Logger.i(TAG, "action set: $value")
        notifyState()
    }

    fun startCountdown(minutes: Int) {
        if (minutes <= 0) return
        stopTimer()
        active = true
        mode = SleepTimerMode.COUNTDOWN
        remainingMs = minutes * 60_000L
        armedTrackTitle = null
        Logger.i(TAG, "countdown started: $minutes min (action=$action)")
        timer = object : CountDownTimer(remainingMs, TICK_MS) {
            override fun onTick(ms: Long) {
                remainingMs = ms
                notifyState()
            }

            override fun onFinish() {
                expire("countdown finished")
            }
        }.start()
        notifyState()
    }

    fun startEndOfSong() {
        stopTimer()
        active = true
        mode = SleepTimerMode.END_OF_SONG
        remainingMs = 0L
        armedTrackTitle = lastTrackTitle
        lastPositionRaw = 0L
        Logger.i(TAG, "end-of-song armed for '${armedTrackTitle ?: "next known track"}' (action=$action)")
        notifyState()
    }

    fun onTrackInfo(title: String?, durationRaw: Long, positionRaw: Long, playing: Boolean) {
        if (!title.isNullOrBlank()) {
            if (isEndOfSongArmed && armedTrackTitle == null) armedTrackTitle = title
            lastTrackTitle = title
        }
        lastPlaying = playing
        detectWebUnits(positionRaw)
        if (durationRaw > 0) lastDurationRaw = durationRaw

        if (!isEndOfSongArmed) {
            lastPositionRaw = positionRaw
            return
        }

        val duration = lastDurationRaw
        val armed = armedTrackTitle
        val trackChanged = armed != null && !title.isNullOrBlank() && title != armed
        val nearEnd = playing && webUnitsAreMs != null && duration > 0 && positionRaw >= 0 &&
                remainingMsOf(duration, positionRaw) in 1..END_OF_SONG_THRESHOLD_MS
        val restarted = playing && duration > 0 && lastPositionRaw > 0 &&
                positionRaw + duration / 10 < lastPositionRaw &&
                lastPositionRaw >= duration - duration / 10

        lastPositionRaw = positionRaw

        when {
            trackChanged -> expire("armed track ended (track changed)")
            nearEnd -> expire("armed track about to end")
            restarted -> expire("armed track restarted (repeat one)")
        }
    }

    fun onPosition(positionRaw: Long) {
        onTrackInfo(null, 0L, positionRaw, lastPlaying)
    }

    fun onTrackCompleted() {
        if (isEndOfSongArmed) expire("armed track completed")
    }

    fun cancel() {
        val wasActive = active
        stopTimer()
        active = false
        mode = SleepTimerMode.COUNTDOWN
        remainingMs = 0L
        armedTrackTitle = null
        if (wasActive) {
            Logger.i(TAG, "timer cancelled")
            notifyState()
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun expire(reason: String) {
        stopTimer()
        active = false
        mode = SleepTimerMode.COUNTDOWN
        remainingMs = 0L
        armedTrackTitle = null
        Logger.i(TAG, "timer expired: $reason (action=$action)")
        notifyState()
        onExpire?.invoke()
    }

    private fun notifyState() {
        onStateChange?.invoke()
    }

    private fun remainingMsOf(durationRaw: Long, positionRaw: Long): Long =
        if (webUnitsAreMs == true) durationRaw - positionRaw
        else (durationRaw - positionRaw) * 1000

    private fun detectWebUnits(positionRaw: Long) {
        if (webUnitsAreMs != null) return
        val now = SystemClock.elapsedRealtime()
        val lastPos = unitSamplePos
        val lastAt = unitSampleAt
        unitSamplePos = positionRaw
        unitSampleAt = now
        if (lastPos !in 0..<positionRaw || now - lastAt < 900) return
        val perWallSecond = (positionRaw - lastPos) * 1000 / (now - lastAt)
        if (perWallSecond >= 300) {
            webUnitsAreMs = true
            Logger.i(TAG, "web progress unit: milliseconds")
        } else if (perWallSecond in 1..3) {
            webUnitsAreMs = false
            Logger.i(TAG, "web progress unit: seconds")
        }
    }
}

object AppQuit {
    private const val TAG = "AppQuit"
    private const val EXIT_DELAY_MS = 250L

    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var quitting = false

    fun quit(context: Context) {
        if (quitting) return
        quitting = true
        val app = context.applicationContext
        Logger.i(TAG, "quitting app")
        runCatching { app.stopService(Intent(app, MediaNotificationService::class.java)) }
        (context as? Activity)?.let { runCatching { it.finishAndRemoveTask() } }
        mainHandler.postDelayed({
            runCatching { exitProcess(0) }
        }, EXIT_DELAY_MS)
    }
}
