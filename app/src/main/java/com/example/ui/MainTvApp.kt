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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    var currentTab by remember { mutableStateOf(TvDestination.NOW_PLAYING) }
    var currentTheme by remember { mutableStateOf(PlayerTheme.OLED_NEON) }

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

    // Back button behavior: Return to Now Playing if browsing other tabs
    BackHandler(enabled = currentTab != TvDestination.NOW_PLAYING) {
        currentTab = TvDestination.NOW_PLAYING
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
        // Pure Native Android TV & Google TV Widescreen Interface
        Row(modifier = Modifier.fillMaxSize()) {
            TvNavRail(
                currentDestination = currentTab,
                onDestinationSelected = { currentTab = it },
                theme = currentTheme,
                modifier = Modifier
                    .width(92.dp)
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
