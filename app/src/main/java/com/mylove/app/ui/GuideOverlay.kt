package com.mylove.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.Capsule

private data class GuideStep(
    val emoji: String,
    val title: String,
    val body: String
)

private val guideSteps = listOf(
    GuideStep("💞", "我们", "在一起的天数、实时计时和接下来的重要日子，都在第一页。"),
    GuideStep("📅", "日历", "纪念日、生日和生理期预测都会标在日历上。"),
    GuideStep("🐾", "一起", "养一只小宠物、答默契题、记录心情，还能戳一下告诉 TA 你想他了。"),
    GuideStep("⚙️", "我的", "换配色、开启实况通知、连接云同步，和 TA 共享同一份数据。")
)

/**
 * 首次进入主界面后的新手引导，可跳过，也可以从「我的」重新观看。
 */
@Composable
fun GuideOverlay(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalLoveColors.current
    var index by remember { mutableIntStateOf(0) }
    val step = guideSteps[index]
    val isLast = index == guideSteps.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (colors.isDark) 0.6f else 0.38f))
            .clickable(interactionSource = null, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(if (colors.isDark) Color(0xFF2A211F) else Color(0xFFFFFCFA))
                .border(1.dp, colors.stroke, RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Text(text = step.emoji, fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = step.title,
                color = colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = step.body,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                guideSteps.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .size(if (i == index) 16.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .background(if (i == index) colors.accent else colors.stroke)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "跳过",
                    color = colors.textTertiary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(onClick = onFinish)
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(Capsule())
                        .background(colors.accent)
                        .clickable {
                            if (isLast) {
                                onFinish()
                            } else {
                                index += 1
                            }
                        }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isLast) "开始使用" else "下一步",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}