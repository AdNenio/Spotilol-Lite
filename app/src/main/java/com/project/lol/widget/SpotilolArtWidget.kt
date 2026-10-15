package com.project.lol.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.project.lol.R
import com.project.lol.ui.MainActivity
import com.project.lol.ui.OfflineActivity

private const val FLOATING_ALPHA = 0.70f

class SpotilolArtWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val providers = WidgetTheme.colorProviders(context)
        provideContent {
            GlanceTheme(colors = providers) {
                ArtContent()
            }
        }
    }
}

class SpotilolArtWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpotilolArtWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetActions.requestRefresh(context)
    }
}

@Composable
private fun ArtContent() {
    val prefs = currentState<Preferences>()
    val state = WidgetState.from(prefs)
    val version = WidgetState.coverVersionOf(prefs)
    val context = LocalContext.current
    val bitmap = CoverCache.load(context, version)

    val open = if (state.offline) actionStartActivity(OfflineActivity::class.java)
    else actionStartActivity(MainActivity::class.java)

    val modifier = if (bitmap != null) {
        GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(bitmap), ContentScale.Crop)
            .cornerRadius(16.dp)
            .clickable(open)
    } else {
        GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .clickable(open)
    }

    Box(
        modifier = modifier,
        contentAlignment = if (state.hasTrack) Alignment.BottomStart else Alignment.Center
    ) {
        if (state.hasTrack) {
            Row(
                modifier = GlanceModifier.padding(10.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .background(withAlpha(GlanceTheme.colors.surface, FLOATING_ALPHA))
                        .cornerRadius(12.dp)
                        .clickable(actionRunCallback<PlayPauseAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(state.playPauseIcon),
                        contentDescription = context.getString(if (state.playing) R.string.notif_action_pause else R.string.notif_action_play),
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Box(
                    modifier = GlanceModifier
                        .background(withAlpha(GlanceTheme.colors.surface, WIDGET_SCRIM_ALPHA))
                        .cornerRadius(20.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = state.title,
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        } else {
            Text(
                text = context.getString(R.string.widget_empty_subtitle),
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
        }
    }
}
