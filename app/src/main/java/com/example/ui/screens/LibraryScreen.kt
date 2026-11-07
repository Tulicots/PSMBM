package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
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
import com.example.ui.components.TvFocusableCard

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
    var selectedFilter by remember { mutableStateOf<AudioFormat?>(null) }

    val filteredTracks = remember(tracks, selectedFilter) {
        if (selectedFilter == null) tracks else tracks.filter { it.format == selectedFilter }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWidescreen = maxWidth >= 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWidescreen) 24.dp else 16.dp, vertical = 14.dp)
        ) {
            if (isWidescreen) {
                // TV Widescreen Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Biblioteca de Música",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredTracks.size} pistas • Audio Local & Samba",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatFilterChipsRow(
                            selectedFilter = selectedFilter,
                            theme = theme,
                            onSelectFilter = { selectedFilter = it },
                            onRescanLocal = onRescanLocal
                        )
                    }
                }
            } else {
                // Mobile Header: Stacked Title + Horizontal Scrollable Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Biblioteca de Música",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredTracks.size} archivos encontrados",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        // Compact scan icon button
                        TvFocusableCard(
                            onClick = onRescanLocal,
                            shape = RoundedCornerShape(10.dp),
                            focusedBorderColor = theme.primaryAccent,
                            backgroundColor = Color(0xFF1E293B),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Escanear",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Escanear", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Horizontally scrollable chips on mobile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatFilterChip(
                            label = "Todos",
                            isSelected = selectedFilter == null,
                            primaryColor = theme.primaryAccent,
                            onClick = { selectedFilter = null }
                        )

                        AudioFormat.entries.forEach { format ->
                            FormatFilterChip(
                                label = format.displayName,
                                isSelected = selectedFilter == format,
                                primaryColor = Color(format.badgeColorHex),
                                onClick = { selectedFilter = format }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Tracks
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isWidescreen) 200.dp else 150.dp),
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Cover Thumbnail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f)
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
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            // Format badge top-left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(track.format.badgeColorHex).copy(alpha = 0.9f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = track.format.extension.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Playing Indicator badge
                            if (isSelected && isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
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
                            fontSize = 15.sp,
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
                                text = track.qualityDetails.substringBefore("•").trim(),
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                            Text(
                                text = track.formattedDuration(),
                                color = Color(0xFF64748B),
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

@Composable
private fun FormatFilterChipsRow(
    selectedFilter: AudioFormat?,
    theme: PlayerTheme,
    onSelectFilter: (AudioFormat?) -> Unit,
    onRescanLocal: () -> Unit
) {
    FormatFilterChip(
        label = "Todos",
        isSelected = selectedFilter == null,
        primaryColor = theme.primaryAccent,
        onClick = { onSelectFilter(null) }
    )

    AudioFormat.entries.forEach { format ->
        FormatFilterChip(
            label = format.displayName,
            isSelected = selectedFilter == format,
            primaryColor = Color(format.badgeColorHex),
            onClick = { onSelectFilter(format) }
        )
    }

    TvFocusableCard(
        onClick = onRescanLocal,
        shape = RoundedCornerShape(10.dp),
        focusedBorderColor = theme.primaryAccent,
        backgroundColor = Color(0xFF1E293B),
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Escanear almacenamiento",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Escanear",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
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
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
