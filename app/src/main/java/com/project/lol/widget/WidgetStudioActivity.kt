package com.project.lol.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.project.lol.R
import com.project.lol.ui.screens.SettingSectionCard
import com.project.lol.ui.screens.SettingSwitchTile
import com.project.lol.ui.theme.SpotifyTheme
import com.project.lol.util.AppLocale
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowsHorizontal
import compose.icons.tablericons.ArrowsSort
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Devices
import compose.icons.tablericons.Heart
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.LayoutGrid
import compose.icons.tablericons.Palette
import compose.icons.tablericons.Photo
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerSkipBack
import compose.icons.tablericons.PlayerSkipForward
import compose.icons.tablericons.PlayerTrackNext
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Typography
import compose.icons.tablericons.User
import compose.icons.tablericons.X
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetStudioActivity : ComponentActivity() {

    private var targets: List<Int> = emptyList()
    private var isNew = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val manager = AppWidgetManager.getInstance(this)
        val provided = intent?.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        targets = if (provided != AppWidgetManager.INVALID_APPWIDGET_ID) {
            listOf(provided)
        } else {
            val component = ComponentName(this, SpotilolPlayerWidgetReceiver::class.java)
            runCatching { manager.getAppWidgetIds(component).toList() }.getOrDefault(emptyList())
        }
        isNew = provided != AppWidgetManager.INVALID_APPWIDGET_ID && !isPlaced(provided)

        setContent {
            SpotifyTheme {
                if (targets.isEmpty()) {
                    NoWidgetScreen(onClose = { finish() })
                } else {
                    StudioScreen(
                        isNew = isNew,
                        load = { loadData(this, targets.first()) },
                        onApply = { config ->
                            lifecycleScope.launch {
                                applyConfig(this@WidgetStudioActivity, targets, config)
                                if (isNew) {
                                    setResult(
                                        RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, targets.first())
                                    )
                                }
                                finish()
                            }
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun isPlaced(appWidgetId: Int): Boolean {
        val options = runCatching {
            AppWidgetManager.getInstance(this).getAppWidgetOptions(appWidgetId)
        }.getOrNull() ?: return false
        return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0) > 0
    }

    private suspend fun loadData(context: Context, appWidgetId: Int): StudioData =
        withContext(Dispatchers.IO) {
            val manager = GlanceAppWidgetManager(context)
            val prefs = runCatching {
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                SpotilolPlayerWidget().getAppWidgetState<Preferences>(context, glanceId)
            }.getOrNull()
            val state = prefs?.let { WidgetState.from(it) } ?: WidgetState()
            val version = prefs?.let { WidgetState.coverVersionOf(it) } ?: 0L
            val config = prefs?.let { WidgetConfig.from(it) } ?: WidgetConfig.STANDARD
            val cover = CoverCache.load(context, version)?.asImageBitmap()
            StudioData(config, state, cover)
        }

    private suspend fun applyConfig(context: Context, ids: List<Int>, config: WidgetConfig) {
        val manager = GlanceAppWidgetManager(context)
        val widget = SpotilolPlayerWidget()
        for (id in ids) {
            runCatching {
                val glanceId = manager.getGlanceIdBy(id)
                updateAppWidgetState(context, glanceId) { prefs -> config.writeTo(prefs) }
                widget.update(context, glanceId)
            }
        }
    }
}

private data class StudioData(
    val config: WidgetConfig,
    val state: WidgetState,
    val cover: ImageBitmap?
)

@Composable
private fun StudioScreen(
    isNew: Boolean,
    load: suspend () -> StudioData,
    onApply: (WidgetConfig) -> Unit,
    onCancel: () -> Unit
) {
    var data by remember { mutableStateOf<StudioData?>(null) }
    var config by remember { mutableStateOf<WidgetConfig?>(null) }

    LaunchedEffect(Unit) {
        val loaded = load()
        data = loaded
        config = loaded.config
    }

    val loaded = data
    val current = config
    if (loaded == null || current == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        StudioHeader(onClose = onCancel)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WidgetPreview(current, loaded.state, loaded.cover)
            LayoutSection(current) { config = it }
            InfoSection(current) { config = it }
            ControlsSection(current) { config = it }
            AppearanceSection(current) { config = it }
            Spacer(Modifier.height(4.dp))
        }
        StudioActions(
            isNew = isNew,
            onApply = { onApply(current) },
            onCancel = onCancel
        )
    }
}
@Composable
private fun StudioHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = stringResource(R.string.widget_studio_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.widget_studio_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = TablerIcons.X,
                contentDescription = stringResource(R.string.widget_studio_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudioActions(isNew: Boolean, onApply: () -> Unit, onCancel: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.widget_studio_cancel))
            }
            Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        if (isNew) R.string.widget_studio_add else R.string.widget_studio_apply
                    )
                )
            }
        }
    }
}

@Composable
private fun NoWidgetScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = TablerIcons.Devices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.widget_studio_no_widget),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.widget_studio_no_widget_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onClose) {
            Text(stringResource(R.string.widget_studio_close))
        }
    }
}

