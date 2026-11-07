package com.example.ui.components

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
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

/**
 * Handles appending a key to an IPv4 address according to the rules:
 * - Octet 1: Auto-adds '.' after 3 digits (e.g. 192 -> 192.)
 * - Octet 2: Auto-adds '.' after 3 digits (e.g. 168 -> 168.)
 * - Octet 3: DOES NOT auto-add '.' (user can type 1, 10, 100, and presses '.' manually)
 * - Octet 4: Max 3 digits, no more dots allowed.
 */
fun appendKeyWithAutoDot(current: String, key: String): String {
    if (key == ".") {
        val dotCount = current.count { it == '.' }
        if (dotCount < 3 && current.isNotEmpty() && !current.endsWith('.')) {
            return "$current."
        }
        return current
    }

    // It's a digit (0-9)
    val segments = current.split('.')
    val currentSegmentIndex = segments.size - 1
    val currentSegment = segments[currentSegmentIndex]

    // Maximum 4 octets in IPv4
    if (segments.size > 4) return current
    if (segments.size == 4 && currentSegment.length >= 3) return current

    // Maximum 3 digits per octet
    if (currentSegment.length >= 3) return current

    val newSegment = currentSegment + key
    val prefix = if (currentSegmentIndex == 0) "" else current.substringBeforeLast('.') + "."

    // Auto-dot rule: only for octet 1 (index 0) and octet 2 (index 1)
    return if ((currentSegmentIndex == 0 || currentSegmentIndex == 1) && newSegment.length == 3) {
        "$prefix$newSegment."
    } else {
        "$prefix$newSegment"
    }
}

fun deleteLastKeyFromIp(current: String): String {
    if (current.isEmpty()) return ""
    return current.dropLast(1)
}

@Composable
fun TvNumpad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    // Custom inverted keypad layout requested by the user:
    // 2 and 4 swapped with 6 and 8, 0 in center, 9 directly under 3
    // ROW 1: [6] [1] [8]
    // ROW 2: [7] [0] [5]
    // ROW 3: [2] [3] [4]
    // ROW 4: [ . ] [9] [ ⌫ ]
    val keypadRows = listOf(
        listOf("6", "1", "8"),
        listOf("7", "0", "5"),
        listOf("2", "3", "4"),
        listOf(".", "9", "⌫")
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF151C2C)) // tvCard from HTML design
            .border(1.dp, Color(0xFF232D42), RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card Header matching HTML
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
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "TECLADO NUMÉRICO PERSONALIZADO",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Text(
                    text = "Matriz 3x3 + 0 Central",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            keypadRows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isZeroKey = key == "0"
                        val isDotKey = key == "."
                        val isBackspaceKey = key == "⌫"

                        TvCustomKeyButton(
                            label = key,
                            isZero = isZeroKey,
                            isDot = isDotKey,
                            isBackspace = isBackspaceKey,
                            onClick = {
                                if (isBackspaceKey) {
                                    onValueChange(deleteLastKeyFromIp(currentValue))
                                } else {
                                    onValueChange(appendKeyWithAutoDot(currentValue, key))
                                }
                            },
                            primaryColor = primaryColor,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info footer chip matching HTML
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.6f),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Disposición personalizada activa (0 al centro, 9 inferior central)",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TvCustomKeyButton(
    label: String,
    isZero: Boolean,
    isDot: Boolean,
    isBackspace: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    // Colors according to the HTML design:
    // Zero key: bg-blue-900/40 text-blue-200 border-blue-500/40
    // Dot key: text-blue-400 font-extrabold text-3xl
    // Backspace: bg-rose-950/40 text-rose-400 font-bold border-rose-800/40
    // Default: bg-slate-800/90 text-white font-bold border-slate-700/60
    val defaultBg = when {
        isZero -> Color(0xFF1E3A8A).copy(alpha = 0.4f)
        isBackspace -> Color(0xFF4C0519).copy(alpha = 0.4f)
        else -> Color(0xFF1E293B).copy(alpha = 0.9f)
    }

    val defaultBorder = when {
        isZero -> Color(0xFF3B82F6).copy(alpha = 0.4f)
        isBackspace -> Color(0xFF9F1239).copy(alpha = 0.4f)
        else -> Color(0xFF334155).copy(alpha = 0.6f)
    }

    val defaultTextColor = when {
        isZero -> Color(0xFFBFDBFE)
        isDot -> Color(0xFF60A5FA)
        isBackspace -> Color(0xFFFB7185)
        else -> Color.White
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(if (isFocused) 1.08f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_keypad_$label"),
        shape = RoundedCornerShape(16.dp),
        color = if (isFocused) Color(0xFF2563EB) else defaultBg,
        border = if (isFocused) BorderStroke(2.5.dp, Color(0xFF93C5FD)) else BorderStroke(1.dp, defaultBorder),
        shadowElevation = if (isFocused) 14.dp else 2.dp,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isBackspace) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Borrar",
                    tint = if (isFocused) Color.White else defaultTextColor,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = label,
                    color = if (isFocused) Color.White else defaultTextColor,
                    fontSize = if (isDot) 28.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
