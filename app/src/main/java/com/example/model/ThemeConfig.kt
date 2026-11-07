package com.example.model

import androidx.compose.ui.graphics.Color

enum class PlayerTheme(
    val title: String,
    val subtitle: String,
    val backgroundDark: Color,
    val surfaceDark: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val ambientGlow: Color
) {
    OLED_NEON(
        title = "OLED Cyber Studio",
        subtitle = "Negro profundo con acentos cian y violeta",
        backgroundDark = Color(0xFF07090E),
        surfaceDark = Color(0xFF111726),
        primaryAccent = Color(0xFF00E5FF),
        secondaryAccent = Color(0xFFA855F7),
        ambientGlow = Color(0x3300E5FF)
    ),
    WARM_AMBER(
        title = "Warm Amber & Vinyl",
        subtitle = "Tonos cálidos, nogal y estilo analógico clásico",
        backgroundDark = Color(0xFF140E0A),
        surfaceDark = Color(0xFF221610),
        primaryAccent = Color(0xFFF59E0B),
        secondaryAccent = Color(0xFFFB923C),
        ambientGlow = Color(0x33F59E0B)
    ),
    EMERALD_HIFI(
        title = "Emerald Master Hi-Fi",
        subtitle = "Verde esmeralda de precisión sobre grafito",
        backgroundDark = Color(0xFF08120E),
        surfaceDark = Color(0xFF102019),
        primaryAccent = Color(0xFF10B981),
        secondaryAccent = Color(0xFF34D399),
        ambientGlow = Color(0x3310B981)
    ),
    MIDNIGHT_INDIGO(
        title = "Midnight Indigo",
        subtitle = "Azul índigo moderno y elegante para sala de estar",
        backgroundDark = Color(0xFF0A0E1A),
        surfaceDark = Color(0xFF151C33),
        primaryAccent = Color(0xFF6366F1),
        secondaryAccent = Color(0xFF38BDF8),
        ambientGlow = Color(0x336366F1)
    )
}

enum class VisualizerStyle(val displayName: String) {
    WAVE_BARS("Barras de Onda Espectral"),
    PULSE_GLOW("Pulso Esférico"),
    MINIMAL_DOTS("Puntos de Frecuencia")
}
