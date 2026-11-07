package com.mylove.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 缓慢漂移的暖色光斑背景，是玻璃面板的取样来源。
 */
@Composable
fun LoveBackground(modifier: Modifier = Modifier) {
    val colors = LocalLoveColors.current
    val transition = rememberInfiniteTransition(label = "background")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                listOf(colors.bgTop, colors.bgMid, colors.bgBottom)
            )
        )

        val w = size.width
        val h = size.height

        fun blob(color: Color, cx: Float, cy: Float, radius: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0f)),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        blob(colors.blobRose, w * (0.12f + 0.06f * drift), h * (0.10f + 0.03f * drift), w * 0.62f)
        blob(colors.blobPeach, w * (0.86f - 0.05f * drift), h * (0.22f + 0.04f * drift), w * 0.55f)
        blob(colors.blobLilac, w * (0.24f + 0.04f * drift), h * (0.62f - 0.03f * drift), w * 0.60f)
        blob(colors.blobMint, w * (0.92f - 0.04f * drift), h * (0.78f + 0.02f * drift), w * 0.50f)
        blob(colors.blobRose, w * (0.55f - 0.03f * drift), h * 0.95f, w * 0.45f)
    }
}