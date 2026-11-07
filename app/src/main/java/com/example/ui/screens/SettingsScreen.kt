package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerTheme
import com.example.ui.components.TvFocusableCard

@Composable
fun SettingsScreen(
    currentSleepTimerMinutes: Int,
    onSetSleepTimer: (Int) -> Unit,
    theme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Ajustes del Reproductor TV",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rendimiento local, audio hi-fi y temporizador para dormir",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Sleep Timer Section
        SettingsCard(
            icon = Icons.Default.Bedtime,
            title = "Temporizador para Dormir",
            description = if (currentSleepTimerMinutes > 0)
                "La música se detendrá automáticamente en $currentSleepTimerMinutes minutos"
            else "Apagado programado de reproducción en la televisión",
            theme = theme
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(0, 15, 30, 45, 60).forEach { minutes ->
                    val isSelected = currentSleepTimerMinutes == minutes
                    TvFocusableCard(
                        onClick = { onSetSleepTimer(minutes) },
                        shape = RoundedCornerShape(10.dp),
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color(0xFF161F30),
                        focusedBackgroundColor = theme.primaryAccent.copy(alpha = 0.35f),
                        borderWidth = if (isSelected) 2.dp else 1.dp,
                        unfocusedBorderColor = if (isSelected) theme.primaryAccent else Color(0xFF26334D),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (minutes == 0) "Desactivado" else "${minutes}m",
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Audio Engine Section
        SettingsCard(
            icon = Icons.Default.VolumeUp,
            title = "Motor de Audio AndroidX Media3 (ExoPlayer)",
            description = "Decodificación nativa por hardware para FLAC 24/96, MP3 CBR/VBR, AAC Lossless, WAV y OGG Vorbis.",
            theme = theme
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("FLAC 24-bit", "MP3 320k", "M4A AAC", "WAV", "OGG").forEach { fmt ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = fmt,
                            color = theme.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Privacy & Zero Bloat Section
        SettingsCard(
            icon = Icons.Default.Security,
            title = "100% Local & Privado",
            description = "Cero servicios de telemetría, cero publicidad, cero conexión a servidores externos. Tu música se reproduce directamente de tus archivos o de tu servidor local Samba.",
            theme = theme
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Text(
                    text = "✓ Cero Telemetría • Cero Publicidad • Local-First",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    description: String,
    theme: PlayerTheme,
    action: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = theme.surfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            action()
        }
    }
}
