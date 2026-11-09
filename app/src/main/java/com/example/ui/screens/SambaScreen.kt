package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrack
import com.example.model.PlayerTheme
import com.example.model.SambaServer
import com.example.model.SambaShareItem
import com.example.samba.SambaManager
import com.example.ui.components.TvFocusableCard
import com.example.ui.components.TvNumpad
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: String,
    val message: String,
    val isSuccess: Boolean = false,
    val isError: Boolean = false
)

@Composable
fun SambaScreen(
    theme: PlayerTheme,
    currentTrackId: String?,
    isPlaying: Boolean,
    onPlayTrack: (AudioTrack) -> Unit,
    onAddTracksToLibrary: (List<AudioTrack>) -> Unit,
    onNavigateToNowPlaying: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hostIp by remember { mutableStateOf("192.168.1.100") }
    var shareName by remember { mutableStateOf("Musica") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(true) }

    var connectedServer by remember { mutableStateOf<SambaServer?>(null) }
    var showExplorer by remember { mutableStateOf(false) }
    var currentPath by remember { mutableStateOf("/") }
    var shareItems by remember { mutableStateOf<List<SambaShareItem>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    var isLoadingPath by remember { mutableStateOf(false) }

    val logs = remember {
        mutableStateListOf(
            LogEntry("14:00:00", "[Samba Service] Listo para recibir dirección de red...", isSuccess = false),
            LogEntry("14:00:01", "Protocolo SMBv2/SMBv3 habilitado en puerto 445.", isSuccess = true)
        )
    }

    val quickPresets = listOf(
        "192.168.1.100",
        "192.168.1.1",
        "192.168.0.1",
        "172.16.0.1"
    )

    fun addLog(msg: String, isSuccess: Boolean = false, isError: Boolean = false) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logs.add(LogEntry(time, msg, isSuccess, isError))
    }

    // Cursor blink transition for LCD screen
    val cursorTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    // Function to load a directory inside connected share
    fun loadDirectory(path: String) {
        val server = connectedServer ?: return
        isLoadingPath = true
        coroutineScope.launch {
            val result = SambaManager.browseDirectory(server, path, context)
            result.onSuccess { items ->
                currentPath = path
                shareItems = items
                val audioCount = items.count { it.isAudioFile }
                val dirCount = items.count { it.isDirectory }
                addLog("Carpeta $path: $dirCount carpetas y $audioCount pistas de audio.", isSuccess = true)
            }.onFailure { err ->
                addLog("Error al abrir $path: ${err.message}", isError = true)
            }
            isLoadingPath = false
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isWidescreen = isLandscape && maxWidth >= 760.dp

        if (connectedServer != null) {
            if (showExplorer) {
                // Detailed Explorer with back to summary
                SmbFileExplorer(
                    server = connectedServer!!,
                    currentPath = currentPath,
                    items = shareItems,
                    isLoading = isLoadingPath,
                    currentTrackId = currentTrackId,
                    isPlaying = isPlaying,
                    theme = theme,
                    onBackToSummary = { showExplorer = false },
                    onNavigateFolder = { folderPath -> loadDirectory(folderPath) },
                    onNavigateUp = {
                        val parent = if (currentPath.contains('/')) {
                            currentPath.substringBeforeLast('/').ifEmpty { "/" }
                        } else "/"
                        loadDirectory(parent)
                    },
                    onPlayItem = { item ->
                        val track = SambaManager.createAudioTrack(item, connectedServer!!, context)
                        onPlayTrack(track)
                        onAddTracksToLibrary(listOf(track))
                        addLog("Reproduciendo por streaming SMB: ${item.name}", isSuccess = true)
                    },
                    onAddAllFolderToLibrary = {
                        val audioItems = shareItems.filter { it.isAudioFile }
                        val tracks = audioItems.map { SambaManager.createAudioTrack(it, connectedServer!!, context) }
                        onAddTracksToLibrary(tracks)
                        addLog("Se añadieron ${tracks.size} pistas a la Biblioteca.", isSuccess = true)
                    },
                    onDisconnect = {
                        SambaManager.disconnectAll()
                        connectedServer = null
                        showExplorer = false
                        shareItems = emptyList()
                        currentPath = "/"
                        addLog("Servidor Samba desconectado.")
                    }
                )
            } else {
                // Connection Confirmation & Direct Shortcuts (Requested by user)
                SmbConnectedSummaryView(
                    server = connectedServer!!,
                    items = shareItems,
                    theme = theme,
                    onNavigateToNowPlaying = onNavigateToNowPlaying,
                    onNavigateToLibrary = onNavigateToLibrary,
                    onOpenExplorer = { showExplorer = true },
                    onDisconnect = {
                        SambaManager.disconnectAll()
                        connectedServer = null
                        showExplorer = false
                        shareItems = emptyList()
                        currentPath = "/"
                        addLog("Servidor Samba desconectado.")
                    }
                )
            }
        } else {
            // VIEW A: SMB CONNECTION SETUP & NUMPAD (Native Android TV Widescreen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                SambaHeader()

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column: Output Display, Presets & Log Console
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TargetServerCard(
                            hostIp = hostIp,
                            isConnecting = isConnecting,
                            cursorAlpha = cursorAlpha,
                            onClear = {
                                hostIp = ""
                                addLog("Campo IP limpiado.")
                            },
                            onConnect = {
                                startConnection(
                                    hostIp = hostIp,
                                    shareName = shareName,
                                    isAnonymous = isAnonymous,
                                    username = username,
                                    password = password,
                                    context = context,
                                    coroutineScope = coroutineScope,
                                    onStart = { isConnecting = true },
                                    onSuccess = { server, items ->
                                        isConnecting = false
                                        connectedServer = server
                                        shareItems = items
                                        currentPath = "/"
                                    },
                                    onError = { isConnecting = false },
                                    onAddTracksToLibrary = onAddTracksToLibrary,
                                    addLog = ::addLog
                                )
                            }
                        )

                        PresetsCard(
                            presets = quickPresets,
                            onSelectPreset = { preset ->
                                hostIp = preset
                                addLog("Preset aplicado: $preset")
                            }
                        )

                        ShareAndAuthCard(
                            shareName = shareName,
                            onShareNameChange = { shareName = it },
                            isAnonymous = isAnonymous,
                            onAnonymousChange = { isAnonymous = it },
                            username = username,
                            onUsernameChange = { username = it },
                            password = password,
                            onPasswordChange = { password = it }
                        )

                        LogConsoleCard(logs = logs, modifier = Modifier.weight(1f))
                    }

                    // Right Column: Custom Numeric Keypad
                    Column(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        TvNumpad(
                            currentValue = hostIp,
                            onValueChange = { hostIp = it },
                            primaryColor = Color(0xFF3B82F6),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun startConnection(
    hostIp: String,
    shareName: String,
    isAnonymous: Boolean,
    username: String,
    password: String,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStart: () -> Unit,
    onSuccess: (SambaServer, List<SambaShareItem>) -> Unit,
    onError: () -> Unit,
    onAddTracksToLibrary: (List<AudioTrack>) -> Unit = {},
    addLog: (String, Boolean, Boolean) -> Unit
) {
    if (hostIp.isBlank() || hostIp.count { it == '.' } < 1) {
        addLog("Error: Por favor ingresa una dirección IP válida (ej: 192.168.1.100)", false, true)
        return
    }

    onStart()
    addLog("Iniciando conexión SMB a $hostIp:445...", false, false)
    addLog("Negociando protocolo SMBv2/SMBv3 con $hostIp...", false, false)

    coroutineScope.launch {
        val server = SambaServer(
            id = "srv_${System.currentTimeMillis()}",
            name = "Servidor $hostIp",
            hostIp = hostIp,
            shareName = shareName.ifBlank { "Musica" },
            username = username,
            password = password,
            isAnonymous = isAnonymous,
            isConnected = true
        )

        val result = SambaManager.browseDirectory(server, "/", context)
        result.onSuccess { items ->
            addLog("¡Handshake SMB exitoso! Conectado a //$hostIp/${server.shareName}", true, false)
            val audioItems = items.filter { it.isAudioFile }
            val tracks = audioItems.map { SambaManager.createAudioTrack(it, server, context) }
            onAddTracksToLibrary(tracks)
            addLog("Se indexaron ${tracks.size} pistas de red a la Biblioteca.", true, false)
            onSuccess(server, items)
        }.onFailure { err ->
            addLog("Fallo de conexión SMB: ${err.message}", false, true)
            onError()
        }
    }
}

@Composable
private fun SmbConnectedSummaryView(
    server: SambaServer,
    items: List<SambaShareItem>,
    theme: PlayerTheme,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onOpenExplorer: () -> Unit,
    onDisconnect: () -> Unit
) {
    val audioCount = items.count { it.isAudioFile }
    val folderCount = items.count { it.isDirectory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success Header Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF151C2C),
            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "¡Servidor Samba Conectado!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "smb://${server.hostIp}/${server.shareName}",
                                color = Color(0xFF34D399),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        onClick = onDisconnect,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF334155),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PowerOff, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(14.dp))
                            Text("Desconectar", color = Color(0xFFFB7185), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "La música compartida en este servidor se ha sincronizado e indexado automáticamente en la Biblioteca. Puedes reproducir directamente o regresar al reproductor.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                // Quick stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pistas de Red", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$audioCount canciones", color = Color(0xFF60A5FA), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Carpetas Encontradas", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$folderCount carpetas", color = Color(0xFFFBBF24), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Seguridad", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(if (server.isAnonymous) "Anónimo" else server.username, color = Color(0xFF34D399), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Direct Navigation Action Buttons
        Text("Acciones Rápidas:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TvFocusableCard(
                onClick = onNavigateToLibrary,
                shape = RoundedCornerShape(14.dp),
                focusedBorderColor = theme.primaryAccent,
                backgroundColor = Color(0xFF1D4ED8),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver en Biblioteca (Música Red)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            TvFocusableCard(
                onClick = onNavigateToNowPlaying,
                shape = RoundedCornerShape(14.dp),
                focusedBorderColor = theme.primaryAccent,
                backgroundColor = theme.primaryAccent,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF070A12), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver al Reproductor", color = Color(0xFF070A12), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Secondary Option: Drill down into files/folders
        TvFocusableCard(
            onClick = onOpenExplorer,
            shape = RoundedCornerShape(12.dp),
            focusedBorderColor = Color(0xFFFBBF24),
            backgroundColor = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explorar Carpetas y Archivos del Servidor", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SmbFileExplorer(
    server: SambaServer,
    currentPath: String,
    items: List<SambaShareItem>,
    isLoading: Boolean,
    currentTrackId: String?,
    isPlaying: Boolean,
    theme: PlayerTheme,
    onBackToSummary: () -> Unit,
    onNavigateFolder: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onPlayItem: (SambaShareItem) -> Unit,
    onAddAllFolderToLibrary: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isRoot = currentPath == "/" || currentPath.isEmpty()
    val audioItems = items.filter { it.isAudioFile }
    val folderItems = items.filter { it.isDirectory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Server Breadcrumb & Controls Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF151C2C),
            border = BorderStroke(1.dp, Color(0xFF232D42)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Text(
                            text = "smb://${server.hostIp}/${server.shareName}",
                            color = Color(0xFF34D399),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onBackToSummary,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(13.dp))
                                Text("Resumen", color = Color(0xFF93C5FD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Disconnect Button
                        Surface(
                            onClick = onDisconnect,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF334155),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PowerOff, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(13.dp))
                                Text("Desconectar", color = Color(0xFFFB7185), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Path Breadcrumb & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!isRoot) {
                            TvFocusableCard(
                                onClick = onNavigateUp,
                                shape = RoundedCornerShape(8.dp),
                                focusedBorderColor = theme.primaryAccent,
                                backgroundColor = Color(0xFF1E293B),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Subir nivel", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                        Text(
                            text = currentPath,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (audioItems.isNotEmpty()) {
                        TvFocusableCard(
                            onClick = onAddAllFolderToLibrary,
                            shape = RoundedCornerShape(8.dp),
                            focusedBorderColor = theme.primaryAccent,
                            backgroundColor = Color(0xFF1E3A8A),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(14.dp))
                                Text("Añadir Todo (${audioItems.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = theme.primaryAccent, modifier = Modifier.size(36.dp))
                    Text("Cargando compartimento Samba...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                    Text("No se encontraron carpetas ni archivos de audio en esta ruta.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        } else {
            // List of Folders and Audio Tracks
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Folder items first
                items(folderItems) { folder ->
                    TvFocusableCard(
                        onClick = { onNavigateFolder(folder.path) },
                        testTag = "smb_folder_${folder.name}",
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = Color(0xFFFBBF24),
                        backgroundColor = Color(0xFF151C2C),
                        focusedBackgroundColor = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFBBF24).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                }

                                Column {
                                    Text(
                                        text = folder.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Carpeta de Música",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Audio File items
                items(audioItems) { audioFile ->
                    val isCurrentlyPlayingThis = currentTrackId != null && currentTrackId.contains(audioFile.path.hashCode().toString())

                    TvFocusableCard(
                        onClick = { onPlayItem(audioFile) },
                        testTag = "smb_audio_${audioFile.name}",
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = if (isCurrentlyPlayingThis) theme.primaryAccent.copy(alpha = 0.18f) else Color(0xFF111726),
                        focusedBackgroundColor = Color(0xFF1B2438),
                        borderWidth = if (isCurrentlyPlayingThis) 1.5.dp else 1.dp,
                        unfocusedBorderColor = if (isCurrentlyPlayingThis) theme.primaryAccent else Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(audioFile.format?.badgeColorHex ?: 0xFF3B82F6).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrentlyPlayingThis && isPlaying) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.primaryAccent, modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = audioFile.name,
                                        color = if (isCurrentlyPlayingThis) theme.primaryAccent else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrentlyPlayingThis) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(audioFile.format?.badgeColorHex ?: 0xFF3B82F6).copy(alpha = 0.8f)
                                        ) {
                                            Text(
                                                text = audioFile.format?.extension?.uppercase() ?: "AUDIO",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(
                                            text = audioFile.formattedSize(),
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrentlyPlayingThis) theme.primaryAccent else Color(0xFF1E293B),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = if (isCurrentlyPlayingThis) "Sonando" else "Reproducir",
                                        color = if (isCurrentlyPlayingThis) Color(0xFF070A10) else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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
private fun SambaHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "Configuración de Red Samba (SMB)",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ingreso de IP optimizado para Control Remoto y D-Pad",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34D399))
                )
                Text(
                    text = "Android TV & Google TV",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TargetServerCard(
    hostIp: String,
    isConnecting: Boolean,
    cursorAlpha: Float,
    onClear: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF151C2C),
        border = BorderStroke(1.dp, Color(0xFF232D42)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "SERVIDOR SAMBA DESTINO",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "${hostIp.length} / 15 chars",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LCD Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070A12))
                    .border(2.dp, Color(0xFF232D42), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (hostIp.isEmpty()) "0.0.0.0" else hostIp,
                        color = if (hostIp.isEmpty()) Color(0xFF475569) else Color(0xFF34D399),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(width = 9.dp, height = 20.dp)
                            .alpha(cursorAlpha)
                            .background(Color(0xFF34D399))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TvActionButton(
                    label = "Borrar Todo",
                    icon = Icons.Default.Delete,
                    textColor = Color(0xFFFB7185),
                    iconColor = Color(0xFFFB7185),
                    backgroundColor = Color(0xFF1E293B),
                    focusedBackgroundColor = Color(0xFF9F1239),
                    focusedBorderColor = Color(0xFFFDA4AF),
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                )

                TvActionButton(
                    label = if (isConnecting) "Conectando..." else "Conectar",
                    icon = if (isConnecting) Icons.Default.Bolt else Icons.Default.Power,
                    textColor = Color.White,
                    iconColor = Color.White,
                    backgroundColor = Color(0xFF2563EB),
                    focusedBackgroundColor = Color(0xFF1D4ED8),
                    focusedBorderColor = Color(0xFF93C5FD),
                    isPrimary = true,
                    onClick = onConnect,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetsCard(
    presets: List<String>,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF151C2C).copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color(0xFF232D42)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "PRESETS RÁPIDOS DE IP",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(2).forEach { preset ->
                    TvPresetButton(
                        ip = preset,
                        onClick = { onSelectPreset(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(2).forEach { preset ->
                    TvPresetButton(
                        ip = preset,
                        onClick = { onSelectPreset(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareAndAuthCard(
    shareName: String,
    onShareNameChange: (String) -> Unit,
    isAnonymous: Boolean,
    onAnonymousChange: (Boolean) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151C2C),
        border = BorderStroke(1.dp, Color(0xFF232D42)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = shareName,
                    onValueChange = onShareNameChange,
                    label = { Text("Recurso / Carpeta", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFCBD5E1),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF232D42)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Acceso Anónimo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Sin contraseña", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = onAnonymousChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6)
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !isAnonymous) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Usuario", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFCBD5E1),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFCBD5E1),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogConsoleCard(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        val logListState = rememberLazyListState()

        LaunchedEffect(logs.size) {
            if (logs.isNotEmpty()) {
                logListState.animateScrollToItem(logs.size - 1)
            }
        }

        LazyColumn(
            state = logListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { entry ->
                val textColor = when {
                    entry.isSuccess -> Color(0xFF34D399)
                    entry.isError -> Color(0xFFFB7185)
                    else -> Color(0xFF93C5FD)
                }
                Text(
                    text = "[${entry.timestamp}] ${entry.message}",
                    color = textColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TvActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color,
    iconColor: Color,
    backgroundColor: Color,
    focusedBackgroundColor: Color,
    focusedBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .scale(if (isFocused) 1.06f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_action_$label"),
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) focusedBackgroundColor else backgroundColor,
        border = if (isFocused) BorderStroke(2.dp, focusedBorderColor) else BorderStroke(1.dp, Color(0xFF334155)),
        shadowElevation = if (isFocused) 10.dp else 2.dp,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isFocused) Color.White else textColor,
                fontSize = 13.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TvPresetButton(
    ip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .scale(if (isFocused) 1.06f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_preset_$ip"),
        shape = RoundedCornerShape(10.dp),
        color = if (isFocused) Color(0xFF2563EB) else Color(0xFF1E293B).copy(alpha = 0.9f),
        border = if (isFocused) BorderStroke(2.dp, Color(0xFF93C5FD)) else BorderStroke(1.dp, Color(0xFF334155)),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ip,
                color = if (isFocused) Color.White else Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isFocused) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
