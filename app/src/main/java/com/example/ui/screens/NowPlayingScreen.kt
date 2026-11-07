package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.model.PlayerTheme
import com.example.player.PlayerPlaybackState
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.TvFocusableCard

@Composable
fun NowPlayingScreen(
    state: PlayerPlaybackState,
    playlist: List<AudioTrack>,
    theme: PlayerTheme,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekRewind: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSelectTrack: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: playlist.firstOrNull()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isWidescreen = (isLandscape && maxWidth >= 760.dp)

        if (isWidescreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Album Art + Visualizer + Track Details + Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Track Info Header & Format Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Audio Quality Badge (FLAC, MP3, etc.)
                        if (track != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(track.format.badgeColorHex).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(track.format.badgeColorHex)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(track.format.badgeColorHex))
                                    )
                                    Text(
                                        text = track.format.displayName,
                                        color = Color(track.format.badgeColorHex),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = track.qualityDetails,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Center: Album Art & Ambient Glow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // Album Art with glowing shadow
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ambient glow backdrop
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(24.dp)
                                    .background(theme.ambientGlow)
                            )

                            if (track?.coverUrl != null) {
                                AsyncImage(
                                    model = track.coverUrl,
                                    contentDescription = "Carátula del álbum",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                        .border(2.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = theme.primaryAccent,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }
                        }

                        // Title, Artist, Album, and Live Audio Waveform Visualizer
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = track?.title ?: "Sin pista seleccionada",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = track?.artist ?: "Selecciona una canción",
                                color = theme.primaryAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track?.album ?: "",
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Audio Visualizer wave bars
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                AudioVisualizer(
                                    isPlaying = state.isPlaying,
                                    primaryColor = theme.primaryAccent,
                                    secondaryColor = theme.secondaryAccent
                                )
                            }
                        }
                    }

                    // Bottom Area: Progress Bar & Player Controls
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Seeker Bar
                        val progress = if (state.durationMs > 0) {
                            (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = theme.primaryAccent,
                            trackColor = Color(0xFF334155),
                        )

                        // Timers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatMillis(state.currentPositionMs),
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatMillis(state.durationMs),
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // D-Pad Controllable TV Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TvIconButton(
                                icon = Icons.Default.Shuffle,
                                contentDescription = "Aleatorio",
                                isActive = state.isShuffleEnabled,
                                primaryColor = theme.primaryAccent,
                                onClick = onToggleShuffle,
                                size = 46.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            TvIconButton(
                                icon = Icons.Default.SkipPrevious,
                                contentDescription = "Pista anterior",
                                primaryColor = theme.primaryAccent,
                                onClick = onPlayPrevious,
                                size = 50.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            TvIconButton(
                                icon = Icons.Default.FastRewind,
                                contentDescription = "Retroceder 10s",
                                primaryColor = theme.primaryAccent,
                                onClick = onSeekRewind,
                                size = 46.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            TvPlayPauseButton(
                                isPlaying = state.isPlaying,
                                primaryColor = theme.primaryAccent,
                                onClick = onTogglePlayPause
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            TvIconButton(
                                icon = Icons.Default.FastForward,
                                contentDescription = "Adelantar 10s",
                                primaryColor = theme.primaryAccent,
                                onClick = onSeekForward,
                                size = 46.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            TvIconButton(
                                icon = Icons.Default.SkipNext,
                                contentDescription = "Pista siguiente",
                                primaryColor = theme.primaryAccent,
                                onClick = onPlayNext,
                                size = 50.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            val repeatIcon = if (state.repeatMode == Player.REPEAT_MODE_ONE) {
                                Icons.Default.RepeatOne
                            } else {
                                Icons.Default.Repeat
                            }
                            TvIconButton(
                                icon = repeatIcon,
                                contentDescription = "Repetir",
                                isActive = state.repeatMode != Player.REPEAT_MODE_OFF,
                                primaryColor = theme.primaryAccent,
                                onClick = onToggleRepeat,
                                size = 46.dp
                            )
                        }

                        if (state.errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                            ) {
                                Text(
                                    text = state.errorMessage,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Right Column: Up Next / Current Queue Preview (Fixed width to avoid vertical word wrap)
                Column(
                    modifier = Modifier
                        .width(310.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.surfaceDark.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cola de Reproducción",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${playlist.size} pistas",
                            color = theme.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(playlist) { index, item ->
                            val isCurrent = item.id == track?.id
                            QueueTrackCard(
                                track = item,
                                index = index + 1,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && state.isPlaying,
                                primaryColor = theme.primaryAccent,
                                onClick = { onSelectTrack(item) }
                            )
                        }
                    }
                }
            }
        } else {
            // Adaptive Vertical Layout for portrait phone/tablet
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header format badge
                if (track != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(track.format.badgeColorHex).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(track.format.badgeColorHex))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(track.format.badgeColorHex))
                                )
                                Text(
                                    text = track.format.displayName,
                                    color = Color(track.format.badgeColorHex),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = track.qualityDetails,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Centered Album Art with ambient glow
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                            .background(theme.ambientGlow)
                    )

                    if (track?.coverUrl != null) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = "Carátula",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }

                // Title & Artist
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = track?.title ?: "Sin pista seleccionada",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track?.artist ?: "",
                        color = theme.primaryAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!track?.album.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track?.album ?: "",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Real-time Audio Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    AudioVisualizer(
                        isPlaying = state.isPlaying,
                        primaryColor = theme.primaryAccent,
                        secondaryColor = theme.secondaryAccent
                    )
                }

                // Seeker Bar & Timers
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val progress = if (state.durationMs > 0) {
                        (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = theme.primaryAccent,
                        trackColor = Color(0xFF334155),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatMillis(state.currentPositionMs), color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = formatMillis(state.durationMs), color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Mobile Player Controls Row (Comfortable 48dp touch targets)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvIconButton(
                        icon = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        isActive = state.isShuffleEnabled,
                        primaryColor = theme.primaryAccent,
                        onClick = onToggleShuffle,
                        size = 48.dp
                    )
                    TvIconButton(
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Pista anterior",
                        primaryColor = theme.primaryAccent,
                        onClick = onPlayPrevious,
                        size = 48.dp
                    )
                    TvIconButton(
                        icon = Icons.Default.FastRewind,
                        contentDescription = "Retroceder 10s",
                        primaryColor = theme.primaryAccent,
                        onClick = onSeekRewind,
                        size = 48.dp
                    )
                    TvPlayPauseButton(
                        isPlaying = state.isPlaying,
                        primaryColor = theme.primaryAccent,
                        onClick = onTogglePlayPause
                    )
                    TvIconButton(
                        icon = Icons.Default.FastForward,
                        contentDescription = "Adelantar 10s",
                        primaryColor = theme.primaryAccent,
                        onClick = onSeekForward,
                        size = 48.dp
                    )
                    TvIconButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Pista siguiente",
                        primaryColor = theme.primaryAccent,
                        onClick = onPlayNext,
                        size = 48.dp
                    )
                    val repeatIcon = if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    TvIconButton(
                        icon = repeatIcon,
                        contentDescription = "Repetir",
                        isActive = state.repeatMode != Player.REPEAT_MODE_OFF,
                        primaryColor = theme.primaryAccent,
                        onClick = onToggleRepeat,
                        size = 48.dp
                    )
                }

                if (state.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Full Queue preview section for mobile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surfaceDark)
                        .border(1.dp, Color(0xFF26334D), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cola de Reproducción",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${playlist.size} pistas en lista",
                            color = theme.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    playlist.forEachIndexed { idx, item ->
                        val isCurrent = item.id == track?.id
                        QueueTrackCard(
                            track = item,
                            index = idx + 1,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && state.isPlaying,
                            primaryColor = theme.primaryAccent,
                            onClick = { onSelectTrack(item) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun QueueTrackCard(
    track: AudioTrack,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        testTag = "queue_item_${track.id}",
        focusedBorderColor = primaryColor,
        backgroundColor = if (isCurrent) primaryColor.copy(alpha = 0.15f) else Color(0xFF1E293B).copy(alpha = 0.6f),
        focusedBackgroundColor = Color(0xFF334155)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Index or Playing indicator
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "$index",
                        color = if (isCurrent) primaryColor else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) primaryColor else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.format.extension.uppercase()}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = track.formattedDuration(),
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TvPlayPauseButton(
    isPlaying: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(68.dp)
            .scale(if (isFocused) 1.15f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_play_pause_button"),
        shape = CircleShape,
        color = if (isFocused) primaryColor else primaryColor.copy(alpha = 0.85f),
        border = if (isFocused) androidx.compose.foundation.BorderStroke(3.dp, Color.White) else null,
        shadowElevation = if (isFocused) 12.dp else 4.dp,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun TvIconButton(
    icon: ImageVector,
    contentDescription: String,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    isActive: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .scale(if (isFocused) 1.12f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_btn_$contentDescription"),
        shape = CircleShape,
        color = when {
            isFocused -> primaryColor.copy(alpha = 0.35f)
            isActive -> primaryColor.copy(alpha = 0.2f)
            else -> Color(0xFF1E293B)
        },
        border = when {
            isFocused -> androidx.compose.foundation.BorderStroke(2.dp, primaryColor)
            isActive -> androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        },
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isFocused || isActive) primaryColor else Color(0xFFE2E8F0),
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
