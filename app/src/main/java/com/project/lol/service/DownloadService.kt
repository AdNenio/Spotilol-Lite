package com.project.lol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.toColorInt
import com.project.lol.R
import com.project.lol.offline.DownloadManager
import com.project.lol.ui.theme.ThemeController
import com.project.lol.util.AppLocale
import com.project.lol.util.Logger
import java.io.File

class DownloadService : Service() {

    companion object {
        private const val CHANNEL_ID = "spotilol_downloads"
        private const val NOTIF_ID = 3

        const val ACTION_SKIP = "com.project.lol.download.ACTION_SKIP"
        const val ACTION_CANCEL = "com.project.lol.download.ACTION_CANCEL"
    }

    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!DownloadManager.isDownloading() &&
                !DownloadManager.isWorkPending() &&
                !DownloadManager.isBatchActive()
            ) {
                stopSelf()
                return
            }
            updateNotification()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        runCatching {
            val dir = File(filesDir, "downloads")
            if (dir.exists()) dir.listFiles()?.forEach { if (it.name.endsWith(".part")) it.delete() }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SKIP -> DownloadManager.skipCurrent()
            ACTION_CANCEL -> DownloadManager.cancelAll()
        }
        try {
            ServiceCompat.startForeground(
                this, NOTIF_ID, buildNotification(), foregroundServiceType()
            )
        } catch (e: Throwable) {
            Logger.e("DownloadService", "startForeground failed", e)
        }
        handler.removeCallbacks(pollRunnable)
        handler.post(pollRunnable)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun foregroundServiceType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else 0

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_downloads_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_downloads_description)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val st = DownloadManager.status
        val queued = DownloadManager.queuedCount()
        val pct = DownloadManager.lastPct
        val active = st.active || queued > 0
        val indeterminate = st.active && !st.error && pct <= 0

        val title = if (st.batch) {
            st.collection.ifBlank { getString(R.string.dl_notif_downloading_playlist) }
        } else {
            st.title.ifBlank { getString(R.string.dl_notif_downloading_title) }
        }

        val performer = if (st.batch) {
            listOf(st.artist, st.title).filter { it.isNotBlank() }.joinToString(" — ")
        } else {
            st.artist
        }
        val position = if (st.batch && st.total > 0) {
            getString(R.string.dl_notif_track_of, st.index.coerceIn(1, st.total), st.total)
        } else {
            ""
        }

        val lines = ArrayList<String>(4)
        val head = listOf(position, performer).filter { it.isNotBlank() }.joinToString(" · ")
        if (head.isNotBlank() && head != title) lines += head
        val stage = st.stage.ifBlank { DownloadManager.lastLabel }
        if (stage.isNotBlank()) {
            lines += if (!st.error && pct > 0) getString(R.string.dl_notif_stage_percent, stage, pct.coerceIn(0, 100)) else stage
        }
        val counts = listOfNotNull(
            st.saved.takeIf { st.batch && it > 0 }?.let { getString(R.string.dl_notif_saved, it) },
            st.failed.takeIf { st.batch && it > 0 }?.let { getString(R.string.dl_notif_failed, it) },
            st.skipped.takeIf { st.batch && it > 0 }?.let { getString(R.string.dl_notif_skipped, it) },
            queued.takeIf { it > 0 }?.let { getString(R.string.dl_notif_queued, it) },
        ).joinToString(" · ")
        if (counts.isNotBlank()) lines += counts
        if (lines.isEmpty()) lines += getString(R.string.dl_notif_preparing)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(lines.first())
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
            .addAction(0, getString(R.string.dl_notif_action_skip), actionPendingIntent(ACTION_SKIP))
            .addAction(0, getString(R.string.dl_notif_action_cancel), actionPendingIntent(ACTION_CANCEL))

        if (active) {
            builder.setProgress(100, pct.coerceIn(0, 100), indeterminate)
        } else {
            builder.setProgress(0, 0, false)
        }

        try {
            builder.color = ThemeController.accentHex(this).toColorIntOrNull() ?: 0xFF1DB954.toInt()
        } catch (_: Exception) {
            builder.color = 0xFF1DB954.toInt()
        }
        return builder.build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, DownloadService::class.java).setAction(action)
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }
}

private fun String.toColorIntOrNull(): Int? = try {
    toColorInt()
} catch (_: IllegalArgumentException) {
    null
}