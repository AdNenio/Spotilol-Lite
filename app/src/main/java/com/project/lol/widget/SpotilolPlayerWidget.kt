package com.project.lol.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.project.lol.R
import com.project.lol.ui.MainActivity
import com.project.lol.ui.OfflineActivity

private const val RADIUS = 18

class SpotilolPlayerWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 48.dp),
            DpSize(250.dp, 72.dp),
            DpSize(250.dp, 110.dp),
            DpSize(250.dp, 190.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val providers = WidgetTheme.colorProviders(context)
        provideContent {
            GlanceTheme(colors = providers) {
                PlayerContent()
            }
        }
    }
}

class SpotilolPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpotilolPlayerWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetActions.requestRefresh(context)
    }
}

@Composable
private fun PlayerContent() {
    val prefs = currentState<Preferences>()
    val state = WidgetState.from(prefs)
    val config = WidgetConfig.from(prefs)
    val version = WidgetState.coverVersionOf(prefs)
    val size = LocalSize.current
    val context = LocalContext.current

    if (!state.hasTrack) {
        EmptyContent(state, config, context)
        return
    }

    when {
        config.layout == WidgetLayout.ART -> ArtContent(state, config, version, context)
        config.layout == WidgetLayout.COMPACT || size.height < 80.dp -> CompactContent(state, config, version, context)
        else -> StandardContent(state, config, version, context)
    }
}

private fun tint(provider: ColorProvider): ColorFilter = ColorFilter.tint(provider)

private fun openAction(offline: Boolean): Action =
    if (offline) actionStartActivity(OfflineActivity::class.java)
    else actionStartActivity(MainActivity::class.java)

@Composable
private fun frame(cover: Bitmap?): GlanceModifier {
    val base = GlanceModifier.fillMaxSize()
    val filled = if (cover != null) {
        base.background(ImageProvider(cover), ContentScale.Crop)
    } else {
        base.background(GlanceTheme.colors.surface)
    }
    return filled.cornerRadius(RADIUS.dp)
}

@Composable
private fun scrim(): GlanceModifier = GlanceModifier
    .fillMaxSize()
    .background(withAlpha(GlanceTheme.colors.surface, WIDGET_SCRIM_ALPHA))
    .cornerRadius(RADIUS.dp)