@Composable
private fun LayoutSection(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    SettingSectionCard(
        title = stringResource(R.string.widget_studio_layout),
        icon = TablerIcons.LayoutGrid
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val options = listOf(WidgetLayout.COMPACT, WidgetLayout.STANDARD, WidgetLayout.ART)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, layout ->
                    SegmentedButton(
                        selected = config.layout == layout,
                        onClick = { onChange(WidgetConfig.preset(layout)) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        label = { Text(layoutLabel(layout)) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.widget_studio_layout_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoSection(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    SettingSectionCard(
        title = stringResource(R.string.widget_studio_info),
        icon = TablerIcons.InfoCircle
    ) {
        StudioSwitch(
            titleRes = R.string.widget_studio_info_cover,
            descRes = R.string.widget_studio_info_cover_desc,
            icon = TablerIcons.Photo,
            checked = config.showCover
        ) { onChange(config.copy(showCover = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_info_title,
            descRes = R.string.widget_studio_info_title_desc,
            icon = TablerIcons.Typography,
            checked = config.showTitle
        ) { onChange(config.copy(showTitle = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_info_artist,
            descRes = R.string.widget_studio_info_artist_desc,
            icon = TablerIcons.User,
            checked = config.showArtist
        ) { onChange(config.copy(showArtist = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_info_progress,
            descRes = R.string.widget_studio_info_progress_desc,
            icon = TablerIcons.ArrowsHorizontal,
            checked = config.showProgress
        ) { onChange(config.copy(showProgress = it)) }
    }
}

@Composable
private fun ControlsSection(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    SettingSectionCard(
        title = stringResource(R.string.widget_studio_controls),
        icon = TablerIcons.PlayerPlay
    ) {
        StudioSwitch(
            titleRes = R.string.widget_studio_controls_play,
            descRes = R.string.widget_studio_controls_play_desc,
            icon = TablerIcons.PlayerPlay,
            checked = config.showPlayPause
        ) { onChange(config.copy(showPlayPause = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_controls_skip,
            descRes = R.string.widget_studio_controls_skip_desc,
            icon = TablerIcons.PlayerTrackNext,
            checked = config.showSkip
        ) { onChange(config.copy(showSkip = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_controls_shuffle,
            descRes = R.string.widget_studio_controls_shuffle_desc,
            icon = TablerIcons.ArrowsSort,
            checked = config.showShuffleRepeat
        ) { onChange(config.copy(showShuffleRepeat = it)) }
        StudioDivider()
        StudioSwitch(
            titleRes = R.string.widget_studio_controls_favorite,
            descRes = R.string.widget_studio_controls_favorite_desc,
            icon = TablerIcons.Heart,
            checked = config.showFavorite
        ) { onChange(config.copy(showFavorite = it)) }
    }
}

@Composable
private fun AppearanceSection(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    SettingSectionCard(
        title = stringResource(R.string.widget_studio_appearance),
        icon = TablerIcons.Palette
    ) {
        StudioSwitch(
            titleRes = R.string.widget_studio_cover_bg,
            descRes = R.string.widget_studio_cover_bg_desc,
            icon = TablerIcons.Photo,
            checked = config.coverBackground
        ) { onChange(config.copy(coverBackground = it)) }
    }
}

@Composable
private fun StudioSwitch(
    titleRes: Int,
    descRes: Int,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingSwitchTile(
        title = stringResource(titleRes),
        subtitle = stringResource(descRes),
        icon = icon,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
private fun StudioDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 44.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    )
}

@Composable
private fun layoutLabel(layout: WidgetLayout): String = stringResource(
    when (layout) {
        WidgetLayout.COMPACT -> R.string.widget_studio_layout_compact
        WidgetLayout.STANDARD -> R.string.widget_studio_layout_standard
        WidgetLayout.ART -> R.string.widget_studio_layout_art
    }
)

@Composable
private fun WidgetPreview(config: WidgetConfig, state: WidgetState, cover: ImageBitmap?) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        ) {
            Icon(
                imageVector = TablerIcons.DeviceDesktop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.widget_studio_preview),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (config.layout) {
                    WidgetLayout.ART -> ArtPreview(config, state, cover)
                    WidgetLayout.COMPACT -> CompactPreview(config, state, cover)
                    WidgetLayout.STANDARD -> StandardPreview(config, state, cover)
                }
            }
        }
    }
}

@Composable
private fun PreviewBackground(config: WidgetConfig, cover: ImageBitmap?, scrim: Boolean) {
    val scheme = MaterialTheme.colorScheme
    if (config.useCoverArt && cover != null) {
        Image(
            bitmap = cover,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (scrim) {
            Box(modifier = Modifier.fillMaxSize().background(scheme.surface.copy(alpha = 0.80f)))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(scheme.surface))
    }
}

@Composable
private fun StandardPreview(config: WidgetConfig, state: WidgetState, cover: ImageBitmap?) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        PreviewBackground(config, cover, scrim = true)
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showThumbnail) {
                    PreviewCover(cover, 46.dp, 10.dp)
                    Spacer(Modifier.width(11.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (config.showTitle) {
                        Text(
                            text = previewTitle(state),
                            color = scheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (config.showArtist) {
                        Text(
                            text = previewArtist(state),
                            color = scheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (config.showProgress) {
                        if (config.showTitle || config.showArtist) Spacer(Modifier.height(7.dp))
                        PreviewProgress(state)
                    }
                }
                if (state.offline) {
                    Spacer(Modifier.width(8.dp))
                    OfflineBadgePreview()
                } else if (config.showFavorite) {
                    Spacer(Modifier.width(8.dp))
                    PreviewCircle(
                        icon = TablerIcons.Heart,
                        dimension = 26.dp,
                        iconSize = 14.dp
                    )
                }
            }
            if (config.hasControls) {
                Spacer(Modifier.height(6.dp))
                PreviewControls(config, state)
            }
        }
    }
}

@Composable
private fun CompactPreview(config: WidgetConfig, state: WidgetState, cover: ImageBitmap?) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        PreviewBackground(config, cover, scrim = true)
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (config.showThumbnail) {
                PreviewCover(cover, 34.dp, 9.dp)
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (config.showTitle) {
                    Text(
                        text = previewTitle(state),
                        color = scheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (config.showArtist) {
                    Text(
                        text = previewArtist(state),
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (config.showSkip) {
                Spacer(Modifier.width(6.dp))
                PreviewCircle(TablerIcons.PlayerSkipBack, 26.dp, 14.dp)
            }
            if (config.showPlayPause) {
                Spacer(Modifier.width(6.dp))
                PreviewCircle(
                    icon = if (state.playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                    dimension = 30.dp,
                    iconSize = 15.dp,
                    background = scheme.primary,
                    tint = scheme.onPrimary
                )
            }
            if (config.showSkip) {
                Spacer(Modifier.width(6.dp))
                PreviewCircle(TablerIcons.PlayerSkipForward, 26.dp, 14.dp)
            }
        }
    }
}

@Composable
private fun ArtPreview(config: WidgetConfig, state: WidgetState, cover: ImageBitmap?) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize()) {
        PreviewBackground(config, cover, scrim = false)
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            if (config.showPlayPause) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(scheme.surface.copy(alpha = 0.70f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                        contentDescription = null,
                        tint = scheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (config.showTitle) {
                if (config.showPlayPause) Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(scheme.surface.copy(alpha = 0.80f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = previewTitle(state),
                        color = scheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewControls(config: WidgetConfig, state: WidgetState) {
    val scheme = MaterialTheme.colorScheme
    val showShuffle = config.showShuffleRepeat
    val showRepeat = config.showShuffleRepeat && !state.offline
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))
        if (showShuffle) {
            PreviewCircle(TablerIcons.ArrowsSort, 28.dp, 15.dp)
            Spacer(Modifier.weight(1f))
        }
        if (config.showSkip) {
            PreviewCircle(TablerIcons.PlayerSkipBack, 28.dp, 15.dp)
            Spacer(Modifier.weight(1f))
        }
        if (config.showPlayPause) {
            PreviewCircle(
                icon = if (state.playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                dimension = 36.dp,
                iconSize = 17.dp,
                background = scheme.primary,
                tint = scheme.onPrimary
            )
            Spacer(Modifier.weight(1f))
        }
        if (config.showSkip) {
            PreviewCircle(TablerIcons.PlayerSkipForward, 28.dp, 15.dp)
            Spacer(Modifier.weight(1f))
        }
        if (showRepeat) {
            PreviewCircle(TablerIcons.Refresh, 28.dp, 15.dp)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun OfflineBadgePreview() {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .background(scheme.onSurface.copy(alpha = 0.14f), RoundedCornerShape(11.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.widget_badge_offline),
            color = scheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun PreviewCover(cover: ImageBitmap?, dimension: Dp, radius: Dp) {
    val scheme = MaterialTheme.colorScheme
    if (cover != null) {
        Image(
            bitmap = cover,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(dimension).clip(RoundedCornerShape(radius))
        )
    } else {
        Box(
            modifier = Modifier
                .size(dimension)
                .background(scheme.onSurface.copy(alpha = 0.20f), RoundedCornerShape(radius)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notification),
                contentDescription = null,
                tint = scheme.onSurface.copy(alpha = 0.60f),
                modifier = Modifier.size(dimension / 2)
            )
        }
    }
}

@Composable
private fun PreviewCircle(
    icon: ImageVector,
    dimension: Dp,
    iconSize: Dp,
    background: Color? = null,
    tint: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(dimension)
            .background(background ?: scheme.onSurface.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: scheme.onSurface,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun PreviewProgress(state: WidgetState) {
    val scheme = MaterialTheme.colorScheme
    val progress = if (state.duration > 0L) state.progress else 0.4f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(scheme.onSurface.copy(alpha = 0.20f), RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                .height(3.dp)
                .background(scheme.primary, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun previewTitle(state: WidgetState): String =
    state.title.ifBlank { stringResource(R.string.widget_studio_preview_sample_title) }

@Composable
private fun previewArtist(state: WidgetState): String =
    state.artist.ifBlank { stringResource(R.string.widget_studio_preview_sample_artist) }
