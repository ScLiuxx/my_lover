package com.mylove.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.mylove.app.data.LoveStore
import com.mylove.app.data.cnShort
import com.mylove.app.data.daysTogether
import com.mylove.app.data.formatAgo
import com.mylove.app.data.nextHundredDay
import com.mylove.app.ui.DialogBody
import com.mylove.app.ui.DialogTitle
import com.mylove.app.ui.GlassButton
import com.mylove.app.ui.GlassCard
import com.mylove.app.ui.GlassPill
import com.mylove.app.ui.HairLine
import com.mylove.app.ui.LocalLoveColors
import com.mylove.app.ui.LoveDialog
import com.mylove.app.ui.LoveTextField
import com.mylove.app.ui.PageHeader
import com.mylove.app.ui.SectionTitle
import com.mylove.app.ui.StatBar
import com.mylove.app.ui.glass
import java.time.LocalDate

private enum class TogetherDialog { Report, Daily, Food, Quiz, Note, Milestone }

private val moodEmojis = listOf(
    "😊", "🥰", "😘", "😄", "😌", "😴",
    "😐", "😢", "😡", "🤔", "🥳", "😭"
)

private val dishPool = listOf(
    "火锅", "烤肉", "寿司", "麻辣烫", "黄焖鸡", "意面",
    "炒饭", "拉面", "烧烤", "沙拉", "小笼包", "部队锅"
)

@Composable
fun TogetherScreen(
    store: LoveStore,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    var dialog by remember { mutableStateOf<TogetherDialog?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(title = "一起", subtitle = "两个人之间，总得有点事做。")
        }
        item {
            ReportCard(
                store = store,
                backdrop = backdrop,
                onOpen = { dialog = TogetherDialog.Report }
            )
        }
        item {
            DailyQuestionCard(
                answer = store.dailyAnswer,
                backdrop = backdrop,
                onAnswer = { dialog = TogetherDialog.Daily }
            )
        }
        item {
            PetCard(store = store, backdrop = backdrop)
        }
        item {
            MiniAppGrid(
                backdrop = backdrop,
                onOpen = { dialog = it }
            )
        }
        item {
            MissingCard(store = store, backdrop = backdrop)
        }
        item {
            MoodCard(store = store, backdrop = backdrop)
        }
    }

    when (dialog) {
        TogetherDialog.Report -> ReportDialog(store = store, onDismiss = { dialog = null })
        TogetherDialog.Daily -> DailyDialog(store = store, onDismiss = { dialog = null })
        TogetherDialog.Food -> FoodDialog(onDismiss = { dialog = null })
        TogetherDialog.Quiz -> QuizDialog(onDismiss = { dialog = null })
        TogetherDialog.Note -> NoteDialog(store = store, onDismiss = { dialog = null })
        TogetherDialog.Milestone -> MilestoneDialog(store = store, onDismiss = { dialog = null })
        null -> Unit
    }
}

@Composable
private fun ReportCard(
    store: LoveStore,
    backdrop: Backdrop,
    onOpen: () -> Unit
) {
    val colors = LocalLoveColors.current
    val days = daysTogether(store.startDate)
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        onClick = onOpen
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📊", fontSize = 26.sp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "恋爱报告",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "在一起 $days 天里，我们攒下了多少",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }
            GlassPill(text = "打开", color = colors.accent)
        }
    }
}

