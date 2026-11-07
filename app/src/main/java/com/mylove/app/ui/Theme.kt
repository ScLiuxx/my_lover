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

@Immutable
data class LovePalette(
    val id: String,
    val name: String,
    val light: LoveColors,
    val dark: LoveColors
)

private fun colors(
    dark: Boolean,
    bgTop: Long,
    bgMid: Long,
    bgBottom: Long,
    blob1: Long,
    blob2: Long,
    blob3: Long,
    blob4: Long,
    text: Long,
    textSecondary: Long,
    textTertiary: Long,
    accent: Long,
    accentSoft: Long,
    heart: Long
) = LoveColors(
    isDark = dark,
    bgTop = Color(bgTop),
    bgMid = Color(bgMid),
    bgBottom = Color(bgBottom),
    blobRose = Color(blob1),
    blobPeach = Color(blob2),
    blobLilac = Color(blob3),
    blobMint = Color(blob4),
    text = Color(text),
    textSecondary = Color(textSecondary),
    textTertiary = Color(textTertiary),
    accent = Color(accent),
    accentSoft = Color(accentSoft),
    heart = Color(heart),
    glassSurface = Color(if (dark) 0x7320191A else 0x8CFFFFFF),
    stroke = Color(if (dark) 0x1FFFFFFF else 0x66FFFFFF)
)

val LightLoveColors = colors(
    dark = false,
    bgTop = 0xFFFFF8F4, bgMid = 0xFFFFEDE7, bgBottom = 0xFFF9EFF6,
    blob1 = 0xFFFFC2CE, blob2 = 0xFFFFD5BC, blob3 = 0xFFE2D4FF, blob4 = 0xFFCFF3E6,
    text = 0xFF2E2422, textSecondary = 0xFF7C6B66, textTertiary = 0xFFA89690,
    accent = 0xFFE4695B, accentSoft = 0xFFFFE1DB, heart = 0xFFF2568A
)

val DarkLoveColors = colors(
    dark = true,
    bgTop = 0xFF1A1312, bgMid = 0xFF241A19, bgBottom = 0xFF1B1620,
    blob1 = 0xFF6E2E3E, blob2 = 0xFF6B4028, blob3 = 0xFF3E3468, blob4 = 0xFF24574A,
    text = 0xFFF7EFEB, textSecondary = 0xFFBCABA5, textTertiary = 0xFF8B7A75,
    accent = 0xFFFF8B79, accentSoft = 0xFF4B2B26, heart = 0xFFFF639B
)

