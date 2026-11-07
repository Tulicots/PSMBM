package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.model.PlayerTheme
import com.example.player.PlayerPlaybackState
import com.example.ui.TvDestination

@Composable
fun TvSimulatorContainer(
    theme: PlayerTheme,
    isPlaying: Boolean,
    currentTab: TvDestination,
    playbackState: PlayerPlaybackState,
    allTracks: List<AudioTrack>,
    onSelectTab: (TvDestination) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekRewind: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onDpadDirection: (String) -> Unit,
    onCloseSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header of TV Simulator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "Simulador Android TV 55\" (16:9)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vista panorámica 100% fiel a Google TV",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            Surface(
                onClick = onCloseSimulator,
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF3B82F6))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Volver a Modo Móvil",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 16:9 Realistic Android TV Screen Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(2.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .shadow(16.dp, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // TV Screen Content rendered with TV 16:9 scaled proportions
            SimulatedTvScreenDisplay(
                currentTab = currentTab,
                playbackState = playbackState,
                allTracks = allTracks,
                theme = theme
            )

            // Realistic TV status LED (bottom center)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(width = 8.dp, height = 3.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFF00E5FF))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // On-Screen TV Remote Controller Panel
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF131927),
            border = BorderStroke(1.dp, Color(0xFF232D42)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TV D-Pad Status & Screen Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONTROL REMOTO ANDROID TV",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "Foco: ${currentTab.title}",
                            color = theme.primaryAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Destination Shortcuts (Jump directly on TV)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TvDestination.entries.forEach { dest ->
                        val isSelected = dest == currentTab
                        Surface(
                            onClick = { onSelectTab(dest) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.primaryAccent.copy(alpha = 0.3f) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isSelected) theme.primaryAccent else Color(0xFF334155)),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dest.title.substringBefore(" "),
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Tactile Circular D-Pad
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B0F19))
                        .border(1.5.dp, Color(0xFF26334D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // UP
                    DpadArrowButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Arriba",
                        onClick = { onDpadDirection("UP") },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                    )

                    // DOWN
                    DpadArrowButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Abajo",
                        onClick = { onDpadDirection("DOWN") },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                    )

                    // LEFT
                    DpadArrowButton(
                        icon = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Izquierda",
                        onClick = { onDpadDirection("LEFT") },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                    )

                    // RIGHT
                    DpadArrowButton(
                        icon = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Derecha",
                        onClick = { onDpadDirection("RIGHT") },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )

                    // CENTER OK BUTTON
                    Surface(
                        onClick = { onDpadDirection("SELECT") },
                        shape = CircleShape,
                        color = Color(0xFF2563EB),
                        border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Media & Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RemoteSmallButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "Atrás",
                        onClick = onBack
                    )

                    RemoteSmallButton(
                        icon = Icons.Default.FastRewind,
                        label = "-10s",
                        onClick = onSeekRewind
                    )

                    RemoteSmallButton(
                        icon = Icons.Default.SkipPrevious,
                        label = "Ant",
                        onClick = onPrev
                    )

                    RemoteSmallButton(
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        label = if (isPlaying) "Pausa" else "Play",
                        tint = Color(0xFF34D399),
                        onClick = onTogglePlayPause
                    )

                    RemoteSmallButton(
                        icon = Icons.Default.SkipNext,
                        label = "Sig",
                        onClick = onNext
                    )

                    RemoteSmallButton(
                        icon = Icons.Default.FastForward,
                        label = "+10s",
                        onClick = onSeekForward
                    )

                    RemoteSmallButton(
                        icon = Icons.Default.Home,
                        label = "Inicio",
                        onClick = onHome
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvScreenDisplay(
    currentTab: TvDestination,
    playbackState: PlayerPlaybackState,
    allTracks: List<AudioTrack>,
    theme: PlayerTheme
) {
    val currentTrack = playbackState.currentTrack ?: allTracks.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundDark)
    ) {
        // TV Navigation Rail (Scaled down for 16:9 container)
        Surface(
            color = theme.surfaceDark,
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .border(BorderStroke(0.5.dp, Color(0xFF1E293B)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // TV Logo
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }

                TvDestination.entries.forEach { dest ->
                    val isSelected = dest == currentTab
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) theme.primaryAccent.copy(alpha = 0.3f) else Color.Transparent)
                            .border(if (isSelected) BorderStroke(1.dp, theme.primaryAccent) else BorderStroke(0.dp, Color.Transparent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title,
                            tint = if (isSelected) theme.primaryAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent)
                )
            }
        }

        // TV Main Display Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            when (currentTab) {
                TvDestination.NOW_PLAYING -> {
                    SimulatedTvNowPlaying(
                        track = currentTrack,
                        state = playbackState,
                        playlist = allTracks,
                        theme = theme
                    )
                }
                TvDestination.SAMBA -> {
                    SimulatedTvSamba(theme = theme)
                }
                TvDestination.LIBRARY -> {
                    SimulatedTvLibrary(tracks = allTracks, currentTrack = currentTrack, theme = theme)
                }
                TvDestination.THEMES -> {
                    SimulatedTvThemes(theme = theme)
                }
                TvDestination.SETTINGS -> {
                    SimulatedTvSettings(theme = theme, sleepMinutes = playbackState.sleepTimerMinutesLeft)
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvNowPlaying(
    track: AudioTrack?,
    state: PlayerPlaybackState,
    playlist: List<AudioTrack>,
    theme: PlayerTheme
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Column: TV Player (Art, Title, Visualizer, Progress, Buttons)
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header format badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(track?.format?.badgeColorHex ?: 0xFF3B82F6).copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, Color(track?.format?.badgeColorHex ?: 0xFF3B82F6))
                ) {
                    Text(
                        text = "● ${track?.format?.displayName ?: "FLAC"} • ${track?.qualityDetails ?: "24-bit / 96kHz"}",
                        color = Color(track?.format?.badgeColorHex ?: 0xFF3B82F6),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = "Android TV 16:9",
                    color = Color(0xFF64748B),
                    fontSize = 7.sp
                )
            }

            // Cover + Metadata + Visualizer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mini Cover
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (track?.coverUrl != null) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "Sin pista",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.artist ?: "Sonora Player",
                        color = theme.primaryAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Mini Visualizer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AudioVisualizer(
                            isPlaying = state.isPlaying,
                            primaryColor = theme.primaryAccent,
                            secondaryColor = theme.secondaryAccent
                        )
                    }
                }
            }

            // Seeker Bar & TV Controls
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val progress = if (state.durationMs > 0) {
                    (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = theme.primaryAccent,
                    trackColor = Color(0xFF334155)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatMiniTime(state.currentPositionMs), color = Color(0xFF94A3B8), fontSize = 7.sp)
                    Text(text = formatMiniTime(state.durationMs), color = Color(0xFF94A3B8), fontSize = 7.sp)
                }

                // TV Remote Controllable Button Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = if (state.isShuffleEnabled) theme.primaryAccent else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    // TV Focused Play Button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.primaryAccent)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF070A10),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                }
            }
        }

        // Right Column: TV Queue Preview (Fixed width so it NEVER wraps)
        Column(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceDark)
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Cola (${playlist.size})",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            playlist.take(3).forEachIndexed { idx, itm ->
                val isCurrent = itm.id == track?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCurrent) theme.primaryAccent.copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.6f))
                        .border(if (isCurrent) BorderStroke(0.5.dp, theme.primaryAccent) else BorderStroke(0.dp, Color.Transparent))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${idx + 1}. ${itm.title}",
                        color = if (isCurrent) theme.primaryAccent else Color.White,
                        fontSize = 8.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = itm.formattedDuration(),
                        color = Color(0xFF94A3B8),
                        fontSize = 7.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvSamba(theme: PlayerTheme) {
    val cursorTransition = rememberInfiniteTransition(label = "tv_cursor")
    val alpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), repeatMode = RepeatMode.Reverse),
        label = "c"
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Column: LCD Display & Status
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "CONEXIÓN RED SAMBA (SMB)", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF070A12))
                        .border(1.dp, Color(0xFF232D42), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "192.168.1.100",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(width = 4.dp, height = 12.dp)
                                .alpha(alpha)
                                .background(Color(0xFF34D399))
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Conectar al Servidor SMB", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "✓ SMBv2/SMBv3 • Puerto 445 • Conexión LAN directa",
                color = Color(0xFF34D399),
                fontSize = 7.sp
            )
        }

        // Right Column: Keypad Preview
        Column(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF151C2C))
                .border(0.5.dp, Color(0xFF232D42), RoundedCornerShape(8.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TECLADO TV INVERTIDO",
                color = Color(0xFF94A3B8),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )

            val miniRows = listOf(
                listOf("6", "1", "8"),
                listOf("7", "0", "5"),
                listOf("2", "3", "4"),
                listOf(".", "9", "⌫")
            )

            miniRows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (key == "0") Color(0xFF1E3A8A) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = key, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvLibrary(tracks: List<AudioTrack>, currentTrack: AudioTrack?, theme: PlayerTheme) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Biblioteca TV (${tracks.size} pistas)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "Formatos: FLAC, MP3, M4A, WAV, OGG", color = theme.primaryAccent, fontSize = 7.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tracks.take(3).forEach { trk ->
                val isSel = trk.id == currentTrack?.id
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) theme.primaryAccent.copy(alpha = 0.25f) else Color(0xFF1E293B),
                    border = BorderStroke(0.5.dp, if (isSel) theme.primaryAccent else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(trk.format.badgeColorHex)
                        ) {
                            Text(text = trk.format.extension.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 3.dp))
                        }
                        Text(text = trk.title, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = trk.artist, color = Color(0xFF94A3B8), fontSize = 7.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvThemes(theme: PlayerTheme) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Temas Visuales Android TV", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayerTheme.entries.take(4).forEach { thm ->
                val isSel = thm == theme
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = thm.surfaceDark,
                    border = BorderStroke(1.dp, if (isSel) thm.primaryAccent else Color(0xFF334155)),
                    modifier = Modifier.weight(1f).height(68.dp)
                ) {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(thm.primaryAccent, thm.secondaryAccent)))
                        )
                        Text(text = thm.title.substringBefore(" "), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedTvSettings(theme: PlayerTheme, sleepMinutes: Int) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Text("Ajustes del Sistema Android TV", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text("Temporizador", color = Color(0xFF94A3B8), fontSize = 7.sp)
                    Text(text = if (sleepMinutes > 0) "$sleepMinutes min" else "Desactivado", color = theme.primaryAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text("Motor ExoPlayer", color = Color(0xFF94A3B8), fontSize = 7.sp)
                    Text("Media3 Hi-Res", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("✓ 100% Local • Cero Anuncios • Cero Telemetría", color = Color(0xFF10B981), fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DpadArrowButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RemoteSmallButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.size(width = 44.dp, height = 40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatMiniTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
