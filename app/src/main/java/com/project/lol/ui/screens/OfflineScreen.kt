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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat
import com.project.lol.R
import com.project.lol.offline.DownloadFolder
import com.project.lol.offline.IntegrityIssue
import com.project.lol.offline.IntegrityReport
import com.project.lol.offline.LibraryIntegrity
import com.project.lol.offline.LocalImportStore
import com.project.lol.offline.OfflinePlaylist
import com.project.lol.offline.OfflinePlaylistStore
import com.project.lol.offline.OfflineSong
import com.project.lol.offline.OfflineStore
import com.project.lol.offline.SongIntegrity
import com.project.lol.service.OfflineMediaService
import com.project.lol.timer.AppQuit
import com.project.lol.timer.SleepTimerAction
import com.project.lol.timer.SleepTimerManager
import com.project.lol.ui.components.SettingsDialog
import com.project.lol.ui.components.SleepTimerDialog
import com.project.lol.ui.theme.schemeFromSeed
import com.project.lol.offline.lyrics.Lrc
import com.project.lol.offline.lyrics.LyricsStore
import com.project.lol.offline.lyrics.ParsedLrc
import com.project.lol.util.BuildInfo
import com.project.lol.webview.helpers.LyricsStyle
import com.project.lol.webview.helpers.LyricsTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.CloudOff
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Filter
import compose.icons.tablericons.Folder
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Menu2
import compose.icons.tablericons.Microphone
import compose.icons.tablericons.Music
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.PictureInPicture
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerSkipBack
import compose.icons.tablericons.PlayerSkipForward
import compose.icons.tablericons.Playlist
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search
import compose.icons.tablericons.Settings
import compose.icons.tablericons.ShieldCheck
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
private enum class OfflineLibraryTab { SONGS, PLAYLISTS }
private enum class OfflineFilterKind { ARTIST, ALBUM, YEAR, FORMAT }

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
    themeSecondary: Color?,
    onThemeSecondaryChange: (Color?) -> Unit,
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
    var libraryTab by remember { mutableStateOf(OfflineLibraryTab.SONGS) }
    var playlists by remember { mutableStateOf<List<OfflinePlaylist>>(emptyList()) }
    var openPlaylist by remember { mutableStateOf<OfflinePlaylist?>(null) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var renamingPlaylist by remember { mutableStateOf<OfflinePlaylist?>(null) }
    var deletingPlaylist by remember { mutableStateOf<OfflinePlaylist?>(null) }
    var addToPlaylistSong by remember { mutableStateOf<OfflineSong?>(null) }
    var newPlaylistName by remember { mutableStateOf("") }
    var importing by remember { mutableStateOf(false) }
    var importLabel by remember { mutableStateOf("") }
    var queueSongs by remember { mutableStateOf<List<OfflineSong>>(emptyList()) }
    var artistFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var albumFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var yearFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var formatFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var headerMenuOpen by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var integrityReport by remember { mutableStateOf<IntegrityReport?>(null) }
    var showIntegrityDialog by remember { mutableStateOf(false) }

    val searchEngine = remember { GenericSearchEngine<OfflineSong>(maxResult = 100) }
    val songExtractor = remember {
        SearchableFieldExtractor<OfflineSong> { song ->
            arrayOf(song.title, song.artist, song.album, song.ytAlbum, song.ytArtist)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        importing = true
        importLabel = ""
        scope.launch {
            val summary = withContext(Dispatchers.IO) {
                LocalImportStore.importFiles(context, uris) { done, total, name ->
                    importLabel = if (name.isBlank()) "" else "$done/$total · $name"
                }
            }
            songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
            importing = false
            importLabel = ""
            val message = buildString {
                if (summary.imported > 0) {
                    if (summary.imported == 1) append(resources.getString(R.string.offline_import_done_one))
                    else append(resources.getString(R.string.offline_import_done_many, summary.imported))
                }
                if (summary.duplicates > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(resources.getString(R.string.offline_import_skipped, summary.duplicates))
                }
                if (summary.failed > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(resources.getString(R.string.offline_import_failed, summary.failed))
                }
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun reloadPlaylists() {
        scope.launch {
            val fresh = withContext(Dispatchers.IO) { OfflinePlaylistStore.load(context) }
            playlists = fresh
            val currentId = openPlaylist?.id
            openPlaylist = currentId?.let { id -> fresh.firstOrNull { it.id == id } }
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
    LaunchedEffect(songs) {
        if (songs.isEmpty()) {
            integrityReport = null
            return@LaunchedEffect
        }
        verifying = true
        integrityReport = LibraryIntegrity.scan(context, songs)
        verifying = false
    }

    val artistOptions = remember(songs) {
        songs.map { it.artist.ifBlank { it.ytArtist } }.distinct()
            .sortedWith(compareBy({ it.isBlank() }, { it.lowercase() }))
    }
    val albumOptions = remember(songs) {
        songs.map { it.album.ifBlank { it.ytAlbum } }.distinct()
            .sortedWith(compareBy({ it.isBlank() }, { it.lowercase() }))
    }
    val yearOptions = remember(songs) {
        val years = songs.mapNotNull { it.year }.distinct().sortedDescending().map { it.toString() }
        if (songs.any { it.year == null }) years + "" else years
    }
    val formatOptions = remember(songs) {
        songs.map { it.format.lowercase() }.distinct()
            .sortedWith(compareBy({ it.isBlank() }, { it }))
    }
    val filtersActive = artistFilters.isNotEmpty() || albumFilters.isNotEmpty() ||
        yearFilters.isNotEmpty() || formatFilters.isNotEmpty()
    val visibleSongs = remember(songs, searchResults, artistFilters, albumFilters, yearFilters, formatFilters) {
        (searchResults ?: songs).filter { song ->
            (artistFilters.isEmpty() || song.artist.ifBlank { song.ytArtist } in artistFilters) &&
                (albumFilters.isEmpty() || song.album.ifBlank { song.ytAlbum } in albumFilters) &&
                (yearFilters.isEmpty() || (song.year?.toString() ?: "") in yearFilters) &&
                (formatFilters.isEmpty() || song.format.lowercase() in formatFilters)
        }
    }
    val openPl = openPlaylist
    val playlistSongs = remember(openPl, songs) {
        openPl?.let { pl -> pl.songIds.mapNotNull { id -> songs.firstOrNull { it.id == id } } }
            ?: emptyList()
    }
    val songIds = remember(songs) { songs.map { it.id }.toHashSet() }

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

    fun playQueueIndex(index: Int) {
        val list = queueSongs
        if (list.isEmpty()) return
        audioFocus.acquire()
        playAt(mediaPlayer, context, list, index, { currentIndex = it }, { playerSong = it }, { isPlaying = it }, { durationMs = it }, { positionMs = it })
        if (shuffleOn) {
            val key = list.getOrNull(index)?.let { songKey(it) }
            val pos = shuffleOrder.indexOf(key)
            if (pos >= 0) shufflePos = pos
        }
        syncService()
    }

    fun playFrom(list: List<OfflineSong>, index: Int) {
        queueSongs = list
        playQueueIndex(index)
    }

    fun play(index: Int) = playFrom(songs, index)

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
        val keys = queueSongs.map { songKey(it) }
        if (shuffleOrder.size == keys.size && shuffleOrder.all { it in keys }) return
        val currentKey = queueSongs.getOrNull(currentIndex)?.let { songKey(it) }
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
        val list = queueSongs
        if (list.isEmpty()) return
        if (shuffleOn) {
            ensureShuffleOrder()
            val n = shuffleOrder.size
            if (n > 0) {
                val pos = if (shufflePos < 0) 0 else ((shufflePos + delta) % n + n) % n
                val index = list.indexOfFirst { songKey(it) == shuffleOrder[pos] }
                if (index >= 0) {
                    shufflePos = pos
                    playQueueIndex(index)
                    return
                }
            }
        }
        val next = ((currentIndex + delta) % list.size + list.size) % list.size
        playQueueIndex(next)
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
        queueSongs = emptyList()
        shuffleOrder = emptyList()
        shufflePos = -1
        runCatching { context.stopService(Intent(context, OfflineMediaService::class.java)) }
    }

    fun performDelete(song: OfflineSong) {
        if (songs.none { it.id == song.id && it.uri == song.uri }) return
        val queueIndex = queueSongs.indexOfFirst { it.id == song.id && it.uri == song.uri }
        if (queueIndex >= 0 && queueIndex == currentIndex) {
            stopAndClear()
        } else if (queueIndex >= 0) {
            queueSongs = queueSongs.filterNot { it.id == song.id && it.uri == song.uri }
            if (queueIndex < currentIndex) currentIndex -= 1
            shuffleOrder = emptyList()
            shufflePos = -1
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                if (LocalImportStore.isImported(song)) LocalImportStore.deleteSong(context, song.id)
                else OfflineStore.deleteSong(context, song)
            }
            songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
            if (!ok) {
                Toast.makeText(context, resources.getString(R.string.offline_toast_could_not_delete_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun runIntegrityCheck() {
        if (verifying) return
        verifying = true
        scope.launch {
            val result = LibraryIntegrity.scan(context, songs)
            verifying = false
            integrityReport = result
            showIntegrityDialog = true
        }
    }

    fun performDeleteAll() {
        val all = songs.filterNot { LocalImportStore.isImported(it) }
        if (all.isEmpty()) return
        stopAndClear()
        searchQuery = ""
        searchResults = null
        songs = songs.filter { LocalImportStore.isImported(it) }
        scope.launch {
            val failed = withContext(Dispatchers.IO) {
                all.count { song -> !OfflineStore.deleteSong(context, song) }
            }
            songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
            if (failed > 0) {
                Toast.makeText(
                    context,
                    if (failed == 1) resources.getString(R.string.offline_toast_could_not_delete_one_file) else resources.getString(R.string.offline_toast_could_not_delete_files, failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    BackHandler(enabled = settingsDialogOpen || openPlaylist != null || playerExpanded || searchFocused || searchQuery.isNotBlank() || libraryTab == OfflineLibraryTab.PLAYLISTS) {
        when {
            settingsDialogOpen -> settingsDialogOpen = false
            openPlaylist != null -> openPlaylist = null
            playerExpanded -> playerExpanded = false
            searchFocused -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            searchQuery.isNotBlank() -> searchQuery = ""
            else -> libraryTab = OfflineLibraryTab.SONGS
        }
    }

    DisposableEffect(Unit) {
        val ctrl = object : OfflineMediaService.OfflineController {
            override fun onPlayFromSearch(query: String?) {
                if (query.isNullOrBlank()) {
                    if (!isPlaying) {
                        if (currentIndex >= 0) togglePlayPause() else if (songs.isNotEmpty()) playFrom(songs, 0)
                    }
                } else {
                    val match = searchEngine.filter(songs, query.take(1024), songExtractor).firstOrNull()
                    val index = songs.indexOf(match)
                    if (index >= 0) playFrom(songs, index)
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
            if (repeatMode == RepeatMode.ONE && currentIndex in queueSongs.indices) {
                playQueueIndex(currentIndex)
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
                if (index in 0 until queueSongs.lastIndex) {
                    playQueueIndex(index + 1)
                    true
                } else if (repeatMode == RepeatMode.ALL && queueSongs.isNotEmpty()) {
                    playQueueIndex(0)
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
        playlists = withContext(Dispatchers.IO) { OfflinePlaylistStore.load(context) }
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
        themeSecondary = themeSecondary,
        onThemeSecondaryChange = onThemeSecondaryChange,
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
                        if (openPl != null) {
                            PlaylistDetailHeader(
                                playlist = openPl,
                                songCount = playlistSongs.size,
                                onBack = { openPlaylist = null },
                                onPlay = {
                                    if (playlistSongs.isNotEmpty()) playFrom(playlistSongs, 0)
                                },
                                onShuffle = {
                                    if (playlistSongs.isNotEmpty()) {
                                        if (!shuffleOn) shuffleOn = true
                                        playFrom(playlistSongs, 0)
                                    }
                                }
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SingleChoiceSegmentedButtonRow {
                                    SegmentedButton(
                                        selected = libraryTab == OfflineLibraryTab.SONGS,
                                        onClick = { libraryTab = OfflineLibraryTab.SONGS },
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                        label = {
                                            Text(
                                                text = stringResource(R.string.offline_tab_songs),
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                    SegmentedButton(
                                        selected = libraryTab == OfflineLibraryTab.PLAYLISTS,
                                        onClick = { libraryTab = OfflineLibraryTab.PLAYLISTS },
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                        label = {
                                            Text(
                                                text = stringResource(R.string.offline_tab_playlists),
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                if (importing || verifying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                if (libraryTab == OfflineLibraryTab.SONGS && songs.isNotEmpty()) {
                                    OfflineFiltersButton(
                                        count = artistFilters.size + albumFilters.size + yearFilters.size + formatFilters.size,
                                        onClick = { showFilterSheet = true }
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Box {
                                    CompactIconButton(
                                        icon = TablerIcons.DotsVertical,
                                        contentDescription = stringResource(R.string.offline_desc_more),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        onClick = { headerMenuOpen = true },
                                        boxSize = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    DropdownMenu(
                                        expanded = headerMenuOpen,
                                        onDismissRequest = { headerMenuOpen = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.offline_desc_verify)) },
                                            leadingIcon = { Icon(TablerIcons.ShieldCheck, null) },
                                            onClick = {
                                                headerMenuOpen = false
                                                runIntegrityCheck()
                                            }
                                        )
                                        if (songs.any { !LocalImportStore.isImported(it) }) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.offline_desc_delete_all_downloads),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                },
                                                leadingIcon = { Icon(TablerIcons.Trash, null, tint = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    headerMenuOpen = false
                                                    confirmDeleteAll = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = when {
                                    loading -> stringResource(R.string.offline_status_loading)
                                    libraryTab == OfflineLibraryTab.PLAYLISTS -> when {
                                        playlists.isEmpty() -> stringResource(R.string.offline_playlists_status_none)
                                        playlists.size == 1 -> stringResource(R.string.offline_playlists_status_one)
                                        else -> stringResource(R.string.offline_playlists_status_many, playlists.size)
                                    }
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
                                    filtersActive -> stringResource(R.string.offline_status_filtered, visibleSongs.size, songs.size)
                                    songs.isEmpty() -> stringResource(R.string.offline_status_no_downloads_yet)
                                    songs.size == 1 -> stringResource(R.string.offline_status_one_song_available_offline)
                                    else -> stringResource(R.string.offline_status_songs_available_offline, songs.size)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val issues = integrityReport
                            if (!loading && libraryTab == OfflineLibraryTab.SONGS && issues != null && issues.entries.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                IntegrityChip(
                                    damaged = issues.damaged.size,
                                    total = issues.entries.size,
                                    onClick = { showIntegrityDialog = true }
                                )
                            }
                            }
                            AnimatedContent(
                                targetState = libraryTab,
                                modifier = Modifier.fillMaxWidth(),
                                transitionSpec = {
                                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                },
                                label = "offlineTabHeader"
                            ) { targetTab ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                            if (!loading && targetTab == OfflineLibraryTab.SONGS && songs.isNotEmpty()) {
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
                                        icon = TablerIcons.Plus,
                                        contentDescription = stringResource(R.string.offline_desc_import),
                                        tint = if (importing) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.primary,
                                        onClick = { if (!importing) importLauncher.launch(arrayOf("audio/*")) },
                                        boxSize = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                if (importing && importLabel.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = importLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (!loading && targetTab == OfflineLibraryTab.PLAYLISTS) {
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    Spacer(Modifier.weight(1f))
                                    CompactIconButton(
                                        icon = TablerIcons.Plus,
                                        contentDescription = stringResource(R.string.offline_desc_new_playlist),
                                        tint = MaterialTheme.colorScheme.primary,
                                        onClick = {
                                            newPlaylistName = ""
                                            showNewPlaylistDialog = true
                                        },
                                        boxSize = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                            }
                            }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = libraryTab,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        transitionSpec = {
                            fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                        },
                        label = "offlineTab"
                    ) { targetTab ->
                    Column(modifier = Modifier.fillMaxSize()) {
                    when {
                        loading -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        openPl != null -> when {
                            playlistSongs.isEmpty() -> Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = TablerIcons.Playlist,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(R.string.offline_playlist_empty_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.offline_playlist_empty_hint),
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
                                items(playlistSongs, key = { "${it.id}-${it.uri}" }) { song ->
                                    val current = queueSongs.getOrNull(currentIndex)
                                    val isCurrent = current != null && current.id == song.id && current.uri == song.uri
                                    PlaylistSongRow(
                                        song = song,
                                        isCurrent = isCurrent,
                                        onClick = {
                                            val index = playlistSongs.indexOfFirst { it.id == song.id && it.uri == song.uri }
                                            if (isCurrent) {
                                                if (durationMs > 0 || mediaPlayer.isPlaying) togglePlayPause()
                                                else if (index >= 0) playFrom(playlistSongs, index)
                                            } else if (index >= 0) {
                                                playFrom(playlistSongs, index)
                                            }
                                        },
                                        onRemove = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) {
                                                    OfflinePlaylistStore.removeSong(context, openPl.id, song.id)
                                                }
                                                reloadPlaylists()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        targetTab == OfflineLibraryTab.PLAYLISTS -> when {
                            playlists.isEmpty() -> Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = TablerIcons.Playlist,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(R.string.offline_playlists_empty_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.offline_playlists_empty_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(18.dp))
                                    Surface(
                                        onClick = {
                                            newPlaylistName = ""
                                            showNewPlaylistDialog = true
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Plus,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.offline_playlists_create),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            else -> LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 112.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(playlists, key = { it.id }) { pl ->
                                    PlaylistRow(
                                        playlist = pl,
                                        songCount = pl.songIds.count { it in songIds },
                                        onClick = { openPlaylist = pl },
                                        onRename = {
                                            newPlaylistName = pl.name
                                            renamingPlaylist = pl
                                        },
                                        onDelete = { deletingPlaylist = pl }
                                    )
                                }
                            }
                        }
                        (searchQuery.isNotBlank() || filtersActive) && visibleSongs.isEmpty() -> Box(
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
                                    text = if (searchQuery.isNotBlank()) {
                                        stringResource(R.string.offline_empty_nothing_matches, searchQuery.trim())
                                    } else {
                                        stringResource(R.string.offline_empty_no_filter_match)
                                    },
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
                                Spacer(Modifier.height(18.dp))
                                Surface(
                                    onClick = { if (!importing) importLauncher.launch(arrayOf("audio/*")) },
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.Plus,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.offline_empty_import_cta),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                if (importing) {
                                    Spacer(Modifier.height(12.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
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
                                val current = queueSongs.getOrNull(currentIndex)
                                val isCurrent = current != null && current.id == song.id && current.uri == song.uri
                                OfflineSongRow(
                                    song = song,
                                    isCurrent = isCurrent,
                                    onClick = {
                                        if (isCurrent) {
                                            if (durationMs > 0 || mediaPlayer.isPlaying) {
                                                togglePlayPause()
                                            } else {
                                                playFrom(songs, index)
                                            }
                                        } else {
                                            playFrom(songs, index)
                                        }
                                    },
                                    onLocate = if (LocalImportStore.isImported(song)) null else {
                                        {
                                            if (!DownloadFolder.openTrack(context, song.uri)) {
                                                Toast.makeText(context, song.uri.lastPathSegment ?: song.title, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onAddToPlaylist = { addToPlaylistSong = song },
                                    onDelete = { pendingDelete = song }
                                )
                            }
                        }
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
                            onEnterPip = onEnterPip,
                            onOpenSleepTimer = { showSleepTimerDialog = true },
                            sleepTimerActive = sleepTimerState.active,
                            hideTopBar = hideTopBar
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
                            val unknownArtist = stringResource(R.string.offline_unknown_artist)
                            Text(
                                text = songToDelete.let { song ->
                                    if (LocalImportStore.isImported(song)) {
                                        stringResource(
                                            R.string.offline_dialog_delete_import_message,
                                            song.title,
                                            song.artist.ifBlank { unknownArtist }
                                        )
                                    } else {
                                        stringResource(
                                            R.string.offline_dialog_delete_song_message,
                                            song.title,
                                            song.artist.ifBlank { unknownArtist }
                                        )
                                    }
                                },
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
                    val total = songs.count { !LocalImportStore.isImported(it) }
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
                                text = stringResource(R.string.offline_dialog_delete_all_message, total),
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

                addToPlaylistSong?.let { addSong ->
                    if (!showNewPlaylistDialog && renamingPlaylist == null) {
                        AlertDialog(
                            onDismissRequest = { addToPlaylistSong = null },
                            shape = RoundedCornerShape(28.dp),
                            title = {
                                Text(
                                    text = stringResource(R.string.offline_dialog_add_to_playlist_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column {
                                    Text(
                                        text = addSong.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .heightIn(max = 320.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    newPlaylistName = ""
                                                    showNewPlaylistDialog = true
                                                }
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Plus,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                text = stringResource(R.string.offline_dialog_new_playlist_option),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        playlists.forEach { pl ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        scope.launch {
                                                            val added = withContext(Dispatchers.IO) {
                                                                OfflinePlaylistStore.addSong(context, pl.id, addSong.id)
                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                resources.getString(
                                                                    if (added) R.string.offline_toast_added_to_playlist
                                                                    else R.string.offline_toast_already_in_playlist,
                                                                    pl.name
                                                                ),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            addToPlaylistSong = null
                                                            reloadPlaylists()
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = TablerIcons.Playlist,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    text = pl.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { addToPlaylistSong = null }) {
                                    Text(stringResource(R.string.offline_dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    }
                }

                val renamingPl = renamingPlaylist
                if (showNewPlaylistDialog || renamingPl != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showNewPlaylistDialog = false
                            renamingPlaylist = null
                            newPlaylistName = ""
                        },
                        shape = RoundedCornerShape(28.dp),
                        title = {
                            Text(
                                text = stringResource(
                                    if (renamingPl != null) R.string.offline_dialog_rename_playlist_title
                                    else R.string.offline_dialog_new_playlist_title
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            OutlinedTextField(
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text(stringResource(R.string.offline_playlist_name_hint)) }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val name = newPlaylistName.trim()
                                    if (name.isBlank()) return@TextButton
                                    val songToAdd = addToPlaylistSong
                                    showNewPlaylistDialog = false
                                    renamingPlaylist = null
                                    newPlaylistName = ""
                                    scope.launch {
                                        if (renamingPl != null) {
                                            withContext(Dispatchers.IO) {
                                                OfflinePlaylistStore.rename(context, renamingPl.id, name)
                                            }
                                        } else {
                                            withContext(Dispatchers.IO) {
                                                OfflinePlaylistStore.create(context, name, firstSongId = songToAdd?.id)
                                            }
                                            if (songToAdd != null) {
                                                Toast.makeText(
                                                    context,
                                                    resources.getString(R.string.offline_toast_added_to_playlist, name),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        addToPlaylistSong = null
                                        reloadPlaylists()
                                    }
                                },
                                enabled = newPlaylistName.isNotBlank()
                            ) {
                                Text(
                                    text = stringResource(
                                        if (renamingPl != null) R.string.offline_dialog_rename_playlist_title
                                        else R.string.offline_playlists_create
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showNewPlaylistDialog = false
                                renamingPlaylist = null
                                newPlaylistName = ""
                            }) {
                                Text(stringResource(R.string.offline_dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                }

                val deletePl = deletingPlaylist
                if (deletePl != null) {
                    AlertDialog(
                        onDismissRequest = { deletingPlaylist = null },
                        shape = RoundedCornerShape(28.dp),
                        title = {
                            Text(
                                text = stringResource(R.string.offline_dialog_delete_playlist_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.offline_dialog_delete_playlist_message, deletePl.name),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                deletingPlaylist = null
                                if (openPlaylist?.id == deletePl.id) openPlaylist = null
                                scope.launch {
                                    withContext(Dispatchers.IO) { OfflinePlaylistStore.delete(context, deletePl.id) }
                                    reloadPlaylists()
                                }
                            }) {
                                Text(
                                    text = stringResource(R.string.offline_dialog_delete),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { deletingPlaylist = null }) {
                                Text(stringResource(R.string.offline_dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                }

    val report = integrityReport
    if (showIntegrityDialog && report != null) {
        IntegrityReportDialog(
            report = report,
            onDismiss = { showIntegrityDialog = false },
            onRemoveDamaged = {
                val damaged = report.damaged.map { it.song }
                showIntegrityDialog = false
                val current = queueSongs.getOrNull(currentIndex)
                if (current != null && damaged.any { it.id == current.id && it.uri == current.uri }) {
                    stopAndClear()
                }
                scope.launch {
                    withContext(Dispatchers.IO) {
                        damaged.forEach { song ->
                            if (LocalImportStore.isImported(song)) LocalImportStore.deleteSong(context, song.id)
                            else OfflineStore.deleteSong(context, song)
                        }
                    }
                    songs = withContext(Dispatchers.IO) { OfflineStore.loadSongs(context) }
                }
            }
        )
    }

    if (showFilterSheet) {
        val unknownArtist = stringResource(R.string.offline_unknown_artist)
        val unknownAlbum = stringResource(R.string.offline_unknown_album)
        val unknownValue = stringResource(R.string.offline_filter_unknown)
        OfflineFilterSheet(
            artistOptions = artistOptions.map { it to it.ifBlank { unknownArtist } },
            albumOptions = albumOptions.map { it to it.ifBlank { unknownAlbum } },
            yearOptions = yearOptions.map { it to it.ifBlank { unknownValue } },
            formatOptions = formatOptions.map { it to it.ifBlank { unknownValue }.uppercase() },
            artistFilters = artistFilters,
            albumFilters = albumFilters,
            yearFilters = yearFilters,
            formatFilters = formatFilters,
            onToggle = { kind, value ->
                when (kind) {
                    OfflineFilterKind.ARTIST -> artistFilters = artistFilters.toggle(value)
                    OfflineFilterKind.ALBUM -> albumFilters = albumFilters.toggle(value)
                    OfflineFilterKind.YEAR -> yearFilters = yearFilters.toggle(value)
                    OfflineFilterKind.FORMAT -> formatFilters = formatFilters.toggle(value)
                }
            },
            onClearAll = {
                artistFilters = emptySet()
                albumFilters = emptySet()
                yearFilters = emptySet()
                formatFilters = emptySet()
            },
            onDismiss = { showFilterSheet = false }
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
private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value

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
    onLocate: (() -> Unit)?,
    onAddToPlaylist: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
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
            Text(
                text = songSubtitle(song),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            CompactIconButton(
                icon = TablerIcons.DotsVertical,
                contentDescription = stringResource(R.string.offline_desc_more),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { menuOpen = true },
                boxSize = 34.dp,
                iconSize = 18.dp
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                if (onLocate != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.offline_desc_locate_track)) },
                        leadingIcon = { Icon(TablerIcons.Folder, null) },
                        onClick = {
                            menuOpen = false
                            onLocate()
                        }
                    )
                }
                if (onAddToPlaylist != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.offline_desc_add_to_playlist)) },
                        leadingIcon = { Icon(TablerIcons.Plus, null) },
                        onClick = {
                            menuOpen = false
                            onAddToPlaylist()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.offline_desc_delete), color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(TablerIcons.Trash, null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun songSubtitle(song: OfflineSong): String {
    val unknownArtist = stringResource(R.string.offline_unknown_artist)
    val explicitLabel = stringResource(R.string.offline_label_explicit)
    return buildString {
        append(song.artist.ifBlank { unknownArtist })
        song.album.takeIf { it.isNotBlank() }?.let { append(" • $it") }
        song.durationSec?.takeIf { it > 0 }?.let { append(" • ${formatSeconds(it)}") }
        if (song.explicit) append(" • $explicitLabel")
    }
}

@Composable
private fun OfflineFiltersButton(count: Int, onClick: () -> Unit) {
    val active = count > 0
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = TablerIcons.Filter,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (active) "${stringResource(R.string.offline_filter_title)} · $count" else stringResource(R.string.offline_filter_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineFilterSheet(
    artistOptions: List<Pair<String, String>>,
    albumOptions: List<Pair<String, String>>,
    yearOptions: List<Pair<String, String>>,
    formatOptions: List<Pair<String, String>>,
    artistFilters: Set<String>,
    albumFilters: Set<String>,
    yearFilters: Set<String>,
    formatFilters: Set<String>,
    onToggle: (OfflineFilterKind, String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val anyActive = artistFilters.isNotEmpty() || albumFilters.isNotEmpty() ||
        yearFilters.isNotEmpty() || formatFilters.isNotEmpty()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.offline_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (anyActive) {
                    TextButton(onClick = onClearAll) {
                        Text(stringResource(R.string.offline_filter_clear))
                    }
                }
            }
            FilterGroup(
                title = stringResource(R.string.offline_filter_artist),
                options = artistOptions,
                selected = artistFilters
            ) { onToggle(OfflineFilterKind.ARTIST, it) }
            FilterGroup(
                title = stringResource(R.string.offline_filter_album),
                options = albumOptions,
                selected = albumFilters
            ) { onToggle(OfflineFilterKind.ALBUM, it) }
            FilterGroup(
                title = stringResource(R.string.offline_filter_year),
                options = yearOptions,
                selected = yearFilters
            ) { onToggle(OfflineFilterKind.YEAR, it) }
            FilterGroup(
                title = stringResource(R.string.offline_filter_format),
                options = formatOptions,
                selected = formatFilters
            ) { onToggle(OfflineFilterKind.FORMAT, it) }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.offline_filter_done))
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (options.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value in selected,
                onClick = { onToggle(value) },
                label = { Text(label, maxLines = 1) },
                leadingIcon = if (value in selected) {
                    { Icon(TablerIcons.Check, null, modifier = Modifier.size(16.dp)) }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun IntegrityChip(damaged: Int, total: Int, onClick: () -> Unit) {
    val error = damaged > 0
    val container = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val content = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = container) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (error) TablerIcons.AlertTriangle else TablerIcons.Check,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "$total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = content
            )
        }
    }
}

@Composable
private fun IntegrityReportDialog(
    report: IntegrityReport,
    onDismiss: () -> Unit,
    onRemoveDamaged: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = stringResource(R.string.offline_verify_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (report.clean) {
                    Text(
                        text = stringResource(R.string.offline_verify_summary_ok, report.checked),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.offline_verify_summary,
                            report.checked,
                            report.damaged.size,
                            report.incompleteTags.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (report.damaged.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        IntegritySection(
                            title = stringResource(R.string.offline_verify_section_damaged),
                            entries = report.damaged,
                            destructive = true
                        )
                    }
                    if (report.incompleteTags.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        IntegritySection(
                            title = stringResource(R.string.offline_verify_section_tags),
                            entries = report.incompleteTags,
                            destructive = false
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.offline_verify_close), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (report.damaged.isNotEmpty()) {
                TextButton(onClick = onRemoveDamaged) {
                    Text(
                        text = stringResource(R.string.offline_verify_remove_damaged),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun IntegritySection(
    title: String,
    entries: List<SongIntegrity>,
    destructive: Boolean,
) {
    val context = LocalContext.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(4.dp))
    entries.forEach { entry ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = entry.song.title.ifBlank { stringResource(R.string.offline_verify_untitled) },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.issues.joinToString(" · ") { integrityIssueLabel(context, it) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun integrityIssueLabel(context: Context, issue: IntegrityIssue): String = context.getString(
    when (issue) {
        IntegrityIssue.MISSING_FILE -> R.string.offline_verify_issue_missing_file
        IntegrityIssue.EMPTY_FILE -> R.string.offline_verify_issue_empty_file
        IntegrityIssue.UNREADABLE -> R.string.offline_verify_issue_unreadable
        IntegrityIssue.MISSING_TITLE -> R.string.offline_verify_issue_title
        IntegrityIssue.MISSING_ARTIST -> R.string.offline_verify_issue_artist
        IntegrityIssue.MISSING_ALBUM -> R.string.offline_verify_issue_album
        IntegrityIssue.MISSING_YEAR -> R.string.offline_verify_issue_year
        IntegrityIssue.MISSING_COVER -> R.string.offline_verify_issue_cover
    }
)

@Composable
private fun PlaylistRow(
    playlist: OfflinePlaylist,
    songCount: Int,
    onClick: () -> Unit,
    onRename: () -> Unit,
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TablerIcons.Playlist,
                contentDescription = stringResource(R.string.offline_desc_open_playlist),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (songCount == 1) stringResource(R.string.offline_playlist_song_count_one)
                else stringResource(R.string.offline_playlist_song_count_many, songCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CompactIconButton(
            icon = TablerIcons.Pencil,
            contentDescription = stringResource(R.string.offline_desc_rename_playlist),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onRename,
            boxSize = 30.dp,
            iconSize = 17.dp
        )
        CompactIconButton(
            icon = TablerIcons.Trash,
            contentDescription = stringResource(R.string.offline_desc_delete_playlist),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            onClick = onDelete,
            boxSize = 30.dp,
            iconSize = 17.dp
        )
    }
}

@Composable
private fun PlaylistDetailHeader(
    playlist: OfflinePlaylist,
    songCount: Int,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CompactIconButton(
            icon = TablerIcons.ChevronLeft,
            contentDescription = stringResource(R.string.offline_desc_back_to_playlists),
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onBack,
            boxSize = 36.dp,
            iconSize = 24.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (songCount == 1) stringResource(R.string.offline_playlist_song_count_one)
                else stringResource(R.string.offline_playlist_song_count_many, songCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CompactIconButton(
            icon = TablerIcons.PlayerPlay,
            contentDescription = stringResource(R.string.offline_desc_play_playlist),
            tint = MaterialTheme.colorScheme.primary,
            onClick = onPlay,
            boxSize = 36.dp,
            iconSize = 20.dp
        )
        CompactIconButton(
            painter = painterResource(R.drawable.ic_shuffle),
            contentDescription = stringResource(R.string.offline_desc_shuffle_playlist),
            tint = MaterialTheme.colorScheme.primary,
            onClick = onShuffle,
            boxSize = 36.dp,
            iconSize = 18.dp
        )
    }
}

@Composable
private fun PlaylistSongRow(
    song: OfflineSong,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
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
            Text(
                text = songSubtitle(song),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CompactIconButton(
            icon = TablerIcons.X,
            contentDescription = stringResource(R.string.offline_desc_remove_from_playlist),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onRemove,
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
    onEnterPip: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    sleepTimerActive: Boolean,
    hideTopBar: Boolean
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

    var showLyrics by remember { mutableStateOf(false) }
    var lyrics by remember(song.id) { mutableStateOf<ParsedLrc?>(null) }
    var lyricsLoaded by remember(song.id) { mutableStateOf(false) }
    var activeLyricLine by remember(song.id) { mutableStateOf(-1) }
    val lyricsListState = rememberLazyListState()
    LaunchedEffect(song.id, showLyrics) {
        if (!showLyrics || lyricsLoaded) return@LaunchedEffect
        lyrics = withContext(Dispatchers.IO) { LyricsStore.parsed(context, song.id) }
        lyricsLoaded = true
    }
    LaunchedEffect(lyrics, positionMs, showLyrics) {
        val parsed = lyrics ?: return@LaunchedEffect
        if (!parsed.synced) return@LaunchedEffect
        val index = Lrc.activeIndex(parsed, positionMs.toLong())
        if (index == activeLyricLine) return@LaunchedEffect
        activeLyricLine = index
        if (showLyrics && index >= 0) {
            val delta = kotlin.math.abs(index - lyricsListState.firstVisibleItemIndex)
            if (delta > 8) lyricsListState.scrollToItem(index) else lyricsListState.animateScrollToItem(index)
        }
    }

    val topBarOffset = if (hideTopBar) with(LocalConfiguration.current) { (screenHeightDp * 0.05f).dp } else 0.dp
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topBarOffset),
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
                val parsedLyrics = lyrics
                if (showLyrics && parsedLyrics != null && parsedLyrics.lines.isNotEmpty()) {
                    OfflineLyricsView(
                        parsed = parsedLyrics,
                        activeIndex = activeLyricLine,
                        listState = lyricsListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                Spacer(Modifier.weight(1f))
                CompactIconButton(
                    icon = TablerIcons.Microphone,
                    contentDescription = stringResource(R.string.offline_desc_lyrics),
                    tint = if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { showLyrics = !showLyrics },
                    boxSize = 44.dp,
                    iconSize = 20.dp
                )
                CompactIconButton(
                    icon = TablerIcons.PictureInPicture,
                    contentDescription = stringResource(R.string.offline_desc_enter_pip),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onEnterPip,
                    boxSize = 44.dp,
                    iconSize = 20.dp
                )
                CompactIconButton(
                    painter = painterResource(R.drawable.ic_timer),
                    contentDescription = stringResource(R.string.timer_desc_open),
                    tint = if (sleepTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenSleepTimer,
                    boxSize = 44.dp,
                    iconSize = 20.dp
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

/**
 * Native lyrics viewer for Offline Mode. Deliberately mirrors the WebView engine: same preset
 * curve through [LyricsTheme.previewStyle] and the same scale / spacing / alignment / animation
 * overrides from [LyricsStyle], so the two renderings look identical.
 */
@Composable
private fun OfflineLyricsView(
    parsed: ParsedLrc,
    activeIndex: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (parsed.lines.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.offline_lyrics_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val context = LocalContext.current
    val style = remember {
        val prefs = context.getSharedPreferences("spotilol_prefs", Context.MODE_PRIVATE)
        LyricsTheme.sanitize(LyricsStyle.from(prefs))
    }
    val preview = remember(style) { LyricsTheme.previewStyle(style) }

    Column(modifier = modifier) {
        if (!parsed.synced) {
            Text(
                text = stringResource(R.string.offline_lyrics_unsynced),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(parsed.lines.size) { index ->
                val line = parsed.lines[index]
                val isActive = parsed.synced && index == activeIndex
                val isPast = parsed.synced && activeIndex >= 0 && index < activeIndex
                val stateAlpha = when {
                    !parsed.synced -> 1f
                    isActive -> 1f
                    isPast -> preview.passedOpacity
                    else -> preview.upcomingOpacity
                }
                val stateScale = if (isActive) preview.activeScale else 1f
                val animAlpha = if (style.animate) {
                    animateFloatAsState(stateAlpha, tween(350), label = "lyrAlpha").value
                } else stateAlpha
                val animScale = if (style.animate) {
                    animateFloatAsState(stateScale, tween(350), label = "lyrScale").value
                } else stateScale
                Text(
                    text = line.text,
                    fontSize = preview.baseSp.sp,
                    lineHeight = (preview.baseSp * preview.lineHeight).sp,
                    fontWeight = FontWeight(if (isActive) preview.activeWeight else preview.weight),
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = when (style.align) {
                        LyricsTheme.ALIGN_CENTER -> TextAlign.Center
                        LyricsTheme.ALIGN_END -> TextAlign.End
                        else -> TextAlign.Start
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = animAlpha
                            scaleX = animScale
                            scaleY = animScale
                            transformOrigin = when (style.align) {
                                LyricsTheme.ALIGN_CENTER -> TransformOrigin(0.5f, 0.5f)
                                LyricsTheme.ALIGN_END -> TransformOrigin(1f, 0.5f)
                                else -> TransformOrigin(0f, 0.5f)
                            }
                        }
                        .padding(vertical = 5.dp)
                )
            }
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
