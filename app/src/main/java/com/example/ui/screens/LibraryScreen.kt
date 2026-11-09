package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.PlayerTheme
import com.example.model.TrackSource
import com.example.ui.components.TvFocusableCard

enum class LibrarySourceFilter(val displayName: String, val icon: String) {
    ALL("Todas", "🎵"),
    LOCAL("Música Local", "📱"),
    SAMBA("Música Red (Samba)", "🌐")
}

@Composable
fun LibraryScreen(
    tracks: List<AudioTrack>,
    currentTrackId: String?,
    isPlaying: Boolean,
    theme: PlayerTheme,
    onTrackSelected: (AudioTrack) -> Unit,
    onRescanLocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSourceFilter by remember { mutableStateOf(LibrarySourceFilter.ALL) }
    var selectedFormatFilter by remember { mutableStateOf<AudioFormat?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }

    val localTracksCount = remember(tracks) {
        tracks.count { it.source == TrackSource.LOCAL_STORAGE || it.source == TrackSource.HI_FI_DEMO }
    }
    val sambaTracksCount = remember(tracks) {
        tracks.count { it.source == TrackSource.SAMBA_SERVER }
    }

    val filteredTracks = remember(tracks, selectedSourceFilter, selectedFormatFilter, searchQuery) {
        tracks.filter { track ->
            val matchesSource = when (selectedSourceFilter) {
                LibrarySourceFilter.ALL -> true
                LibrarySourceFilter.LOCAL -> track.source == TrackSource.LOCAL_STORAGE || track.source == TrackSource.HI_FI_DEMO
                LibrarySourceFilter.SAMBA -> track.source == TrackSource.SAMBA_SERVER
            }
            val matchesFormat = selectedFormatFilter == null || track.format == selectedFormatFilter
            val matchesSearch = searchQuery.isBlank() ||
                track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.album.contains(searchQuery, ignoreCase = true)

            matchesSource && matchesFormat && matchesSearch
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWidescreen = maxWidth >= 760.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWidescreen) 24.dp else 16.dp, vertical = 14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Biblioteca de Música",
                        color = Color.White,
                        fontSize = if (isWidescreen) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredTracks.size} pistas (${localTracksCount} local • ${sambaTracksCount} red Samba)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // Rescan button
                TvFocusableCard(
                    onClick = {
                        isScanning = true
                        onRescanLocal()
                        // Small UI timeout for feedback
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isScanning = false
                        }, 1200)
                    },
                    shape = RoundedCornerShape(10.dp),
                    focusedBorderColor = theme.primaryAccent,
                    backgroundColor = Color(0xFF1E293B),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = theme.primaryAccent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Escanear almacenamiento",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isScanning) "Buscando..." else "Escanear Local",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Level 1: SOURCE ORIGIN (Todas / Música Local / Música Red Samba)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LibrarySourceFilter.entries.forEach { sourceFilter ->
                    val isSelected = selectedSourceFilter == sourceFilter
                    val count = when (sourceFilter) {
                        LibrarySourceFilter.ALL -> tracks.size
                        LibrarySourceFilter.LOCAL -> localTracksCount
                        LibrarySourceFilter.SAMBA -> sambaTracksCount
                    }

                    TvFocusableCard(
                        onClick = { selectedSourceFilter = sourceFilter },
                        testTag = "source_filter_${sourceFilter.name}",
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color(0xFF151C2C),
                        focusedBackgroundColor = theme.primaryAccent.copy(alpha = 0.35f),
                        borderWidth = if (isSelected) 2.dp else 1.dp,
                        unfocusedBorderColor = if (isSelected) theme.primaryAccent else Color(0xFF232D42),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${sourceFilter.icon} ${sourceFilter.displayName}",
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) theme.primaryAccent else Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isSelected) Color(0xFF070A12) else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Level 2: AUDIO FORMATS (FLAC, MP3, M4A, WAV, OGG)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatFilterChip(
                    label = "Todos los formatos",
                    isSelected = selectedFormatFilter == null,
                    primaryColor = Color(0xFF94A3B8),
                    onClick = { selectedFormatFilter = null }
                )

                AudioFormat.entries.forEach { format ->
                    FormatFilterChip(
                        label = format.displayName,
                        isSelected = selectedFormatFilter == format,
                        primaryColor = Color(format.badgeColorHex),
                        onClick = { selectedFormatFilter = format }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Tracks or Empty State
            if (filteredTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedSourceFilter == LibrarySourceFilter.SAMBA) Icons.Default.Router else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (selectedSourceFilter == LibrarySourceFilter.SAMBA) {
                                "No hay música de red Samba sincronizada aún"
                            } else {
                                "No se encontraron archivos de música con los filtros actuales"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (selectedSourceFilter == LibrarySourceFilter.SAMBA) {
                                "Ve a la pestaña 'Samba SMB' y conéctate a tu servidor local para indexar tu biblioteca compartida."
                            } else {
                                "Prueba cambiando los filtros o presiona 'Escanear Local' para buscar en carpetas del dispositivo."
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = if (isWidescreen) 210.dp else 156.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTracks, key = { it.id }) { track ->
                        val isSelected = track.id == currentTrackId

                        TvFocusableCard(
                            onClick = { onTrackSelected(track) },
                            testTag = "library_item_${track.id}",
                            focusedBorderColor = theme.primaryAccent,
                            backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.15f) else Color(0xFF131B2E),
                            focusedBackgroundColor = Color(0xFF1E2B45),
                            borderWidth = if (isSelected) 1.5.dp else 1.dp,
                            unfocusedBorderColor = if (isSelected) theme.primaryAccent else Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Cover Thumbnail
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.22f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0B0F1A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (track.coverUrl != null) {
                                        AsyncImage(
                                            model = track.coverUrl,
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Album,
                                            contentDescription = null,
                                            tint = theme.primaryAccent.copy(alpha = 0.6f),
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }

                                    // Top-left: Format badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(track.format.badgeColorHex).copy(alpha = 0.95f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = track.format.extension.uppercase(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Top-right: Source badge (Local vs Samba)
                                    val isSamba = track.source == TrackSource.SAMBA_SERVER
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSamba) Color(0xFF1D4ED8).copy(alpha = 0.95f) else Color(0xFF065F46).copy(alpha = 0.95f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSamba) "🌐 Samba" else "📱 Local",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Playing Indicator badge
                                    if (isSelected && isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .clip(CircleShape)
                                                .background(theme.primaryAccent)
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Title & Artist
                                Text(
                                    text = track.title,
                                    color = if (isSelected) theme.primaryAccent else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = track.artist,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (track.fileSizeMb > 0f) "%.1f MB".format(track.fileSizeMb) else track.qualityDetails.substringBefore("•").trim(),
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = track.formattedDuration(),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatFilterChip(
    label: String,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        focusedBorderColor = primaryColor,
        backgroundColor = if (isSelected) primaryColor.copy(alpha = 0.25f) else Color(0xFF161F30),
        focusedBackgroundColor = primaryColor.copy(alpha = 0.35f),
        borderWidth = if (isSelected) 2.dp else 1.dp,
        unfocusedBorderColor = if (isSelected) primaryColor else Color(0xFF26334D),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
