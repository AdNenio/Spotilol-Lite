package com.project.lol.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.project.lol.service.MediaNotificationService
import com.project.lol.service.OfflineMediaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal enum class WidgetCommand { PLAY_PAUSE, NEXT, PREV, SHUFFLE, REPEAT, FAVORITE }

internal object WidgetSource {

    private const val PREFS = "spotilol_widget_prefs"
    private const val KEY = "active_source"

    fun set(context: Context, source: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, source).apply()
    }

    fun get(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, WidgetState.SOURCE_WEB) ?: WidgetState.SOURCE_WEB
}

internal object WidgetActions {

    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun send(context: Context, command: WidgetCommand) {
        val action = if (WidgetSource.get(context) == WidgetState.SOURCE_OFFLINE) {
            offlineAction(command)
        } else {
            webAction(command)
        } ?: return
        context.sendBroadcast(Intent(action).setPackage(context.packageName))
    }

    fun requestRefresh(context: Context) {
        val action = if (WidgetSource.get(context) == WidgetState.SOURCE_OFFLINE) {
            OfflineMediaService.ACTION_WIDGET_REFRESH
        } else {
            MediaNotificationService.ACTION_WIDGET_REFRESH
        }
        context.sendBroadcast(Intent(action).setPackage(context.packageName))
    }

    fun refreshAll(context: Context) {
        val app = context.applicationContext
        refreshScope.launch {
            val manager = GlanceAppWidgetManager(app)
            redraw(app, manager, SpotilolPlayerWidget::class.java) { SpotilolPlayerWidget() }
            redraw(app, manager, SpotilolArtWidget::class.java) { SpotilolArtWidget() }
        }
    }

    fun setOfflineMode(context: Context, offline: Boolean) {
        val app = context.applicationContext
        val source = if (offline) WidgetState.SOURCE_OFFLINE else WidgetState.SOURCE_WEB
        refreshScope.launch {
            runCatching { WidgetUpdater.push(app, WidgetState(source = source)) }
        }
    }

    private suspend fun redraw(
        context: Context,
        manager: GlanceAppWidgetManager,
        cls: Class<out GlanceAppWidget>,
        factory: () -> GlanceAppWidget
    ) {
        val ids = runCatching { manager.getGlanceIds(cls) }.getOrNull() ?: return
        if (ids.isEmpty()) return
        val widget = factory()
        for (id in ids) {
            runCatching { widget.update(context, id) }
        }
    }

    private fun webAction(command: WidgetCommand): String? = when (command) {
        WidgetCommand.PLAY_PAUSE -> MediaNotificationService.ACTION_PLAY_PAUSE
        WidgetCommand.NEXT -> MediaNotificationService.ACTION_NEXT
        WidgetCommand.PREV -> MediaNotificationService.ACTION_PREV
        WidgetCommand.SHUFFLE -> MediaNotificationService.ACTION_SHUFFLE
        WidgetCommand.REPEAT -> MediaNotificationService.ACTION_REPEAT
        WidgetCommand.FAVORITE -> MediaNotificationService.ACTION_FAVORITE
    }

    private fun offlineAction(command: WidgetCommand): String? = when (command) {
        WidgetCommand.PLAY_PAUSE -> OfflineMediaService.ACTION_PLAY_PAUSE
        WidgetCommand.NEXT -> OfflineMediaService.ACTION_NEXT
        WidgetCommand.PREV -> OfflineMediaService.ACTION_PREV
        WidgetCommand.SHUFFLE -> OfflineMediaService.ACTION_SHUFFLE
        WidgetCommand.REPEAT, WidgetCommand.FAVORITE -> null
    }
}

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.PLAY_PAUSE)
    }
}

class NextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.NEXT)
    }
}

class PreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.PREV)
    }
}

class ShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.SHUFFLE)
    }
}

class RepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.REPEAT)
    }
}

class FavoriteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetActions.send(context, WidgetCommand.FAVORITE)
    }
}