@Composable
private fun EmptyContent(state: WidgetState, config: WidgetConfig, context: Context) {
    Box(modifier = frame(null)) {
        Row(
            modifier = scrim().clickable(openAction(state.offline)).padding(10.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_notification),
                contentDescription = null,
                modifier = GlanceModifier.size(26.dp)
            )
            Spacer(GlanceModifier.width(12.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.widget_empty_title),
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (config.layout != WidgetLayout.COMPACT) {
                    Text(
                        text = context.getString(R.string.widget_empty_subtitle),
                        maxLines = 1,
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardContent(state: WidgetState, config: WidgetConfig, version: Long, context: Context) {
    val cover = if (config.showCover) CoverCache.load(context, version) else null
    Box(modifier = frame(if (config.useCoverArt) cover else null)) {
        Column(modifier = scrim().padding(10.dp)) {
            val showFavorite = config.showFavorite && state.supportsFavorite
            val showBadge = state.offline
            if (config.hasInfo || config.showThumbnail || showFavorite || showBadge) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier.defaultWeight().clickable(openAction(state.offline)),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        if (config.showThumbnail) {
                            Cover(cover, 46.dp, 10.dp)
                            Spacer(GlanceModifier.width(11.dp))
                        }
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            if (config.showTitle) {
                                Text(
                                    text = state.title,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            if (config.showArtist) {
                                Text(
                                    text = state.artist,
                                    maxLines = 1,
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                                )
                            }
                            if (config.showProgress && state.duration > 0L) {
                                if (config.showTitle || config.showArtist) Spacer(GlanceModifier.height(7.dp))
                                ProgressBar(state)
                            }
                        }
                    }
                    if (showBadge) {
                        Spacer(GlanceModifier.width(8.dp))
                        OfflineBadge()
                    } else if (showFavorite) {
                        Spacer(GlanceModifier.width(8.dp))
                        CircleIconButton(
                            resId = state.favoriteIcon,
                            contentDescription = context.getString(if (state.favorite) R.string.notif_action_unlike else R.string.notif_action_like),
                            iconTint = if (state.favorite) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                            dimension = 26.dp,
                            iconSize = 14.dp,
                            action = actionRunCallback<FavoriteAction>()
                        )
                    }
                }
            }
            if (config.hasControls) {
                if (config.hasInfo || config.showThumbnail || showFavorite || showBadge) Spacer(GlanceModifier.height(6.dp))
                Controls(state, config, context)
            }
        }
    }
}

@Composable
private fun CompactContent(state: WidgetState, config: WidgetConfig, version: Long, context: Context) {
    val cover = if (config.showCover) CoverCache.load(context, version) else null
    Box(modifier = frame(if (config.useCoverArt) cover else null)) {
        Row(
            modifier = scrim().clickable(openAction(state.offline)).padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            if (config.showThumbnail) {
                Cover(cover, 32.dp, 9.dp)
                Spacer(GlanceModifier.width(10.dp))
            }
            if (config.showTitle || config.showArtist) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    if (config.showTitle) {
                        Text(
                            text = state.title,
                            maxLines = 1,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (config.showArtist) {
                        Text(
                            text = state.artist,
                            maxLines = 1,
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }
            } else {
                Spacer(GlanceModifier.defaultWeight())
            }
            if (config.showSkip) {
                Spacer(GlanceModifier.width(6.dp))
                CircleIconButton(
                    resId = R.drawable.ic_skip_prev,
                    contentDescription = context.getString(R.string.notif_action_previous),
                    iconTint = GlanceTheme.colors.onSurface,
                    dimension = 26.dp,
                    iconSize = 14.dp,
                    action = actionRunCallback<PreviousAction>()
                )
            }
            if (config.showPlayPause) {
                Spacer(GlanceModifier.width(6.dp))
                CircleIconButton(
                    resId = state.playPauseIcon,
                    contentDescription = context.getString(if (state.playing) R.string.notif_action_pause else R.string.notif_action_play),
                    iconTint = GlanceTheme.colors.onPrimary,
                    background = GlanceTheme.colors.primary,
                    dimension = 30.dp,
                    iconSize = 15.dp,
                    action = actionRunCallback<PlayPauseAction>()
                )
            }
            if (config.showSkip) {
                Spacer(GlanceModifier.width(6.dp))
                CircleIconButton(
                    resId = R.drawable.ic_skip_next,
                    contentDescription = context.getString(R.string.notif_action_next),
                    iconTint = GlanceTheme.colors.onSurface,
                    dimension = 26.dp,
                    iconSize = 14.dp,
                    action = actionRunCallback<NextAction>()
                )
            }
        }
    }
}

@Composable
private fun ArtContent(state: WidgetState, config: WidgetConfig, version: Long, context: Context) {
    val cover = if (config.showCover) CoverCache.load(context, version) else null
    Box(
        modifier = frame(if (config.useCoverArt) cover else null),
        contentAlignment = Alignment.BottomStart
    ) {
        val showPlay = config.showPlayPause
        val showTitle = config.showTitle
        if (showPlay || showTitle) {
            Row(
                modifier = GlanceModifier.padding(10.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (showPlay) {
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .background(withAlpha(GlanceTheme.colors.surface, WIDGET_SCRIM_ALPHA))
                            .cornerRadius(12.dp)
                            .clickable(actionRunCallback<PlayPauseAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(state.playPauseIcon),
                            contentDescription = context.getString(if (state.playing) R.string.notif_action_pause else R.string.notif_action_play),
                            modifier = GlanceModifier.size(16.dp),
                            colorFilter = tint(GlanceTheme.colors.onSurface)
                        )
                    }
                }
                if (showTitle) {
                    if (showPlay) Spacer(GlanceModifier.width(8.dp))
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
            }
        }
    }
}

@Composable
private fun Cover(bitmap: Bitmap?, dimension: Dp, radius: Dp) {
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.size(dimension).cornerRadius(radius),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = GlanceModifier
                .size(dimension)
                .background(withAlpha(GlanceTheme.colors.onSurface, WIDGET_TRACK_ALPHA))
                .cornerRadius(radius),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_notification),
                contentDescription = null,
                modifier = GlanceModifier.size(dimension / 2)
            )
        }
    }
}

@Composable
private fun ProgressBar(state: WidgetState) {
    if (state.duration <= 0L) return
    LinearProgressIndicator(
        progress = state.progress,
        modifier = GlanceModifier.fillMaxWidth().height(3.dp).cornerRadius(2.dp),
        color = GlanceTheme.colors.primary,
        backgroundColor = withAlpha(GlanceTheme.colors.onSurface, WIDGET_TRACK_ALPHA)
    )
}

@Composable
private fun Controls(state: WidgetState, config: WidgetConfig, context: Context) {
    val showShuffle = config.showShuffleRepeat
    val showRepeat = config.showShuffleRepeat && state.supportsRepeat
    val showPlay = config.showPlayPause
    val showSkip = config.showSkip
    if (!showShuffle && !showRepeat && !showSkip && !showPlay) return

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Spacer(GlanceModifier.defaultWeight())
        if (showShuffle) {
            CircleIconButton(
                resId = state.shuffleIcon,
                contentDescription = context.getString(R.string.widget_action_shuffle),
                iconTint = if (state.shuffleOn) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                dimension = 28.dp,
                iconSize = 15.dp,
                enabled = state.shuffleAvailable,
                action = actionRunCallback<ShuffleAction>()
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        if (showSkip) {
            CircleIconButton(
                resId = R.drawable.ic_skip_prev,
                contentDescription = context.getString(R.string.notif_action_previous),
                iconTint = GlanceTheme.colors.onSurface,
                dimension = 28.dp,
                iconSize = 15.dp,
                action = actionRunCallback<PreviousAction>()
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        if (showPlay) {
            CircleIconButton(
                resId = state.playPauseIcon,
                contentDescription = context.getString(if (state.playing) R.string.notif_action_pause else R.string.notif_action_play),
                iconTint = GlanceTheme.colors.onPrimary,
                background = GlanceTheme.colors.primary,
                dimension = 36.dp,
                iconSize = 17.dp,
                action = actionRunCallback<PlayPauseAction>()
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        if (showSkip) {
            CircleIconButton(
                resId = R.drawable.ic_skip_next,
                contentDescription = context.getString(R.string.notif_action_next),
                iconTint = GlanceTheme.colors.onSurface,
                dimension = 28.dp,
                iconSize = 15.dp,
                action = actionRunCallback<NextAction>()
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        if (showRepeat) {
            CircleIconButton(
                resId = state.repeatIcon,
                contentDescription = context.getString(R.string.widget_action_repeat),
                iconTint = if (state.repeatOn) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                dimension = 28.dp,
                iconSize = 15.dp,
                action = actionRunCallback<RepeatAction>()
            )
            Spacer(GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun OfflineBadge() {
    Box(
        modifier = GlanceModifier
            .background(withAlpha(GlanceTheme.colors.onSurface, WIDGET_BUTTON_ALPHA))
            .cornerRadius(11.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = LocalContext.current.getString(R.string.widget_badge_offline),
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CircleIconButton(
    resId: Int,
    contentDescription: String,
    iconTint: ColorProvider,
    dimension: Dp,
    iconSize: Dp,
    action: Action,
    background: ColorProvider? = null,
    enabled: Boolean = true
) {
    val fill = background ?: withAlpha(GlanceTheme.colors.onSurface, WIDGET_BUTTON_ALPHA)
    val base = GlanceModifier
        .size(dimension)
        .background(fill)
        .cornerRadius(dimension / 2)
    val modifier = if (enabled) base.clickable(action) else base
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            provider = ImageProvider(resId),
            contentDescription = contentDescription,
            modifier = GlanceModifier.size(iconSize),
            colorFilter = tint(
                if (enabled) iconTint else withAlpha(GlanceTheme.colors.onSurface, WIDGET_DISABLED_ALPHA)
            )
        )
    }
}
