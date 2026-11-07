package com.mylove.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

private val tabLabels = listOf("我们", "日历", "一起", "我的")

@Composable
fun LoveBottomBar(
    backdrop: Backdrop,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 14.dp)
    ) {
        val tabWidth = maxWidth / tabLabels.size
        val indicatorX by animateDpAsState(
            targetValue = tabWidth * selected.toFloat(),
            animationSpec = spring(dampingRatio = 0.74f, stiffness = 420f),
            label = "indicatorX"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .glass(
                    backdrop = backdrop,
                    shape = Capsule(),
                    blurRadius = 30.dp,
                    lensHeight = 24.dp,
                    lensAmount = 28.dp,
                    surface = colors.glassSurface.copy(alpha = 0.55f),
                    withShadow = true
                )
        ) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .clip(Capsule())
                    .background(
                        if (colors.isDark) Color.White.copy(alpha = 0.12f)
                        else Color.White.copy(alpha = 0.62f)
                    )
            )

            Row(Modifier.fillMaxSize()) {
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = index == selected
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) colors.text else colors.textTertiary,
                        label = "tabColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = null,
                                indication = null
                            ) { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}