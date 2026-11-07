package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Router
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerTheme
import com.example.model.SambaServer
import com.example.ui.components.TvNumpad
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
    onConnectServer: (SambaServer) -> Unit,
    modifier: Modifier = Modifier
) {
    var hostIp by remember { mutableStateOf("192.168.1.") }
    var shareName by remember { mutableStateOf("Musica") }
    var isAnonymous by remember { mutableStateOf(true) }

    val logs = remember {
        mutableStateListOf(
            LogEntry("13:00:00", "[Samba Service] Listo para recibir dirección de red...", isSuccess = false),
            LogEntry("13:00:01", "Protocolo SMBv2/SMBv3 habilitado en puerto 445.", isSuccess = true)
        )
    }

    val quickPresets = listOf(
        "192.168.1.1",
        "192.168.1.100",
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isWidescreen = isLandscape && maxWidth >= 760.dp

        if (isWidescreen) {
            // Widescreen TV Layout (Side-by-side)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                // Header
                SambaHeader(isTvMode = true)

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
                            cursorAlpha = cursorAlpha,
                            onClear = {
                                hostIp = ""
                                addLog("Campo IP limpiado.")
                            },
                            onConnect = {
                                handleConnect(hostIp, shareName, isAnonymous, ::addLog, onConnectServer)
                            }
                        )

                        PresetsCard(
                            presets = quickPresets,
                            onSelectPreset = { preset ->
                                hostIp = preset
                                addLog("Preset aplicado: $preset")
                            }
                        )

                        ShareAndAuthRow(
                            shareName = shareName,
                            onShareNameChange = { shareName = it },
                            isAnonymous = isAnonymous,
                            onAnonymousChange = { isAnonymous = it }
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
        } else {
            // Adaptive Mobile Layout (Single Column Scrollable, perfect for phones!)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                SambaHeader(isTvMode = false)

                // Target Server Card (LCD screen + action buttons)
                TargetServerCard(
                    hostIp = hostIp,
                    cursorAlpha = cursorAlpha,
                    onClear = {
                        hostIp = ""
                        addLog("Campo IP limpiado.")
                    },
                    onConnect = {
                        handleConnect(hostIp, shareName, isAnonymous, ::addLog, onConnectServer)
                    }
                )

                // Custom Keypad Card (Full width on mobile, comfortable touch targets)
                TvNumpad(
                    currentValue = hostIp,
                    onValueChange = { hostIp = it },
                    primaryColor = Color(0xFF3B82F6),
                    modifier = Modifier.fillMaxWidth()
                )

                // Presets Section
                PresetsCard(
                    presets = quickPresets,
                    onSelectPreset = { preset ->
                        hostIp = preset
                        addLog("Preset aplicado: $preset")
                    }
                )

                // Share & Auth
                ShareAndAuthRow(
                    shareName = shareName,
                    onShareNameChange = { shareName = it },
                    isAnonymous = isAnonymous,
                    onAnonymousChange = { isAnonymous = it }
                )

                // Log Console
                LogConsoleCard(
                    logs = logs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun handleConnect(
    hostIp: String,
    shareName: String,
    isAnonymous: Boolean,
    addLog: (String, Boolean, Boolean) -> Unit,
    onConnectServer: (SambaServer) -> Unit
) {
    if (hostIp.isBlank() || hostIp.count { it == '.' } < 1) {
        addLog("Error: Por favor ingresa una dirección IP válida.", false, true)
        return
    }
    addLog("Iniciando handshake Samba (smb://$hostIp/$shareName)...", false, false)
    val server = SambaServer(
        id = "srv_${System.currentTimeMillis()}",
        name = "Servidor $hostIp",
        hostIp = hostIp,
        shareName = shareName,
        isAnonymous = isAnonymous,
        isConnected = true,
        trackCount = 1240
    )
    onConnectServer(server)
    addLog("¡Conexión exitosa a $hostIp:445! Montando compartimentos...", true, false)
}

@Composable
private fun SambaHeader(isTvMode: Boolean) {
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
                    text = "Ingreso de IP optimizado para Control Remoto y Móvil",
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
                    text = if (isTvMode) "Android TV Mode" else "Modo Móvil",
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
                    label = "Conectar",
                    icon = Icons.Default.Power,
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
private fun ShareAndAuthRow(
    shareName: String,
    onShareNameChange: (String) -> Unit,
    isAnonymous: Boolean,
    onAnonymousChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                focusedContainerColor = Color(0xFF151C2C),
                unfocusedContainerColor = Color(0xFF151C2C)
            ),
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF151C2C),
            border = BorderStroke(1.dp, Color(0xFF232D42)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Acceso Anónimo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Sin contraseña",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
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
