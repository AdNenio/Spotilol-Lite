package com.project.lol.ui.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import com.project.lol.searchEngine.GenericSearchEngine
import com.project.lol.searchEngine.SearchableFieldExtractor
import com.project.lol.util.Logger
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat
import com.project.lol.R
import com.project.lol.offline.DownloadFolder
import com.project.lol.offline.OfflineSong
import com.project.lol.offline.OfflineStore
import com.project.lol.service.OfflineMediaService
import com.project.lol.timer.AppQuit
import com.project.lol.timer.SleepTimerAction
import com.project.lol.timer.SleepTimerManager
import com.project.lol.ui.components.SettingsDialog
import com.project.lol.ui.components.SleepTimerDialog
import com.project.lol.ui.theme.schemeFromSeed
import com.project.lol.util.BuildInfo
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.CloudOff
import compose.icons.tablericons.Folder
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Menu2
import compose.icons.tablericons.Music
import compose.icons.tablericons.PictureInPicture
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerSkipBack
import compose.icons.tablericons.PlayerSkipForward
import compose.icons.tablericons.Search
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Trash
import compose.icons.tablericons.Volume
import compose.icons.tablericons.Volume2
import compose.icons.tablericons.Volume3
import compose.icons.tablericons.X
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private enum class RepeatMode { OFF, ALL, ONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(
    modifier: Modifier = Modifier,
    prefs: SharedPreferences,
    materialYou: Boolean,
    onMaterialYouChange: (Boolean) -> Unit,
    amoledTheme: Boolean,
    onAmoledThemeChange: (Boolean) -> Unit,
    hideTopBar: Boolean,
    onHideTopBarChange: (Boolean) -> Unit,
    landscapeMode: Boolean,
    onLandscapeModeChange: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    themePreset: String,
    onThemePresetChange: (String) -> Unit,
    onOfflineModeChange: (Boolean) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onLoadProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onClearCache: () -> Unit,
    onClearData: () -> Unit,
    onClearThirdParty: () -> Unit,
    pipActive: Boolean,
    onEnterPip: () -> Unit,
    onPlaybackStateChange: (Boolean) -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var settingsDialogOpen by remember { mutableStateOf(false) }
    var showQuickMenu by remember { mutableStateOf(false) }

    var songs by remember { mutableStateOf<List<OfflineSong>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var playerSong by remember { mutableStateOf<OfflineSong?>(null) }
    var pendingDelete by remember { mutableStateOf<OfflineSong?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }
    var scrubMs by remember { mutableIntStateOf(-1) }
    var shuffleOn by remember { mutableStateOf(false) }
    var shuffleOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var shufflePos by remember { mutableIntStateOf(-1) }
    var repeatMode by remember { mutableStateOf(RepeatMode.OFF) }
    var playerExpanded by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerInput by remember { mutableStateOf("") }
    var sleepTimerState by remember { mutableStateOf(SleepTimerManager.state()) }

    val mediaPlayer = remember { MediaPlayer() }
    val audioFocus = remember { OfflineAudioFocus(context) }
    var resumeAfterFocus by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<OfflineSong>?>(null) }

    val searchEngine = remember { GenericSearchEngine<OfflineSong>(maxResult = 100) }
    val songExtractor = remember {
        SearchableFieldExtractor<OfflineSong> { song ->
            arrayOf(song.title, song.artist, song.album, song.ytAlbum, song.ytArtist)
        }
    }

    LaunchedEffect(searchQuery, songs) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            searchResults = null
        } else {
            delay(300.milliseconds)
            searchResults = withContext(Dispatchers.Default) {
                searchEngine.filter(songs, q, songExtractor)
            }
        }
    }
    val visibleSongs = searchResults ?: songs

    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: ""
    }

    fun syncService() {
        val song = playerSong ?: return
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, OfflineMediaService::class.java).apply {
                    putExtra("title", song.title)
                    putExtra("artist", song.artist)
                    putExtra("album", song.album)
                    putExtra("duration", durationMs.toLong())
                    putExtra("playing", isPlaying)
                    putExtra("position", positionMs.toLong())
                    putExtra("coverPath", song.coverFile?.absolutePath)
                    putExtra("shuffle", shuffleOn)
                }
            )
        }
    }

    fun handleSleepTimerExpire() {
        when (SleepTimerManager.action) {
            SleepTimerAction.PAUSE -> {
                runCatching {
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                }
                isPlaying = false
                positionMs = runCatching { mediaPlayer.currentPosition }.getOrDefault(positionMs)
                syncService()
                OfflineMediaService.instance?.updatePlaying(false, positionMs.toLong())
            }

            SleepTimerAction.QUIT -> AppQuit.quit(context)
        }
    }

    fun play(index: Int) {
        audioFocus.acquire()
        playAt(mediaPlayer, context, songs, index, { currentIndex = it }, { playerSong = it }, { isPlaying = it }, { durationMs = it }, { positionMs = it })
        if (shuffleOn) {
            val key = songs.getOrNull(index)?.let { songKey(it) }
            val pos = shuffleOrder.indexOf(key)
            if (pos >= 0) shufflePos = pos
        }
        syncService()
    }

    fun togglePlayPause() {
        runCatching {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
            } else if (durationMs > 0) {
                audioFocus.acquire()
                mediaPlayer.start()
                isPlaying = true
            }
        }
        syncService()
    }

    DisposableEffect(audioFocus) {
        audioFocus.onSuspend = {
            resumeAfterFocus = isPlaying
            if (isPlaying) {
                runCatching { mediaPlayer.pause() }
                isPlaying = false
                syncService()
            }
        }
        audioFocus.onResume = {
            if (resumeAfterFocus) {
                resumeAfterFocus = false
                runCatching {
                    if (durationMs > 0) {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                }
                syncService()
            }
        }
        onDispose {
            audioFocus.onSuspend = null
            audioFocus.onResume = null
            resumeAfterFocus = false
            audioFocus.abandon()
        }
    }

    fun ensureShuffleOrder() {
        val keys = songs.map { songKey(it) }
        if (shuffleOrder.size == keys.size && shuffleOrder.all { it in keys }) return
        val currentKey = songs.getOrNull(currentIndex)?.let { songKey(it) }
        shuffleOrder = buildList {
            if (currentKey != null) add(currentKey)
            addAll(keys.filter { it != currentKey }.shuffled())
        }
        shufflePos = if (currentKey != null) 0 else -1
    }

    fun toggleShuffle() {
        shuffleOn = !shuffleOn
        if (shuffleOn) {
            shuffleOrder = emptyList()
            shufflePos = -1
            ensureShuffleOrder()
        }
    }

    fun cycleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun step(delta: Int) {
        if (songs.isEmpty()) return
        if (shuffleOn) {
            ensureShuffleOrder()
            val n = shuffleOrder.size
            if (n > 0) {
                val pos = if (shufflePos < 0) 0 else ((shufflePos + delta) % n + n) % n
                val index = songs.indexOfFirst { songKey(it) == shuffleOrder[pos] }
                if (index >= 0) {
                    shufflePos = pos
                    play(index)
                    return
                }
            }
        }
        val next = ((currentIndex + delta) % songs.size + songs.size) % songs.size
        play(next)
    }

    fun seekTo(position: Long) {
        if (durationMs <= 0) return
        runCatching { mediaPlayer.seekTo(position.toInt()) }
        positionMs = position.toInt()
        OfflineMediaService.instance?.updatePosition(position)
    }

    fun stopAndClear() {
        runCatching {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            mediaPlayer.reset()
        }
        audioFocus.abandon()
        resumeAfterFocus = false
        isPlaying = false
        currentIndex = -1
        positionMs = 0
        durationMs = 0
        runCatching { context.stopService(Intent(context, OfflineMediaService::class.java)) }
    }

    fun performDelete(song: OfflineSong) {
        val index = songs.indexOfFirst { it.id == song.id && it.uri == song.uri }
        if (index == -1) return
        if (index == currentIndex) {
            stopAndClear()
        } else if (index < currentIndex) {
            currentIndex -= 1
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) { OfflineStore.deleteSong(context, song) }
            songs = songs.filterNot { it.id == song.id && it.uri == song.uri }
            if (!ok) {
                Toast.makeText(context, resources.getString(R.string.offline_toast_could_not_delete_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performDeleteAll() {
        val all = songs
        if (all.isEmpty()) return
        stopAndClear()
        searchQuery = ""
        searchResults = null
        songs = emptyList()
        scope.launch {
            val failed = withContext(Dispatchers.IO) {
                all.count { song -> !OfflineStore.deleteSong(context, song) }
            }
            if (failed > 0) {
                Toast.makeText(
                    context,
                    if (failed == 1) resources.getString(R.string.offline_toast_could_not_delete_one_file) else resources.getString(R.string.offline_toast_could_not_delete_files, failed),
                    Toast.LENGTH_SHORT
                ).show()
                songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
            }
        }
    }

    BackHandler(enabled = settingsDialogOpen || playerExpanded || searchFocused || searchQuery.isNotBlank()) {
        when {
            settingsDialogOpen -> settingsDialogOpen = false
            playerExpanded -> playerExpanded = false
            searchFocused -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            else -> searchQuery = ""
        }
    }

    DisposableEffect(Unit) {
        val ctrl = object : OfflineMediaService.OfflineController {
            override fun onPlayFromSearch(query: String?) {
                if (query.isNullOrBlank()) {
                    if (!isPlaying) {
                        if (currentIndex >= 0) togglePlayPause() else if (songs.isNotEmpty()) play(0)
                    }
                } else {
                    val match = searchEngine.filter(songs, query.take(1024), songExtractor).firstOrNull()
                    val index = songs.indexOf(match)
                    if (index >= 0) play(index)
                }
            }
            override fun onPlayPause() = togglePlayPause()
            override fun onNext() = step(1)
            override fun onPrev() = step(-1)
            override fun onStop() {
                stopAndClear()
                runCatching { context.stopService(Intent(context, OfflineMediaService::class.java)) }
            }

            override fun onSeekTo(position: Long) = seekTo(position)
            override fun onToggleShuffle() = toggleShuffle()
        }
        OfflineMediaService.controller = ctrl
        onDispose {
            if (OfflineMediaService.controller === ctrl) OfflineMediaService.controller = null
            runCatching { mediaPlayer.release() }
            runCatching { context.stopService(Intent(context, OfflineMediaService::class.java)) }
        }
    }

    DisposableEffect(Unit) {
        SleepTimerManager.loadAction(context)
        val releaseHost = SleepTimerManager.registerHost(
            stateChange = { sleepTimerState = SleepTimerManager.state() },
            expire = { handleSleepTimerExpire() }
        )
        onDispose { releaseHost() }
    }

    DisposableEffect(mediaPlayer) {
        mediaPlayer.setOnCompletionListener {
            if (SleepTimerManager.isEndOfSongArmed) {
                SleepTimerManager.onTrackCompleted()
                return@setOnCompletionListener
            }
            if (repeatMode == RepeatMode.ONE && currentIndex in songs.indices) {
                play(currentIndex)
                return@setOnCompletionListener
            }
            val advanced = if (shuffleOn) {
                ensureShuffleOrder()
                if (shufflePos in 0 until shuffleOrder.size - 1) {
                    step(1)
                    true
                } else if (repeatMode == RepeatMode.ALL && shuffleOrder.isNotEmpty()) {
                    shufflePos = -1
                    step(1)
                    true
                } else {
                    false
                }
            } else {
                val index = currentIndex
                if (index in 0 until songs.lastIndex) {
                    play(index + 1)
                    true
                } else if (repeatMode == RepeatMode.ALL && songs.isNotEmpty()) {
                    play(0)
                    true
                } else {
                    false
                }
            }
            if (!advanced) {
                isPlaying = false
                positionMs = 0
                OfflineMediaService.instance?.updatePlaying(false, 0)
            }
        }
        onDispose { }
    }

    LaunchedEffect(Unit) {
        songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
        loading = false
    }

    LaunchedEffect(isPlaying, currentIndex) {
        while (isPlaying) {
            runCatching { positionMs = mediaPlayer.currentPosition }
            OfflineMediaService.instance?.updatePosition(positionMs.toLong())
            delay(500.milliseconds)
        }
    }

    LaunchedEffect(shuffleOn) {
        OfflineMediaService.instance?.updateShuffle(shuffleOn)
    }

    LaunchedEffect(isPlaying) {
        onPlaybackStateChange(isPlaying)
    }

    SettingsDialog(
        visible = settingsDialogOpen,
        onClose = { settingsDialogOpen = false },
        prefs = prefs,
        materialYou = materialYou,
        onMaterialYouChange = onMaterialYouChange,
        amoledThemeState = amoledTheme,
        onAmoledThemeChange = onAmoledThemeChange,
        hideTopBar = hideTopBar,
        onHideTopBarChange = onHideTopBarChange,
        landscapeMode = landscapeMode,
        onLandscapeModeChange = onLandscapeModeChange,
        keepScreenOn = keepScreenOn,
        onKeepScreenOnChange = onKeepScreenOnChange,
        themePreset = themePreset,
        onThemePresetChange = onThemePresetChange,
        onOfflineModeChange = onOfflineModeChange,
        onSaveProfile = onSaveProfile,
        onLoadProfile = onLoadProfile,
        onDeleteProfile = onDeleteProfile,
        onClearCache = onClearCache,
        onClearData = onClearData,
        onClearThirdParty = onClearThirdParty,
        onDebugToggle = {},
        blockServiceWorker = prefs.getBoolean("BlockServiceWorker", true),
        onBlockServiceWorkerChange = { enabled ->
            prefs.edit().putBoolean("BlockServiceWorker", enabled).apply()
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                if (!hideTopBar && !pipActive) {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.offline_app_name),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.offline_version, versionName, BuildInfo.id),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { settingsDialogOpen = true }) {
                                Icon(
                                    imageVector = TablerIcons.Menu2,
                                    contentDescription = stringResource(R.string.offline_desc_settings),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSleepTimerDialog = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_timer),
                                    contentDescription = stringResource(R.string.timer_desc_open),
                                    tint = if (sleepTimerState.active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = onExit) {
                                Icon(
                                    imageVector = TablerIcons.Logout,
                                    contentDescription = stringResource(R.string.offline_desc_exit_offline_mode),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                            .padding(top = if (hideTopBar) 56.dp else 10.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = when {
                                loading -> stringResource(R.string.offline_status_loading)
                                searchQuery.isNotBlank() -> {
                                    val n = visibleSongs.size
                                    if (n == 0) {
                                        stringResource(R.string.offline_status_no_results_for, searchQuery.trim())
                                    } else if (n == 1) {
                                        stringResource(R.string.offline_status_one_result_for, searchQuery.trim())
                                    } else {
                                        stringResource(R.string.offline_status_results_for, n, searchQuery.trim())
                                    }
                                }
                                songs.isEmpty() -> stringResource(R.string.offline_status_no_downloads_yet)
                                songs.size == 1 -> stringResource(R.string.offline_status_one_song_available_offline)
                                else -> stringResource(R.string.offline_status_songs_available_offline, songs.size)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!loading && songs.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OfflineSearchField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        onSearchFocusChange = { searchFocused = it }
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                CompactIconButton(
                                    icon = TablerIcons.Trash,
                                    contentDescription = stringResource(R.string.offline_desc_delete_all_downloads),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    onClick = { confirmDeleteAll = true },
                                    boxSize = 40.dp,
                                    iconSize = 20.dp
                                )
                            }
                        }
                    }

                    when {
                        loading -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        searchQuery.isNotBlank() && visibleSongs.isEmpty() -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = TablerIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.offline_empty_no_results),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.offline_empty_nothing_matches, searchQuery.trim()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        songs.isEmpty() -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = TablerIcons.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.offline_empty_nothing_here_yet),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.offline_empty_download_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        else -> LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 112.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(visibleSongs, key = { "${it.id}-${it.uri}" }) { song ->
                                val index = songs.indexOfFirst { it.id == song.id && it.uri == song.uri }
                                OfflineSongRow(
                                    song = song,
                                    isCurrent = index == currentIndex,
                                    onClick = {
                                        if (index == currentIndex) {
                                            if (durationMs > 0 || mediaPlayer.isPlaying) {
                                                togglePlayPause()
                                            } else {
                                                play(index)
                                            }
                                        } else {
                                            play(index)
                                        }
                                    },
                                    onLocate = {
                                        if (!DownloadFolder.openTrack(context, song.uri)) {
                                            Toast.makeText(context, song.uri.lastPathSegment ?: song.title, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onDelete = { pendingDelete = song }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = currentIndex >= 0,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(220)) + fadeIn(tween(220)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(180)) + fadeOut(tween(180))
                ) {
                    val song = playerSong
                    if (song != null) {
                        NowPlayingBar(
                            song = song,
                            playing = isPlaying,
                            positionMs = positionMs,
                            durationMs = durationMs,
                            scrubMs = scrubMs,
                            onScrub = { scrubMs = it },
                            onScrubFinished = {
                                if (scrubMs >= 0) seekTo(scrubMs.toLong())
                                scrubMs = -1
                            },
                            onTogglePlay = { togglePlayPause() },
                            onPrev = { step(-1) },
                            onNext = { step(1) },
                            onClose = { stopAndClear() },
                            shuffleOn = shuffleOn,
                            onToggleShuffle = { toggleShuffle() },
                            repeatMode = repeatMode,
                            onCycleRepeat = { cycleRepeat() },
                            onExpand = { playerExpanded = true }
                        )
                    }
                }

                val expandedSong = playerSong
                AnimatedVisibility(
                    visible = playerExpanded && expandedSong != null,
                    modifier = Modifier.fillMaxSize(),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(tween(240)) + scaleIn(
                        initialScale = 0.92f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(220)
                    ) + fadeOut(tween(180)) + scaleOut(
                        targetScale = 0.94f,
                        transformOrigin = TransformOrigin(0.5f, 1f),
                        animationSpec = tween(220)
                    )
                ) {
                    if (expandedSong != null) {
                        FullScreenPlayer(
                            song = expandedSong,
                            playing = isPlaying,
                            positionMs = positionMs,
                            durationMs = durationMs,
                            scrubMs = scrubMs,
                            onScrub = { scrubMs = it },
                            onScrubFinished = {
                                if (scrubMs >= 0) seekTo(scrubMs.toLong())
                                scrubMs = -1
                            },
                            onTogglePlay = { togglePlayPause() },
                            onPrev = { step(-1) },
                            onNext = { step(1) },
                            onCollapse = { playerExpanded = false },
                            onClose = {
                                playerExpanded = false
                                stopAndClear()
                            },
                            shuffleOn = shuffleOn,
                            onToggleShuffle = { toggleShuffle() },
                            repeatMode = repeatMode,
                            onCycleRepeat = { cycleRepeat() },
                            onEnterPip = onEnterPip
                        )
                    }
                }

                if (pipActive) {
                    PipContent(song = playerSong)
                }

                if (hideTopBar && !pipActive) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(44.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .clickable { showQuickMenu = !showQuickMenu },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_launcher_playstore),
                                contentDescription = stringResource(R.string.offline_desc_quick_actions),
                                tint = Color.Unspecified,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (showQuickMenu) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                offset = IntOffset(0, with(LocalDensity.current) { 64.dp.toPx() }.toInt()),
                                onDismissRequest = { showQuickMenu = false }
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Column(modifier = Modifier.width(220.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showQuickMenu = false
                                                    settingsDialogOpen = true
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Settings,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                text = stringResource(R.string.offline_menu_settings),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = TablerIcons.Menu2,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showQuickMenu = false
                                                    onExit()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Logout,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                text = stringResource(R.string.offline_menu_exit_offline_mode),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    }

    val songToDelete = pendingDelete
    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.offline_dialog_delete_song_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.offline_dialog_delete_song_message, songToDelete.title, songToDelete.artist.ifBlank { stringResource(R.string.offline_unknown_artist) }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    performDelete(songToDelete)
                }) {
                    Text(
                        text = stringResource(R.string.offline_dialog_delete),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.offline_dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (confirmDeleteAll) {
        val total = songs.size
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.offline_dialog_delete_all_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (total == 1) {
                        stringResource(R.string.offline_dialog_delete_all_message_one)
                    } else {
                        stringResource(R.string.offline_dialog_delete_all_message_many, total)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAll = false
                    performDeleteAll()
                }) {
                    Text(
                        text = stringResource(R.string.offline_dialog_delete_all),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.offline_dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            state = sleepTimerState,
            inputText = sleepTimerInput,
            onInputChange = { sleepTimerInput = it },
            onStartCountdown = { minutes ->
                showSleepTimerDialog = false
                SleepTimerManager.startCountdown(minutes)
            },
            onStartEndOfSong = {
                showSleepTimerDialog = false
                SleepTimerManager.startEndOfSong()
            },
            onActionChange = { SleepTimerManager.setAction(context, it) },
            onCancelTimer = {
                showSleepTimerDialog = false
                SleepTimerManager.cancel()
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}
            }
        }
    }

private fun playAt(
    mediaPlayer: MediaPlayer,
    context: android.content.Context,
    songs: List<OfflineSong>,
    index: Int,
    setCurrentIndex: (Int) -> Unit,
    setPlayerSong: (OfflineSong) -> Unit,
    setPlaying: (Boolean) -> Unit,
    setDuration: (Int) -> Unit,
    setPosition: (Int) -> Unit,
) {
    val song = songs.getOrNull(index) ?: return
    runCatching {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, song.uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        setCurrentIndex(index)
        setPlayerSong(song)
        setPlaying(true)
        setDuration(mediaPlayer.duration)
        setPosition(0)
    }.onFailure {
        setPlaying(false)
    }
}

private class OfflineAudioFocus(context: Context) {
    private companion object {
        const val TAG = "offline.focus"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var request: AudioFocusRequest? = null
    private var held = false
    private var suspended = false

    var onSuspend: (() -> Unit)? = null
    var onResume: (() -> Unit)? = null

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        val name = when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> "gain"
            AudioManager.AUDIOFOCUS_LOSS -> "loss"
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "loss transient"
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "loss transient can duck"
            else -> "unknown $change"
        }
        Logger.i(TAG, "focus change: $name")
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> if (suspended) {
                suspended = false
                onResume?.invoke()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                suspended = true
                onSuspend?.invoke()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                suspended = false
                abandon()
                onSuspend?.invoke()
            }
        }
    }

    fun acquire(): Boolean {
        if (held) return true
        val req = request ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(listener, handler)
            .build()
            .also { request = it }
        held = audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Logger.i(TAG, "focus acquire: granted=$held")
        return held
    }

    fun abandon() {
        if (!held && request == null) return
        Logger.i(TAG, "focus abandon: held=$held")
        held = false
        val req = request ?: return
        runCatching { audioManager.abandonAudioFocusRequest(req) }
    }
}

@Composable
private fun CompactIconButton(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    boxSize: Dp = 32.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun CompactIconButton(
    painter: Painter,
    contentDescription: String?,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    boxSize: Dp = 32.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

private fun songKey(song: OfflineSong): String = "${song.id}-${song.uri}"

@Composable
private fun OfflineSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = TablerIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onSearchFocusChange(it.isFocused) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.offline_search_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )
            if (value.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                CompactIconButton(
                    icon = TablerIcons.X,
                    contentDescription = stringResource(R.string.offline_desc_clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onValueChange("") },
                    boxSize = 28.dp,
                    iconSize = 15.dp
                )
            }
        }
    }
}

@Composable
private fun SeekBar(
    positionMs: Int,
    durationMs: Int,
    scrubbing: Boolean,
    onScrub: (Int) -> Unit,
    onScrubFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = durationMs.coerceAtLeast(1)
    CompactBar(
        fraction = positionMs.coerceIn(0, total).toFloat() / total.toFloat(),
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        progressColor = MaterialTheme.colorScheme.primary,
        active = scrubbing,
        onScrub = { onScrub((it * total).toInt().coerceIn(0, total)) },
        onScrubFinished = onScrubFinished,
        modifier = modifier
    )
}

@Composable
private fun VolumeBar(
    fraction: Float,
    onScrub: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    CompactBar(
        fraction = fraction,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        progressColor = MaterialTheme.colorScheme.onSurface,
        onScrub = onScrub,
        onScrubFinished = {},
        modifier = modifier
    )
}

@Composable
private fun CompactBar(
    fraction: Float,
    trackColor: Color,
    progressColor: Color,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false
) {
    val clamped = fraction.coerceIn(0f, 1f)
    var widthPx by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }

    fun fractionAt(x: Float): Float =
        if (widthPx <= 0) 0f else (x / widthPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(widthPx) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        onScrub(fractionAt(offset.x))
                    },
                    onDragEnd = {
                        dragging = false
                        onScrubFinished()
                    },
                    onDragCancel = {
                        dragging = false
                        onScrubFinished()
                    }
                ) { change, _ ->
                    change.consume()
                    onScrub(fractionAt(change.position.x))
                }
            }
            .pointerInput(widthPx) {
                detectTapGestures { offset ->
                    onScrub(fractionAt(offset.x))
                    onScrubFinished()
                }
            }
            .drawBehind {
                val centerY = size.height / 2f
                val trackHeight = 3.dp.toPx()
                val radius = trackHeight / 2f
                val progressWidth = size.width * clamped
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(progressWidth, trackHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )
                val thumbRadius = (if (dragging || active) 6.dp else 4.dp).toPx()
                drawCircle(
                    color = progressColor,
                    radius = thumbRadius,
                    center = Offset(
                        x = progressWidth.coerceIn(thumbRadius, size.width - thumbRadius),
                        y = centerY
                    )
                )
            }
    )
}

@Composable
private fun OfflineSongRow(
    song: OfflineSong,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLocate: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        SongCover(song = song, size = 44.dp, corner = 8.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val unknownArtist = stringResource(R.string.offline_unknown_artist)
            val explicitLabel = stringResource(R.string.offline_label_explicit)
            val subtitle = buildString {
                append(song.artist.ifBlank { unknownArtist })
                song.album.ifBlank { "" }.takeIf { it.isNotBlank() }?.let { append(" • $it") }
                song.durationSec?.takeIf { it > 0 }?.let { append(" • ${formatSeconds(it)}") }
                if (song.explicit) append(" • $explicitLabel")
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CompactIconButton(
            icon = TablerIcons.Folder,
            contentDescription = stringResource(R.string.offline_desc_locate_track),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onLocate,
            boxSize = 30.dp,
            iconSize = 17.dp
        )
        CompactIconButton(
            icon = TablerIcons.Trash,
            contentDescription = stringResource(R.string.offline_desc_delete),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            onClick = onDelete,
            boxSize = 30.dp,
            iconSize = 17.dp
        )
    }
}

@Composable
private fun SongCover(song: OfflineSong, size: Dp, corner: Dp) {
    CoverBox(
        song = song,
        corner = corner,
        iconFraction = 0.5f,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun CoverBox(
    song: OfflineSong,
    corner: Dp,
    modifier: Modifier = Modifier,
    iconFraction: Float = 0.5f
) {
    val context = LocalContext.current
    var bitmap by remember(song.id, song.uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(song.id, song.uri) {
        bitmap = withContext(Dispatchers.IO) { decodeCover(context, song) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = TablerIcons.Music,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxSize(iconFraction)
            )
        }
    }
}

private fun decodeCover(context: android.content.Context, song: OfflineSong): Bitmap? {
    song.coverFile?.let { file ->
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = maxOf(1, minOf(bounds.outWidth, bounds.outHeight) / 256)
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        }.getOrNull()?.let { return it }
    }
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, song.uri)
            retriever.embeddedPicture?.let { data ->
                BitmapFactory.decodeByteArray(data, 0, data.size)
            }
        } finally {
            runCatching { retriever.release() }
        }
    }.getOrNull()
}

