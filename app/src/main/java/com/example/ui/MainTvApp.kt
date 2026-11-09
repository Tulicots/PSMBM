package com.example.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.components.TvSimulatorContainer
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.model.PlayerTheme
import com.example.player.AudioPlayerEngine
import com.example.player.SampleMusicProvider
import com.example.ui.components.TvFocusableCard
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SambaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemesScreen

enum class TvDestination(val title: String, val icon: ImageVector) {
    NOW_PLAYING("Reproduciendo", Icons.Default.PlayCircle),
    LIBRARY("Biblioteca", Icons.Default.LibraryMusic),
    THEMES("Estilo & Tema", Icons.Default.Palette),
    SETTINGS("Ajustes", Icons.Default.Settings),
    SAMBA("Samba SMB", Icons.Default.Router)
}

@Composable
fun MainTvApp(
    playerEngine: AudioPlayerEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(TvDestination.NOW_PLAYING) }
    var currentTheme by remember { mutableStateOf(PlayerTheme.OLED_NEON) }
    var isSimulatedTvMode by remember { mutableStateOf(false) }

    // Playlist tracks
    var allTracks by remember {
        mutableStateOf(SampleMusicProvider.getSampleTracks(context))
    }

    // Auto-scan local music on launch every time the app opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val localTracks = com.example.player.LocalMusicScanner.scanAll(context)
        allTracks = (localTracks + allTracks).distinctBy { it.id }
    }

    val playbackState by playerEngine.playbackState.collectAsState()

    // Back button behavior
    BackHandler(enabled = currentTab != TvDestination.NOW_PLAYING || isSimulatedTvMode) {
        if (isSimulatedTvMode) {
            isSimulatedTvMode = false
        } else {
            currentTab = TvDestination.NOW_PLAYING
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundDark)
            .onKeyEvent { keyEvent ->
                // Global TV remote media key handling
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            playerEngine.play()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            playerEngine.pause()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            playerEngine.togglePlayPause()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            playerEngine.playNext()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            playerEngine.playPrevious()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            playerEngine.seekForward()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            playerEngine.seekRewind()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val isTvDevice = remember {
                val uiModeManager = context.getSystemService(android.content.Context.UI_MODE_SERVICE) as? android.app.UiModeManager
                uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION ||
                    context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)
            }
            // TV widescreen applies ONLY on a real TV hardware or in true landscape orientation on large screen (>= 760dp)
            val isRealTvWidescreen = isTvDevice || (isLandscape && maxWidth >= 760.dp)

            if (isRealTvWidescreen) {
                // Real Android TV / Landscape Tablet Mode
                Row(modifier = Modifier.fillMaxSize()) {
                    TvNavRail(
                        currentDestination = currentTab,
                        onDestinationSelected = { currentTab = it },
                        theme = currentTheme,
                        modifier = Modifier
                            .width(88.dp)
                            .fillMaxHeight()
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TvScreensDispatcher(
                                currentTab = currentTab,
                                playbackState = playbackState,
                                allTracks = allTracks,
                                currentTheme = currentTheme,
                                playerEngine = playerEngine,
                                context = context,
                                onUpdateTracks = { allTracks = it },
                                onSelectTheme = { currentTheme = it },
                                onNavigateToDestination = { currentTab = it }
                            )
                        }

                        if (currentTab != TvDestination.NOW_PLAYING && playbackState.currentTrack != null) {
                            TvMiniPlayer(
                                track = playbackState.currentTrack,
                                isPlaying = playbackState.isPlaying,
                                currentPositionMs = playbackState.currentPositionMs,
                                durationMs = playbackState.durationMs,
                                theme = currentTheme,
                                onTogglePlayPause = { playerEngine.togglePlayPause() },
                                onNext = { playerEngine.playNext() },
                                onPrev = { playerEngine.playPrevious() },
                                onClick = { currentTab = TvDestination.NOW_PLAYING }
                            )
                        }
                    }
                }
            } else {
                // Mobile / Phone View (Portrait)
                if (isSimulatedTvMode) {
                    // Interactive Android TV 16:9 Simulator with on-screen D-Pad Remote!
                    TvSimulatorContainer(
                        theme = currentTheme,
                        isPlaying = playbackState.isPlaying,
                        currentTab = currentTab,
                        playbackState = playbackState,
                        allTracks = allTracks,
                        onSelectTab = { currentTab = it },
                        onTogglePlayPause = { playerEngine.togglePlayPause() },
                        onNext = { playerEngine.playNext() },
                        onPrev = { playerEngine.playPrevious() },
                        onSeekForward = { playerEngine.seekForward() },
                        onSeekRewind = { playerEngine.seekRewind() },
                        onBack = { currentTab = TvDestination.NOW_PLAYING },
                        onHome = { currentTab = TvDestination.NOW_PLAYING },
                        onDpadDirection = { direction ->
                            when (direction) {
                                "UP" -> {
                                    val nextIndex = (currentTab.ordinal - 1).coerceAtLeast(0)
                                    currentTab = TvDestination.entries[nextIndex]
                                }
                                "DOWN" -> {
                                    val nextIndex = (currentTab.ordinal + 1).coerceAtMost(TvDestination.entries.lastIndex)
                                    currentTab = TvDestination.entries[nextIndex]
                                }
                                "SELECT" -> playerEngine.togglePlayPause()
                                "LEFT" -> playerEngine.playPrevious()
                                "RIGHT" -> playerEngine.playNext()
                            }
                        },
                        onCloseSimulator = { isSimulatedTvMode = false }
                    )
                } else {
                    // Pristine Native Mobile Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        // Top Mobile Bar with TV Simulator Mode Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(currentTheme.primaryAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = currentTheme.primaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Sonora Music TV",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val currentTrack = playbackState.currentTrack
                                    if (currentTrack != null) {
                                        Text(
                                            text = "● ${currentTrack.format.extension.uppercase()} Hi-Res • ${currentTrack.qualityDetails.substringBefore("•")}",
                                            color = currentTheme.primaryAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Audio Hi-Fi • Android TV",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // TV Simulator Toggle Button
                            Surface(
                                onClick = { isSimulatedTvMode = true },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.primaryAccent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = currentTheme.primaryAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Simulador TV 16:9",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Screen Content Area
                        Box(modifier = Modifier.weight(1f)) {
                            TvScreensDispatcher(
                                currentTab = currentTab,
                                playbackState = playbackState,
                                allTracks = allTracks,
                                currentTheme = currentTheme,
                                playerEngine = playerEngine,
                                context = context,
                                onUpdateTracks = { allTracks = it },
                                onSelectTheme = { currentTheme = it },
                                onNavigateToDestination = { currentTab = it }
                            )
                        }

                        // Mini-player bar when user is browsing other screens
                        if (currentTab != TvDestination.NOW_PLAYING && playbackState.currentTrack != null) {
                            TvMiniPlayer(
                                track = playbackState.currentTrack,
                                isPlaying = playbackState.isPlaying,
                                currentPositionMs = playbackState.currentPositionMs,
                                durationMs = playbackState.durationMs,
                                theme = currentTheme,
                                onTogglePlayPause = { playerEngine.togglePlayPause() },
                                onNext = { playerEngine.playNext() },
                                onPrev = { playerEngine.playPrevious() },
                                onClick = { currentTab = TvDestination.NOW_PLAYING }
                            )
                        }

                        // Bottom Navigation Bar with navigationBarsPadding
                        TvBottomNavBar(
                            currentDestination = currentTab,
                            onDestinationSelected = { currentTab = it },
                            theme = currentTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(64.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvScreensDispatcher(
    currentTab: TvDestination,
    playbackState: com.example.player.PlayerPlaybackState,
    allTracks: List<AudioTrack>,
    currentTheme: PlayerTheme,
    playerEngine: AudioPlayerEngine,
    context: android.content.Context,
    onUpdateTracks: (List<AudioTrack>) -> Unit,
    onSelectTheme: (PlayerTheme) -> Unit,
    onNavigateToDestination: (TvDestination) -> Unit
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    Crossfade(
        targetState = currentTab,
        label = "tv_screen_crossfade"
    ) { destination ->
        when (destination) {
            TvDestination.NOW_PLAYING -> {
                NowPlayingScreen(
                    state = playbackState,
                    playlist = allTracks,
                    theme = currentTheme,
                    onTogglePlayPause = { playerEngine.togglePlayPause() },
                    onPlayNext = { playerEngine.playNext() },
                    onPlayPrevious = { playerEngine.playPrevious() },
                    onSeekForward = { playerEngine.seekForward() },
                    onSeekRewind = { playerEngine.seekRewind() },
                    onToggleShuffle = { playerEngine.toggleShuffle() },
                    onToggleRepeat = { playerEngine.toggleRepeat() },
                    onSelectTrack = { track -> playerEngine.playTrack(track) }
                )
            }
            TvDestination.LIBRARY -> {
                LibraryScreen(
                    tracks = allTracks,
                    currentTrackId = playbackState.currentTrack?.id,
                    isPlaying = playbackState.isPlaying,
                    theme = currentTheme,
                    onTrackSelected = { track ->
                        playerEngine.playTrack(track)
                    },
                    onRescanLocal = {
                        coroutineScope.launch {
                            val localTracks = com.example.player.LocalMusicScanner.scanAll(context)
                            onUpdateTracks((localTracks + allTracks).distinctBy { it.id })
                        }
                    }
                )
            }
            TvDestination.SAMBA -> {
                SambaScreen(
                    theme = currentTheme,
                    currentTrackId = playbackState.currentTrack?.id,
                    isPlaying = playbackState.isPlaying,
                    onPlayTrack = { track ->
                        playerEngine.playTrack(track)
                    },
                    onAddTracksToLibrary = { newTracks ->
                        onUpdateTracks((newTracks + allTracks).distinctBy { it.id })
                    },
                    onNavigateToNowPlaying = { onNavigateToDestination(TvDestination.NOW_PLAYING) },
                    onNavigateToLibrary = { onNavigateToDestination(TvDestination.LIBRARY) }
                )
            }
            TvDestination.THEMES -> {
                ThemesScreen(
                    currentTheme = currentTheme,
                    onSelectTheme = onSelectTheme
                )
            }
            TvDestination.SETTINGS -> {
                SettingsScreen(
                    currentSleepTimerMinutes = playbackState.sleepTimerMinutesLeft,
                    onSetSleepTimer = { minutes -> playerEngine.setSleepTimer(minutes) },
                    theme = currentTheme,
                    onRescanLocal = suspend {
                        val localTracks = com.example.player.LocalMusicScanner.scanAll(context)
                        onUpdateTracks((localTracks + allTracks).distinctBy { it.id })
                        localTracks.size
                    }
                )
            }
        }
    }
}

@Composable
private fun TvNavRail(
    currentDestination: TvDestination,
    onDestinationSelected: (TvDestination) -> Unit,
    theme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        color = theme.surfaceDark,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(theme.primaryAccent.copy(alpha = 0.2f))
                    .border(1.5.dp, theme.primaryAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Sonora TV",
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Navigation items
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TvDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination

                    TvFocusableCard(
                        onClick = { onDestinationSelected(destination) },
                        testTag = "nav_${destination.name}",
                        shape = RoundedCornerShape(14.dp),
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color.Transparent,
                        focusedBackgroundColor = theme.primaryAccent.copy(alpha = 0.35f),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = if (isSelected) theme.primaryAccent else Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = destination.title,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Live status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(theme.primaryAccent)
            )
        }
    }
}

@Composable
private fun TvMiniPlayer(
    track: AudioTrack?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    theme: PlayerTheme,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClick: () -> Unit
) {
    if (track == null) return

    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    TvFocusableCard(
        onClick = onClick,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        focusedBorderColor = theme.primaryAccent,
        backgroundColor = theme.surfaceDark,
        focusedBackgroundColor = theme.surfaceDark,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = theme.primaryAccent,
                trackColor = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.coverUrl != null) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.format.extension.uppercase()} (${track.qualityDetails})",
                            color = theme.primaryAccent,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TvFocusableCard(
                        onClick = onPrev,
                        shape = CircleShape,
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = Color(0xFF1E293B),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        focusedBorderColor = Color.White,
                        backgroundColor = theme.primaryAccent,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = Color(0xFF070A12),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onNext,
                        shape = CircleShape,
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = Color(0xFF1E293B),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvBottomNavBar(
    currentDestination: TvDestination,
    onDestinationSelected: (TvDestination) -> Unit,
    theme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        color = theme.surfaceDark,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TvDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination

                TvFocusableCard(
                    onClick = { onDestinationSelected(destination) },
                    testTag = "bottom_nav_${destination.name}",
                    shape = RoundedCornerShape(12.dp),
                    focusedBorderColor = theme.primaryAccent,
                    backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.2f) else Color.Transparent,
                    focusedBackgroundColor = theme.primaryAccent.copy(alpha = 0.3f),
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            tint = if (isSelected) theme.primaryAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = destination.title,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
