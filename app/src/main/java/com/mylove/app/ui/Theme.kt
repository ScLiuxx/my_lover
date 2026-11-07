package com.mylove.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 全局配色。暖色底 + 玻璃面板，暗色模式降低饱和度。
 */
@Immutable
data class LoveColors(
    val isDark: Boolean,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val blobRose: Color,
    val blobPeach: Color,
    val blobLilac: Color,
    val blobMint: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val heart: Color,
    val glassSurface: Color,
    val stroke: Color
)

val LightLoveColors = LoveColors(
    isDark = false,
    bgTop = Color(0xFFFFF8F4),
    bgMid = Color(0xFFFFEDE7),
    bgBottom = Color(0xFFF9EFF6),
    blobRose = Color(0xFFFFC2CE),
    blobPeach = Color(0xFFFFD5BC),
    blobLilac = Color(0xFFE2D4FF),
    blobMint = Color(0xFFCFF3E6),
    text = Color(0xFF2E2422),
    textSecondary = Color(0xFF7C6B66),
    textTertiary = Color(0xFFA89690),
    accent = Color(0xFFE4695B),
    accentSoft = Color(0xFFFFE1DB),
    heart = Color(0xFFF2568A),
    glassSurface = Color(0x8CFFFFFF),
    stroke = Color(0x66FFFFFF)
)

val DarkLoveColors = LoveColors(
    isDark = true,
    bgTop = Color(0xFF1A1312),
    bgMid = Color(0xFF241A19),
    bgBottom = Color(0xFF1B1620),
    blobRose = Color(0xFF6E2E3E),
    blobPeach = Color(0xFF6B4028),
    blobLilac = Color(0xFF3E3468),
    blobMint = Color(0xFF24574A),
    text = Color(0xFFF7EFEB),
    textSecondary = Color(0xFFBCABA5),
    textTertiary = Color(0xFF8B7A75),
    accent = Color(0xFFFF8B79),
    accentSoft = Color(0xFF4B2B26),
    heart = Color(0xFFFF639B),
    glassSurface = Color(0x73261E1D),
    stroke = Color(0x1FFFFFFF)
)

val LocalLoveColors = staticCompositionLocalOf { LightLoveColors }

@Composable
fun MyLoveTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLoveColors provides if (darkTheme) DarkLoveColors else LightLoveColors,
        content = content
    )
}