private fun extractSeedColor(bitmap: Bitmap): Color? {
    val sample = 32
    val scaled = Bitmap.createScaledBitmap(bitmap, sample, sample, false)
    val pixels = IntArray(sample * sample)
    scaled.getPixels(pixels, 0, sample, 0, 0, sample, sample)
    if (scaled !== bitmap) scaled.recycle()

    var allR = 0L
    var allG = 0L
    var allB = 0L
    var allN = 0
    var vividR = 0L
    var vividG = 0L
    var vividB = 0L
    var vividN = 0
    for (pixel in pixels) {
        if (android.graphics.Color.alpha(pixel) < 200) continue
        val r = android.graphics.Color.red(pixel)
        val g = android.graphics.Color.green(pixel)
        val b = android.graphics.Color.blue(pixel)
        allR += r
        allG += g
        allB += b
        allN++
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val saturation = if (max == 0) 0f else (max - min).toFloat() / max
        if (saturation > 0.28f && max > 70 && min < 230) {
            vividR += r
            vividG += g
            vividB += b
            vividN++
        }
    }
    if (allN == 0) return null
    return if (vividN > 0) {
        Color(
            android.graphics.Color.rgb(
                (vividR / vividN).toInt(),
                (vividG / vividN).toInt(),
                (vividB / vividN).toInt()
            )
        )
    } else {
        Color(
            android.graphics.Color.rgb(
                (allR / allN).toInt(),
                (allG / allN).toInt(),
                (allB / allN).toInt()
            )
        )
    }
}

