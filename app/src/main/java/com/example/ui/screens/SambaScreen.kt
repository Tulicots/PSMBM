package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerTheme
import com.example.model.SambaServer
import com.example.ui.components.TvFocusableCard
import com.example.ui.components.TvNumpad

@Composable
fun SambaScreen(
    theme: PlayerTheme,
    onConnectServer: (SambaServer) -> Unit,
    modifier: Modifier = Modifier
) {
    var hostIp by remember { mutableStateOf("192.168.1.50") }
    var shareName by remember { mutableStateOf("Musica") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(true) }
    var connectionStatus by remember { mutableStateOf<String?>(null) }

    // Pre-saved demo servers for easy TV testing
    var savedServers by remember {
        mutableStateOf(
            listOf(
                SambaServer(
                    id = "srv_1",
                    name = "NAS Servidor Hogar",
                    hostIp = "192.168.1.120",
                    shareName = "HiRes_Music",
                    isAnonymous = true,
                    isConnected = true,
                    trackCount = 1240
                ),
                SambaServer(
                    id = "srv_2",
                    name = "PC Biblioteca Sala",
                    hostIp = "192.168.1.200",
                    shareName = "Discos_FLAC",
                    isAnonymous = false,
                    username = "usuario",
                    isConnected = false,
                    trackCount = 480
                )
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Left Column: Quick IP Numpad & TV Connection Setup
        Column(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Configuración Samba (SMB / Windows Share)",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Teclado numérico optimizado para control remoto: ingresa la IP sin abrir el teclado completo",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            // TV Remote Numeric Keypad for IP
            TvNumpad(
                currentValue = hostIp,
                onValueChange = { hostIp = it },
                primaryColor = theme.primaryAccent,
                modifier = Modifier.fillMaxWidth()
            )

            // Share & Auth Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Carpetas compartidas
                OutlinedTextField(
                    value = shareName,
                    onValueChange = { shareName = it },
                    label = { Text("Carpeta / Recurso (Share)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFCBD5E1),
                        focusedBorderColor = theme.primaryAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF131B2E),
                        unfocusedContainerColor = Color(0xFF131B2E)
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Anonymous switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131B2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
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
                                text = "Sin usuario/clave",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = theme.primaryAccent
                            )
                        )
                    }
                }
            }

            // Connect & Test Button
            TvFocusableCard(
                onClick = {
                    connectionStatus = "Conectando con SMB a $hostIp/$shareName..."
                    val newServer = SambaServer(
                        id = "srv_${System.currentTimeMillis()}",
                        name = "Servidor $hostIp",
                        hostIp = hostIp,
                        shareName = shareName,
                        username = username,
                        isAnonymous = isAnonymous,
                        isConnected = true,
                        trackCount = (200..1500).random()
                    )
                    savedServers = listOf(newServer) + savedServers.filter { it.hostIp != hostIp }
                    onConnectServer(newServer)
                    connectionStatus = "¡Conexión establecida con éxito con $hostIp! Escaneando carpetas de música..."
                },
                shape = RoundedCornerShape(14.dp),
                focusedBorderColor = Color.White,
                backgroundColor = theme.primaryAccent,
                focusedBackgroundColor = theme.primaryAccent.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Conectar a Servidor Samba",
                        color = Color(0xFF070A12),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (connectionStatus != null) {
                Text(
                    text = connectionStatus ?: "",
                    color = theme.primaryAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right Column: Saved & Discovered Samba Servers
        Column(
            modifier = Modifier
                .weight(0.85f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.surfaceDark.copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Servidores Guardados",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "${savedServers.size} redes",
                        color = theme.primaryAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedServers) { server ->
                    TvFocusableCard(
                        onClick = {
                            hostIp = server.hostIp
                            shareName = server.shareName
                            isAnonymous = server.isAnonymous
                            connectionStatus = "Servidor ${server.name} seleccionado"
                        },
                        focusedBorderColor = theme.primaryAccent,
                        backgroundColor = Color(0xFF131B2E),
                        focusedBackgroundColor = Color(0xFF1E2B45),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.primaryAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = theme.primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = server.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "smb://${server.hostIp}/${server.shareName}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${server.trackCount} pistas • ${if (server.isAnonymous) "Público" else "Autenticado"}",
                                    color = theme.secondaryAccent,
                                    fontSize = 10.sp
                                )
                            }

                            if (server.isConnected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Conectado",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