@Composable
private fun DailyQuestionCard(
    answer: String,
    backdrop: Backdrop,
    onAnswer: () -> Unit
) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "每日一问",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            GlassPill(
                text = if (answer.isBlank()) "还没答" else "已回答",
                color = if (answer.isBlank()) colors.textTertiary else colors.accent
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "今天最想对我抱怨的？",
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        if (answer.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "你的回答：$answer",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "互相可见 · 每天 0 点更新",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(12.dp))
        GlassButton(
            backdrop = backdrop,
            onClick = onAnswer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (answer.isBlank()) "写回答" else "修改回答",
                color = colors.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PetCard(store: LoveStore, backdrop: Backdrop) {
    val colors = LocalLoveColors.current
    val status = when {
        store.energy < 25 -> "我快没力气了"
        store.food < 25 -> "肚子有点饿了"
        store.joy < 25 -> "陪我说说话嘛"
        else -> "今天也想和你待在一起"
    }

    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🦊", fontSize = 42.sp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "小毛球",
                    color = colors.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Lv.${store.petLevel} ${store.petForm}",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }
            GlassPill(text = "🪙 ${store.coins}", color = colors.textSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .clip(Capsule())
                .background(colors.stroke.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = status, color = colors.textSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        StatBar(label = "饱食", value = store.food)
        Spacer(Modifier.height(8.dp))
        StatBar(label = "开心", value = store.joy)
        Spacer(Modifier.height(8.dp))
        StatBar(label = "活力", value = store.energy)
        Spacer(Modifier.height(8.dp))
        StatBar(label = "成长", value = store.growth, tint = colors.heart)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PetAction(
                emoji = "🍚",
                label = "喂食 ${store.feedLimit - store.feedLeft}/${store.feedLimit}",
                enabled = store.feedLeft > 0 && store.coins >= 10,
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = store::feed
            )
            PetAction(
                emoji = "🤚",
                label = "摸摸",
                enabled = true,
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = store::petAction
            )
            PetAction(
                emoji = "🎮",
                label = "玩耍",
                enabled = store.coins >= 5,
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = store::play
            )
            PetAction(
                emoji = "😴",
                label = "睡觉",
                enabled = true,
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = store::sleep
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "任务：喂食一次 · 摸摸三次 · 玩耍一次",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PetAction(
    emoji: String,
    label: String,
    enabled: Boolean,
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    Column(
        modifier = modifier
            .glass(
                backdrop = backdrop,
                shape = Capsule(),
                blurRadius = 8.dp,
                lensHeight = 8.dp,
                lensAmount = 10.dp
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = label,
            color = if (enabled) colors.textSecondary else colors.textTertiary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MiniAppGrid(
    backdrop: Backdrop,
    onOpen: (TogetherDialog) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniAppCard(
                emoji = "🍽️",
                title = "今天吃什么",
                subtitle = "转一转，别再纠结了",
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = { onOpen(TogetherDialog.Food) }
            )
            MiniAppCard(
                emoji = "🧠",
                title = "默契问答",
                subtitle = "默契度 80 分",
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = { onOpen(TogetherDialog.Quiz) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniAppCard(
                emoji = "💌",
                title = "情话",
                subtitle = "写一句只给 TA 看",
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = { onOpen(TogetherDialog.Note) }
            )
            MiniAppCard(
                emoji = "🚩",
                title = "里程碑",
                subtitle = "一起走过的每个整数",
                backdrop = backdrop,
                modifier = Modifier.weight(1f),
                onClick = { onOpen(TogetherDialog.Milestone) }
            )
        }
    }
}

@Composable
private fun MiniAppCard(
    emoji: String,
    title: String,
    subtitle: String,
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MissingCard(store: LoveStore, backdrop: Backdrop) {
    val colors = LocalLoveColors.current
    val heartScale = remember { Animatable(1f) }
    LaunchedEffect(store.missMe) {
        heartScale.snapTo(1f)
        heartScale.animateTo(1.28f, spring(dampingRatio = 0.35f, stiffness = 320f))
        heartScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 260f))
    }

    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "想你了",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "今天 ${store.missMe % 7 + 1} 次",
                color = colors.textTertiary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "💗",
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .graphicsLayer {
                    scaleX = heartScale.value
                    scaleY = heartScale.value
                }
                .clickable(
                    interactionSource = null,
                    indication = null
                ) { store.miss() }
        )
        Spacer(Modifier.height(6.dp))
        Row {
            Text(
                text = "你 ${store.missMe} 次",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "TA ${store.missTa} 次",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(14.dp))
        GlassButton(
            backdrop = backdrop,
            onClick = { store.miss() },
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent
        ) {
            Text(
                text = "戳一下，让 TA 知道",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MoodCard(store: LoveStore, backdrop: Backdrop) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "今日心情",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "TA 还没记",
                color = colors.textTertiary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        for (rowStart in moodEmojis.indices step 6) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (index in rowStart until (rowStart + 6).coerceAtMost(moodEmojis.size)) {
                    val emoji = moodEmojis[index]
                    val selected = store.myMood == emoji
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (selected) colors.accentSoft
                                else colors.stroke.copy(alpha = 0.4f)
                            )
                            .then(
                                if (selected) Modifier.border(1.dp, colors.accent, CircleShape)
                                else Modifier
                            )
                            .clickable { store.myMood = if (selected) null else emoji }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = if (store.myMood == null) "选一个今天的心情" else "今天的心情：${store.myMood}",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ReportDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    val days = daysTogether(store.startDate)
    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("恋爱报告")
        Spacer(Modifier.height(4.dp))
        DialogBody("在一起 $days 天，这些数字都被记下来了。")
        Spacer(Modifier.height(16.dp))
        ReportRow("在一起", "$days 天")
        HairLine(Modifier.padding(vertical = 10.dp))
        ReportRow("想你", "${store.missMe + store.missTa} 次")
        HairLine(Modifier.padding(vertical = 10.dp))
        ReportRow("小毛球", "Lv.${store.petLevel} ${store.petForm}")
        HairLine(Modifier.padding(vertical = 10.dp))
        ReportRow("每日一问", if (store.dailyAnswer.isBlank()) "还没回答" else "已回答")
        Spacer(Modifier.height(18.dp))
        GlassButton(
            backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("知道啦", color = colors.accent, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    val colors = LocalLoveColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DailyDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    var text by remember { mutableStateOf(store.dailyAnswer) }
    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("每日一问")
        Spacer(Modifier.height(6.dp))
        DialogBody("今天最想对我抱怨的？")
        Spacer(Modifier.height(14.dp))
        LoveTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "写点什么吧"
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton(
                backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消", color = colors.textSecondary, fontSize = 14.sp)
            }
            GlassButton(
                backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
                onClick = {
                    store.dailyAnswer = text.trim()
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                tint = colors.accent
            ) {
                Text("保存", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun FoodDialog(onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    var dish by remember { mutableStateOf(dishPool.random()) }
    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("今天吃什么")
        Spacer(Modifier.height(6.dp))
        DialogBody("交给运气，别再纠结了。")
        Spacer(Modifier.height(18.dp))
        Text(
            text = dish,
            color = colors.accent,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton(
                backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("就它了", color = colors.textSecondary, fontSize = 14.sp)
            }
            GlassButton(
                backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
                onClick = { dish = dishPool.random() },
                modifier = Modifier.weight(1f),
                tint = colors.accent
            ) {
                Text("再转一次", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun QuizDialog(onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("默契问答")
        Spacer(Modifier.height(6.dp))
        DialogBody("今天的问题：TA 最想收到的礼物是什么？")
        Spacer(Modifier.height(16.dp))
        Text(
            text = "80 分",
            color = colors.accent,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "你们的默契度超过 82% 的情侣",
            color = colors.textTertiary,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(18.dp))
        GlassButton(
            backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent
        ) {
            Text("下次再答", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NoteDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    var text by remember { mutableStateOf(store.loveNote) }
    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("情话")
        Spacer(Modifier.height(6.dp))
        DialogBody("写一句只给 TA 看的话。")
        Spacer(Modifier.height(14.dp))
        LoveTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "今天也很喜欢你"
        )
        Spacer(Modifier.height(16.dp))
        GlassButton(
            backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
            onClick = {
                store.loveNote = text.trim()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent
        ) {
            Text("收好", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MilestoneDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    val today = LocalDate.now()
    val days = daysTogether(store.startDate, today)
    val next = nextHundredDay(store.startDate, today)

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("里程碑")
        Spacer(Modifier.height(6.dp))
        DialogBody("一起走过的每个整数。")
        Spacer(Modifier.height(16.dp))
        listOf(1, 100, 200, 300, 365).forEach { milestone ->
            val reached = days >= milestone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text(
                    text = if (reached) "✅" else "⏳",
                    fontSize = 14.sp
                )
                Text(
                    text = "$milestone 天",
                    color = if (reached) colors.text else colors.textTertiary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Text(
                    text = if (reached) "已达成" else "还有 ${milestone - days} 天",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "下一个：${next.cnShort()} · ${days - (days / 100) * 100} / 100",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(16.dp))
        GlassButton(
            backdrop = com.kyant.backdrop.backdrops.rememberCanvasBackdrop { },
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("关闭", color = colors.accent, fontSize = 14.sp)
        }
    }
}