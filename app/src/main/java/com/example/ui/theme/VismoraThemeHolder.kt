package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class VismoraThemeColors(
    val background: Color,
    val primary: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

object VismoraThemeHolder {
    val Netflix = VismoraThemeColors(
        background = Color(0xFF141414),
        primary = Color(0xFFE50914),
        surface = Color(0xFF1F1F1F),
        text = Color.White,
        textSecondary = Color(0xFFB3B3B3),
        accent = Color(0xFFE50914),
        cardBorder = Color(0xFF333333),
        isDark = true
    )

    val Developer = VismoraThemeColors(
        background = Color(0xFF0B0F19),
        primary = Color(0xFF007ACC),
        surface = Color(0xFF151D2A),
        text = Color(0xFFD4D4D4),
        textSecondary = Color(0xFF858585),
        accent = Color(0xFF00FFCC),
        cardBorder = Color(0xFF1E293B),
        isDark = true
    )

    val Light = VismoraThemeColors(
        background = Color(0xFFF3F4F6),
        primary = Color(0xFF2563EB),
        surface = Color.White,
        text = Color(0xFF1F2937),
        textSecondary = Color(0xFF6B7280),
        accent = Color(0xFF4F46E5),
        cardBorder = Color(0xFFE5E7EB),
        isDark = false
    )

    val Cyberpunk = VismoraThemeColors(
        background = Color(0xFF0F001A),
        primary = Color(0xFFEA00D9),
        surface = Color(0xFF23003B),
        text = Color(0xFF00FFFF),
        textSecondary = Color(0xFFFF007F),
        accent = Color(0xFF39FF14),
        cardBorder = Color(0xFFFF007F),
        isDark = true
    )

    val Galaxy = VismoraThemeColors(
        background = Color(0xFF010214),
        primary = Color(0xFF6366F1),
        surface = Color(0xFF0A0F30),
        text = Color(0xFFE0E7FF),
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFFF43F5E),
        cardBorder = Color(0xFF1E1E50),
        isDark = true
    )

    val Nature = VismoraThemeColors(
        background = Color(0xFF06140A),
        primary = Color(0xFF10B981),
        surface = Color(0xFF0E2414),
        text = Color(0xFFECFDF5),
        textSecondary = Color(0xFF6EE7B7),
        accent = Color(0xFFFBBF24),
        cardBorder = Color(0xFF105B28),
        isDark = true
    )

    fun getColors(themeId: String): VismoraThemeColors {
        return when (themeId) {
            "netflix" -> Netflix
            "developer" -> Developer
            "light" -> Light
            "cyberpunk" -> Cyberpunk
            "galaxy" -> Galaxy
            "nature" -> Nature
            else -> Netflix
        }
    }
}
