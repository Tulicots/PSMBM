package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerTheme
import com.example.player.LocalMusicScanner
import com.example.ui.components.TvFocusableCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentSleepTimerMinutes: Int,
    onSetSleepTimer: (Int) -> Unit,
    theme: PlayerTheme,
    onRescanLocal: suspend () -> Int = { 0 },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var customFolders by remember { mutableStateOf(LocalMusicScanner.getCustomFolders(context)) }
    var newFolderPathInput by remember { mutableStateOf("") }
    var isScanningLocal by remember { mutableStateOf(false) }
    var scanFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.primaryAccent.copy(alpha = 0.2f))
                    .border(1.dp, theme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "Ajustes del Reproductor TV",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Carpetas locales de música, almacenamiento USB y motor de audio",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Section 1: Carpetas Locales de Búsqueda Automática
        SettingsCard(
            icon = Icons.Default.FolderOpen,
            title = "Búsqueda Automática de Música Local",
            description = "Cada vez que abres la app, busca automáticamente pistas FLAC, MP3, M4A, WAV y OGG sin necesidad de configuración manual. También puedes registrar carpetas adicionales o unidades USB de tu Android TV.",
            theme = theme
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Auto-scanned standard directories notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Text("Rutas exploradas automáticamente en cada inicio:", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("• Almacenamiento Android: /Music y /Download", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        Text("• Unidades USB externas en Android TV: /storage/*", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        Text("• Biblioteca MediaStore del sistema Android", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    }
                }

                // Custom Folders List
                if (customFolders.isNotEmpty()) {
                    Text(
                        text = "Carpetas Personalizadas Agregadas:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    customFolders.forEach { folderPath ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = folderPath,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }

                                TvFocusableCard(
                                    onClick = {
                                        LocalMusicScanner.removeCustomFolder(context, folderPath)
                                        customFolders = LocalMusicScanner.getCustomFolders(context)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    focusedBorderColor = Color(0xFFFB7185),
                                    backgroundColor = Color(0xFF991B1B).copy(alpha = 0.3f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFB7185), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Custom Folder Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFolderPathInput,
                        onValueChange = { newFolderPathInput = it },
                        placeholder = { Text("Ej: /storage/emulated/0/Audio", fontSize = 11.sp) },
                        label = { Text("Ruta de carpeta personalizada", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFCBD5E1),
                            focusedBorderColor = theme.primaryAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    TvFocusableCard(
                        onClick = {
                            if (newFolderPathInput.isNotBlank()) {
                                LocalMusicScanner.addCustomFolder(context, newFolderPathInput.trim())
                                customFolders = LocalMusicScanner.getCustomFolders(context)
                                newFolderPathInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = Color(0xFF2563EB),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Añadir", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick directory suggestion chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("/storage/emulated/0/Music", "/storage/emulated/0/Download", "/storage/USB_DISK/Musica").forEach { samplePath ->
                        Surface(
                            onClick = { newFolderPathInput = samplePath },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(0.5.dp, Color(0xFF475569))
                        ) {
                            Text(
                                text = "+ $samplePath",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rescan Action Button
                TvFocusableCard(
                    onClick = {
                        isScanningLocal = true
                        scanFeedbackMessage = null
                        coroutineScope.launch {
                            val count = onRescanLocal()
                            scanFeedbackMessage = "✓ Escaneo completado. Pistas locales disponibles: $count"
                            isScanningLocal = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    focusedBorderColor = theme.primaryAccent,
                    backgroundColor = theme.primaryAccent.copy(alpha = 0.2f),
                    borderWidth = 1.dp,
                    unfocusedBorderColor = theme.primaryAccent,
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isScanningLocal) {
                            CircularProgressIndicator(
                                color = theme.primaryAccent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando pistas locales...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear Almacenamiento Local Ahora", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (scanFeedbackMessage != null) {
                    Text(
                        text = scanFeedbackMessage!!,
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section 2: Temporizador para Dormir
        SettingsCard(
            icon = Icons.Default.Bedtime,
            title = "Temporizador para Dormir",
            description = if (currentSleepTimerMinutes > 0)
                "La música se detendrá automáticamente en $currentSleepTimerMinutes minutos."
            else "Apagado programado de reproducción en la televisión.",
            theme = theme
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(0, 15, 30, 45, 60, 90).forEach { minutes ->
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

        // Section 3: Audio Engine
        SettingsCard(
            icon = Icons.Default.VolumeUp,
            title = "Motor de Audio AndroidX Media3 (ExoPlayer)",
            description = "Decodificación nativa por hardware para FLAC 24/96 Hi-Res, MP3 CBR/VBR, AAC Lossless, WAV Studio Master y OGG Vorbis.",
            theme = theme
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("FLAC 24-bit Hi-Res", "MP3 320 kbps", "M4A Lossless", "WAV Studio", "OGG Vorbis").forEach { fmt ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155))
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

        // Section 4: Privacy & Zero Bloat
        SettingsCard(
            icon = Icons.Default.Security,
            title = "100% Local & Privado",
            description = "Cero servicios de telemetría, cero publicidad, cero conexión a servidores externos de tracking. Tu música se reproduce directamente de tus archivos locales o de tu servidor Samba en tu red privada de casa.",
            theme = theme
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Text(
                    text = "✓ Cero Telemetría • Cero Anuncios • 100% Red Local & Almacenamiento",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
        border = BorderStroke(1.dp, Color(0xFF334155)),
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
