package com.mylove.app.ui

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

/**
 * 液态玻璃表面：取样背景 -> 提饱和 -> 模糊 -> 边缘折射。
 */
@Composable
fun Modifier.glass(
    backdrop: Backdrop,
    shape: Shape = RoundedCornerShape(26.dp),
    blurRadius: Dp = 18.dp,
    lensHeight: Dp = 12.dp,
    lensAmount: Dp = 18.dp,
    surface: Color = LocalLoveColors.current.glassSurface,
    tint: Color? = null,
    withShadow: Boolean = false
): Modifier {
    val isCornerBased = shape is CornerBasedShape
    val isDark = LocalLoveColors.current.isDark
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            if (isCornerBased && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lens(lensHeight.toPx(), lensAmount.toPx())
            }
        },
        highlight = { Highlight.Default.copy(alpha = 0.8f) },
        shadow = if (withShadow) {
            { Shadow(radius = 20.dp, alpha = 0.55f) }
        } else {
            null
        },
        onDrawSurface = {
            if (tint != null) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = if (isDark) 0.58f else 0.66f))
            } else {
                drawRect(surface)
            }
        }
    )
}

@Composable
fun GlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glass(backdrop = backdrop, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassButton(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.955f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 720f),
        label = "press"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .glass(
                backdrop = backdrop,
                shape = Capsule(),
                blurRadius = 10.dp,
                lensHeight = 10.dp,
                lensAmount = 14.dp,
                tint = tint
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .heightIn(min = 44.dp)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalLoveColors.current.textSecondary,
    background: Color = LocalLoveColors.current.stroke
) {
    val shape = Capsule()
    Text(
        text = text,
        modifier = modifier
            .background(background, shape)
            .border(1.dp, LocalLoveColors.current.stroke, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalLoveColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }
        }
        trailing?.invoke()
    }
}