@Composable
private fun coverBackgroundBrush(seed: Color?): Brush {
    val base = MaterialTheme.colorScheme.background
    if (seed == null) return Brush.verticalGradient(listOf(base, base))
    return Brush.verticalGradient(
        listOf(
            lerp(base, seed, 0.55f),
            lerp(base, seed, 0.14f),
            base
        )
    )
}

private fun repeatIcon(mode: RepeatMode): Int = when (mode) {
    RepeatMode.OFF -> R.drawable.ic_repeat_off
    RepeatMode.ALL -> R.drawable.ic_repeat
    RepeatMode.ONE -> R.drawable.ic_repeat_one
}

private fun repeatDescription(mode: RepeatMode): Int = when (mode) {
    RepeatMode.OFF -> R.string.offline_desc_repeat_off
    RepeatMode.ALL -> R.string.offline_desc_repeat_all
    RepeatMode.ONE -> R.string.offline_desc_repeat_one
}

@Composable
private fun NowPlayingBar(
    song: OfflineSong,
    playing: Boolean,
    positionMs: Int,
    durationMs: Int,
    scrubMs: Int,
    onScrub: (Int) -> Unit,
    onScrubFinished: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    shuffleOn: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: RepeatMode,
    onCycleRepeat: () -> Unit
) {
    val scrubbing = scrubMs >= 0
    val shownPosition = if (scrubbing) scrubMs else positionMs
    val density = LocalDensity.current
    val expandThreshold = with(density) { 56.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .graphicsLayer { translationY = dragOffset }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -expandThreshold) onExpand()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { _, amount ->
                        dragOffset = (dragOffset + amount).coerceIn(-size.height * 0.6f, 0f)
                    }
                )
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongCover(song = song, size = 40.dp, corner = 8.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist.ifBlank { stringResource(R.string.offline_unknown_artist) },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                CompactIconButton(
                    painter = painterResource(if (shuffleOn) R.drawable.ic_shuffle_active else R.drawable.ic_shuffle),
                    contentDescription = stringResource(if (shuffleOn) R.string.offline_desc_shuffle_disable else R.string.offline_desc_shuffle_enable),
                    tint = if (shuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onToggleShuffle,
                    boxSize = 30.dp,
                    iconSize = 16.dp
                )
                CompactIconButton(
                    icon = TablerIcons.PlayerSkipBack,
                    contentDescription = stringResource(R.string.offline_desc_previous),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onPrev,
                    iconSize = 17.dp
                )
                CompactIconButton(
                    icon = if (playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                    contentDescription = if (playing) stringResource(R.string.offline_desc_pause) else stringResource(R.string.offline_desc_play),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onTogglePlay,
                    boxSize = 36.dp,
                    iconSize = 22.dp
                )
                CompactIconButton(
                    icon = TablerIcons.PlayerSkipForward,
                    contentDescription = stringResource(R.string.offline_desc_next),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onNext,
                    iconSize = 17.dp
                )
                CompactIconButton(
                    painter = painterResource(repeatIcon(repeatMode)),
                    contentDescription = stringResource(repeatDescription(repeatMode)),
                    tint = if (repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    onClick = onCycleRepeat,
                    boxSize = 30.dp,
                    iconSize = 16.dp
                )
                CompactIconButton(
                    icon = TablerIcons.X,
                    contentDescription = stringResource(R.string.offline_desc_close_player),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onClose,
                    iconSize = 16.dp
                )
            }
            Spacer(Modifier.height(2.dp))
            SeekBar(
                positionMs = shownPosition,
                durationMs = durationMs,
                scrubbing = scrubbing,
                onScrub = onScrub,
                onScrubFinished = onScrubFinished,
                modifier = Modifier.padding(end = 4.dp)
            )
            Row(modifier = Modifier.padding(end = 4.dp)) {
                Text(
                    text = formatTime(shownPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FullScreenPlayer(
    song: OfflineSong,
    playing: Boolean,
    positionMs: Int,
    durationMs: Int,
    scrubMs: Int,
    onScrub: (Int) -> Unit,
    onScrubFinished: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onCollapse: () -> Unit,
    onClose: () -> Unit,
    shuffleOn: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: RepeatMode,
    onCycleRepeat: () -> Unit,
    onEnterPip: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    }
    var volumeFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(maxVolume) {
        while (true) {
            val current = runCatching {
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            }.getOrDefault(0)
            volumeFraction = current.toFloat() / maxVolume
            delay(400.milliseconds)
        }
    }

    var coverSeed by remember(song.id, song.uri) { mutableStateOf<Color?>(null) }
    LaunchedEffect(song.id, song.uri) {
        val bitmap = withContext(Dispatchers.IO) { decodeCover(context, song) }
        coverSeed = bitmap?.let { extractSeedColor(it) }
    }
    val coverScheme = coverSeed?.let { remember(it) { schemeFromSeed(it) } }

    val collapseThreshold = with(density) { 84.dp.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val scrubbing = scrubMs >= 0
    val shownPosition = if (scrubbing) scrubMs else positionMs

    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 6.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactIconButton(
                    icon = TablerIcons.ChevronDown,
                    contentDescription = stringResource(R.string.offline_desc_collapse_player),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onCollapse,
                    boxSize = 40.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.offline_now_playing),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                CompactIconButton(
                    icon = TablerIcons.PictureInPicture,
                    contentDescription = stringResource(R.string.offline_desc_enter_pip),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onEnterPip,
                    boxSize = 40.dp,
                    iconSize = 18.dp
                )
                CompactIconButton(
                    icon = TablerIcons.X,
                    contentDescription = stringResource(R.string.offline_desc_close_player),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onClose,
                    boxSize = 40.dp,
                    iconSize = 18.dp
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val side = if (maxWidth < maxHeight) maxWidth else maxHeight
                CoverBox(
                    song = song,
                    corner = 16.dp,
                    iconFraction = 0.4f,
                    modifier = Modifier
                        .size(side)
                        .shadow(10.dp, RoundedCornerShape(16.dp))
                )
            }

            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = song.artist.ifBlank { stringResource(R.string.offline_unknown_artist) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            SeekBar(
                positionMs = shownPosition,
                durationMs = durationMs,
                scrubbing = scrubbing,
                onScrub = onScrub,
                onScrubFinished = onScrubFinished
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatTime(shownPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactIconButton(
                    painter = painterResource(if (shuffleOn) R.drawable.ic_shuffle_active else R.drawable.ic_shuffle),
                    contentDescription = stringResource(if (shuffleOn) R.string.offline_desc_shuffle_disable else R.string.offline_desc_shuffle_enable),
                    tint = if (shuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onToggleShuffle,
                    boxSize = 44.dp,
                    iconSize = 20.dp
                )
                CompactIconButton(
                    icon = TablerIcons.PlayerSkipBack,
                    contentDescription = stringResource(R.string.offline_desc_previous),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onPrev,
                    boxSize = 52.dp,
                    iconSize = 30.dp
                )
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay,
                        contentDescription = stringResource(if (playing) R.string.offline_desc_pause else R.string.offline_desc_play),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                CompactIconButton(
                    icon = TablerIcons.PlayerSkipForward,
                    contentDescription = stringResource(R.string.offline_desc_next),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onNext,
                    boxSize = 52.dp,
                    iconSize = 30.dp
                )
                CompactIconButton(
                    painter = painterResource(repeatIcon(repeatMode)),
                    contentDescription = stringResource(repeatDescription(repeatMode)),
                    tint = if (repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    onClick = onCycleRepeat,
                    boxSize = 44.dp,
                    iconSize = 20.dp
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = volumeIcon(volumeFraction),
                    contentDescription = stringResource(R.string.offline_desc_volume),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                VolumeBar(
                    fraction = volumeFraction,
                    onScrub = { fraction ->
                        val level = (fraction * maxVolume).roundToInt().coerceIn(0, maxVolume)
                        runCatching {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
                        }
                        volumeFraction = level.toFloat() / maxVolume
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationY = dragOffset }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset > collapseThreshold) onCollapse()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { _, amount ->
                        dragOffset = (dragOffset + amount).coerceIn(0f, size.height * 0.7f)
                    }
                )
            }
            .background(coverBackgroundBrush(coverSeed))
    ) {
        val scheme = coverScheme
        if (scheme != null) {
            MaterialTheme(colorScheme = scheme) { body() }
        } else {
            body()
        }
    }
}

@Composable
private fun PipContent(song: OfflineSong?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (song != null) {
            CoverBox(
                song = song,
                corner = 0.dp,
                iconFraction = 0.4f,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = TablerIcons.Music,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

private fun volumeIcon(fraction: Float): ImageVector = when {
    fraction < 0.4f -> TablerIcons.Volume
    fraction < 0.75f -> TablerIcons.Volume2
    else -> TablerIcons.Volume3
}

private fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}

private fun formatSeconds(sec: Int): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
