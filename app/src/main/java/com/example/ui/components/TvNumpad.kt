package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TvNumpad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val quickPrefixes = listOf("192.168.1.", "192.168.0.", "10.0.0.", "172.16.0.")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F1523))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current IP Preview Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070A12))
                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentValue.isEmpty()) "0.0.0.0" else currentValue,
                    color = if (currentValue.isEmpty()) Color.Gray else Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Dirección IP (SMB)",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick prefix buttons for common home networks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrefixes.forEach { prefix ->
                TvKeyButton(
                    label = prefix,
                    onClick = { onValueChange(prefix) },
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    height = 36.dp,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3x4 Numpad Grid
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "⌫")
        )

        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKeys.forEach { key ->
                    TvKeyButton(
                        label = key,
                        onClick = {
                            when (key) {
                                "⌫" -> {
                                    if (currentValue.isNotEmpty()) {
                                        onValueChange(currentValue.dropLast(1))
                                    }
                                }
                                else -> {
                                    if (currentValue.length < 15) {
                                        onValueChange(currentValue + key)
                                    }
                                }
                            }
                        },
                        primaryColor = primaryColor,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        height = 46.dp,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TvKeyButton(
                label = "Borrar Todo",
                onClick = { onValueChange("") },
                primaryColor = Color(0xFFEF4444),
                modifier = Modifier.width(120.dp),
                height = 34.dp,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TvKeyButton(
    label: String,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 44.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .scale(if (isFocused) 1.08f else 1.0f)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .testTag("tv_numpad_key_$label"),
        shape = RoundedCornerShape(10.dp),
        color = if (isFocused) primaryColor.copy(alpha = 0.25f) else Color(0xFF1B2438),
        border = if (isFocused) androidx.compose.foundation.BorderStroke(2.dp, primaryColor)
        else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334D)),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isFocused) Color.White else Color(0xFFE2E8F0),
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