val LovePalettes = listOf(
    LovePalette("warm", "暖阳", LightLoveColors, DarkLoveColors),
    LovePalette(
        id = "sakura",
        name = "樱花",
        light = colors(
            dark = false,
            bgTop = 0xFFFFF7FA, bgMid = 0xFFFFE9F2, bgBottom = 0xFFF7ECFF,
            blob1 = 0xFFFFB8D1, blob2 = 0xFFFFD3E3, blob3 = 0xFFE3D4FF, blob4 = 0xFFCFF0F7,
            text = 0xFF33232A, textSecondary = 0xFF836C75, textTertiary = 0xFFB09AA3,
            accent = 0xFFD95C86, accentSoft = 0xFFFFE0EC, heart = 0xFFF0568F
        ),
        dark = colors(
            dark = true,
            bgTop = 0xFF1C1216, bgMid = 0xFF251820, bgBottom = 0xFF1D1522,
            blob1 = 0xFF6E2C43, blob2 = 0xFF6A3448, blob3 = 0xFF40305E, blob4 = 0xFF23505A,
            text = 0xFFFBEFF4, textSecondary = 0xFFC4AEB6, textTertiary = 0xFF95808A,
            accent = 0xFFFF8FB4, accentSoft = 0xFF4C2634, heart = 0xFFFF6C9F
        )
    ),
    LovePalette(
        id = "mint",
        name = "薄荷",
        light = colors(
            dark = false,
            bgTop = 0xFFF5FCF9, bgMid = 0xFFE7F8F1, bgBottom = 0xFFEFFAFF,
            blob1 = 0xFFA9EBD2, blob2 = 0xFFBBE9F7, blob3 = 0xFFD9E6FF, blob4 = 0xFFFFD9E4,
            text = 0xFF20302B, textSecondary = 0xFF647A73, textTertiary = 0xFF8FA39C,
            accent = 0xFF2E9E76, accentSoft = 0xFFDCF5EA, heart = 0xFFEF6F98
        ),
        dark = colors(
            dark = true,
            bgTop = 0xFF0F1916, bgMid = 0xFF152320, bgBottom = 0xFF141C23,
            blob1 = 0xFF1F5C4A, blob2 = 0xFF215866, blob3 = 0xFF2C4270, blob4 = 0xFF6A2F45,
            text = 0xFFECF7F2, textSecondary = 0xFFA7BEB6, textTertiary = 0xFF7D938C,
            accent = 0xFF5CD3A5, accentSoft = 0xFF1C3B31, heart = 0xFFFF7FA6
        )
    ),
    LovePalette(
        id = "ocean",
        name = "海岸",
        light = colors(
            dark = false,
            bgTop = 0xFFF4FAFF, bgMid = 0xFFE7F2FC, bgBottom = 0xFFF0F8FF,
            blob1 = 0xFFA9D8FF, blob2 = 0xFFB9EDF7, blob3 = 0xFFCFE5FF, blob4 = 0xFFFFD7DC,
            text = 0xFF1E2A35, textSecondary = 0xFF637585, textTertiary = 0xFF8C9BA8,
            accent = 0xFF2C7BC4, accentSoft = 0xFFDDEBFA, heart = 0xFFEE6A93
        ),
        dark = colors(
            dark = true,
            bgTop = 0xFF0E1720, bgMid = 0xFF13202B, bgBottom = 0xFF111A23,
            blob1 = 0xFF1F476B, blob2 = 0xFF1E5560, blob3 = 0xFF2A3D6B, blob4 = 0xFF5C2B3E,
            text = 0xFFEDF5FB, textSecondary = 0xFFA6B8C6, textTertiary = 0xFF7D8E9B,
            accent = 0xFF6DB6FF, accentSoft = 0xFF1B3145, heart = 0xFFFF7FA6
        )
    ),
    LovePalette(
        id = "dusk",
        name = "暮色",
        light = colors(
            dark = false,
            bgTop = 0xFFF8F4FF, bgMid = 0xFFEEE8FF, bgBottom = 0xFFF7F0FF,
            blob1 = 0xFFD9C4FF, blob2 = 0xFFC9D4FF, blob3 = 0xFFFFC9E0, blob4 = 0xFFCFF0F7,
            text = 0xFF2A2333, textSecondary = 0xFF766C85, textTertiary = 0xFFA094B0,
            accent = 0xFF7A5BD6, accentSoft = 0xFFE8E0FF, heart = 0xFFE85A9A
        ),
        dark = colors(
            dark = true,
            bgTop = 0xFF17121F, bgMid = 0xFF1E1729, bgBottom = 0xFF191423,
            blob1 = 0xFF3F2C6B, blob2 = 0xFF333C6B, blob3 = 0xFF5E2B4C, blob4 = 0xFF24505A,
            text = 0xFFF2EDFA, textSecondary = 0xFFB7ABC5, textTertiary = 0xFF8B7F9A,
            accent = 0xFFA98CFF, accentSoft = 0xFF2C2445, heart = 0xFFF477AC
        )
    )
)

fun paletteById(id: String): LovePalette =
    LovePalettes.firstOrNull { it.id == id } ?: LovePalettes.first()

val LocalLoveColors = staticCompositionLocalOf { LightLoveColors }

@Composable
fun MyLoveTheme(
    darkTheme: Boolean,
    paletteId: String,
    content: @Composable () -> Unit
) {
    val palette = paletteById(paletteId)
    CompositionLocalProvider(
        LocalLoveColors provides if (darkTheme) palette.dark else palette.light,
        content = content
    )
}