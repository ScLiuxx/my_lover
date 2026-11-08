package com.mylove.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.mylove.app.data.LoveStore
import com.mylove.app.data.PeriodPrediction
import com.mylove.app.data.buildPrediction
import com.mylove.app.data.cnDate
import com.mylove.app.data.cnShort
import com.mylove.app.data.periodDaysInMonth
import com.mylove.app.data.safeAtYear
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
import com.mylove.app.ui.rememberDialogBackdrop
import java.time.LocalDate
import java.time.MonthDay
import java.time.YearMonth

private data class DayEvent(val emoji: String, val title: String)

@Composable
fun CalendarScreen(
    store: LoveStore,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    val today = LocalDate.now()
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf(today) }

    val events = remember(month, store.startDate, store.meBirthday, store.partnerBirthday) {
        buildEvents(
            month = month,
            start = store.startDate,
            meName = store.meName,
            partnerName = store.partnerName,
            meBirthday = store.meBirthday,
            partnerBirthday = store.partnerBirthday
        )
    }
    val periodDays = remember(month, store.periodStarts, store.cycleLength, store.periodLength) {
        periodDaysInMonth(
            starts = store.periodStarts,
            cycleLength = store.cycleLength,
            periodLength = store.periodLength,
            month = month
        )
    }
    var showCycleSettings by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(title = "日历", subtitle = "把两个人的日子放在一起")
        }
        item {
            PeriodCard(
                store = store,
                backdrop = backdrop,
                onSettings = { showCycleSettings = true }
            )
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                MonthHeader(
                    month = month,
                    onPrev = {
                        month = month.minusMonths(1)
                        selected = month.atDay(1)
                    },
                    onNext = {
                        month = month.plusMonths(1)
                        selected = month.atDay(1)
                    }
                )
                Spacer(Modifier.height(10.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))
                MonthGrid(
                    month = month,
                    selected = selected,
                    today = today,
                    events = events,
                    periodDays = periodDays,
                    onSelect = { selected = it }
                )
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = selected.cnDate(),
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                val dayEvents = events[selected].orEmpty()
                if (dayEvents.isEmpty()) {
                    Text(
                        text = "这一天还没有安排",
                        color = colors.textTertiary,
                        fontSize = 13.sp
                    )
                } else {
                    dayEvents.forEachIndexed { index, event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(event.emoji, fontSize = 16.sp)
                            Text(
                                text = event.title,
                                color = colors.text,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                        if (index != dayEvents.lastIndex) {
                            HairLine(Modifier.padding(vertical = 6.dp))
                        }
                    }
                }
            }
        }
        item {
            MonthEventList(month = month, events = events, backdrop = backdrop)
        }
    }

    if (showCycleSettings) {
        CycleSettingsDialog(
            store = store,
            onDismiss = { showCycleSettings = false }
        )
    }
}

@Composable
private fun PeriodCard(
    store: LoveStore,
    backdrop: Backdrop,
    onSettings: () -> Unit
) {
    val colors = LocalLoveColors.current
    val today = LocalDate.now()
    val prediction = buildPrediction(
        starts = store.periodStarts,
        cycleLength = store.cycleLength,
        periodLength = store.periodLength,
        today = today
    )
    val statusText = when {
        prediction.currentDay != null -> "经期第 ${prediction.currentDay} 天"
        prediction.daysUntilNext != null -> "还有 ${prediction.daysUntilNext} 天"
        else -> "未记录"
    }

    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "生理期",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            GlassPill(
                text = statusText,
                color = if (prediction.currentDay != null) colors.heart else colors.textSecondary
            )
        }
        Spacer(Modifier.height(10.dp))
        if (prediction.lastStart == null) {
            Text(
                text = "记录一次开始日期，就能预测下次经期、排卵日和易孕期。",
                color = colors.textTertiary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        } else {
            PeriodRow("上次开始", prediction.lastStart.cnDate())
            Spacer(Modifier.height(6.dp))
            prediction.nextStart?.let {
                PeriodRow("预计下次", "${it.cnDate()} · 还有 ${prediction.daysUntilNext} 天")
                Spacer(Modifier.height(6.dp))
            }
            prediction.ovulation?.let {
                PeriodRow("排卵日", it.cnDate())
                Spacer(Modifier.height(6.dp))
            }
            if (prediction.fertileStart != null && prediction.fertileEnd != null) {
                PeriodRow(
                    "易孕期",
                    "${prediction.fertileStart.cnShort()} - ${prediction.fertileEnd.cnShort()}"
                )
                Spacer(Modifier.height(6.dp))
            }
            PeriodRow("周期 / 经期", "${store.cycleLength} 天 / ${store.periodLength} 天")
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassButton(
                backdrop = backdrop,
                onClick = { store.recordPeriodStart(today) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (prediction.lastStart == today) "今天已记录" else "今天来了",
                    color = colors.heart,
                    fontSize = 13.sp
                )
            }
            GlassButton(
                backdrop = backdrop,
                onClick = onSettings,
                modifier = Modifier.weight(1f)
            ) {
                Text("周期设置", color = colors.textSecondary, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = when {
                store.periodPassword.isBlank() -> "未设置口令 · 数据不会上传"
                store.isSynced -> "已加密同步 · 两台设备口令一致即可查看"
                else -> "已设置口令 · 云同步后双方可见"
            },
            color = colors.textTertiary,
            fontSize = 11.sp
        )
        Text(
            text = "预测仅供参考，不构成医疗建议",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PeriodRow(label: String, value: String) {
    val colors = LocalLoveColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = colors.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CycleSettingsDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    var cycle by remember { mutableIntStateOf(store.cycleLength) }
    var period by remember { mutableIntStateOf(store.periodLength) }
    var password by remember { mutableStateOf(store.periodPassword) }

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("周期设置")
        Spacer(Modifier.height(6.dp))
        DialogBody("按自己的规律调整。设置隐私口令后，生理期数据会加密上传，两台设备口令一致才能查看。")
        Spacer(Modifier.height(16.dp))
        AdjustRow(
            label = "周期长度",
            value = "$cycle 天",
            onMinus = { cycle = (cycle - 1).coerceAtLeast(20) },
            onPlus = { cycle = (cycle + 1).coerceAtMost(45) }
        )
        Spacer(Modifier.height(8.dp))
        AdjustRow(
            label = "经期长度",
            value = "$period 天",
            onMinus = { period = (period - 1).coerceAtLeast(2) },
            onPlus = { period = (period + 1).coerceAtMost(10) }
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "隐私口令",
            color = colors.textSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        LoveTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "两台设备要填一样的口令"
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "口令只保存在本机，服务器拿到的是密文",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton(
                backdrop = rememberDialogBackdrop(),
                onClick = {
                    store.clearPeriods()
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("清除记录", color = colors.textSecondary, fontSize = 13.sp)
            }
            GlassButton(
                backdrop = rememberDialogBackdrop(),
                onClick = {
                    store.cycleLength = cycle
                    store.periodLength = period
                    store.periodPassword = password.trim()
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                tint = colors.accent
            ) {
                Text("保存", color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AdjustRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    val colors = LocalLoveColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        RoundAdjust(text = "−", onClick = onMinus)
        Text(
            text = value,
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.width(70.dp)
        )
        RoundAdjust(text = "+", onClick = onPlus)
    }
}

@Composable
private fun RoundAdjust(text: String, onClick: () -> Unit) {
    val colors = LocalLoveColors.current
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(colors.stroke.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = colors.text, fontSize = 15.sp)
    }
}

private fun buildEvents(
    month: YearMonth,
    start: LocalDate,
    meName: String,
    partnerName: String,
    meBirthday: MonthDay?,
    partnerBirthday: MonthDay?
): Map<LocalDate, List<DayEvent>> {
    val map = mutableMapOf<LocalDate, MutableList<DayEvent>>()

    fun add(date: LocalDate, event: DayEvent) {
        if (YearMonth.from(date) == month) {
            map.getOrPut(date) { mutableListOf() }.add(event)
        }
    }

    val anniversary = runCatching { start.withYear(month.year) }.getOrNull()
    if (anniversary != null) {
        add(anniversary, DayEvent("❤️", "在一起纪念日"))
    }
    meBirthday?.let { add(it.safeAtYear(month.year), DayEvent("🎂", "$meName 生日")) }
    partnerBirthday?.let { add(it.safeAtYear(month.year), DayEvent("🎂", "$partnerName 生日")) }

    var hundred = 100L
    while (true) {
        val date = start.plusDays(hundred)
        if (YearMonth.from(date) == month) {
            add(date, DayEvent("💯", "在一起 ${hundred} 天"))
        }
        if (date.isAfter(month.atEndOfMonth())) break
        hundred += 100
    }

    return map
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val colors = LocalLoveColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${month.year}年${month.monthValue}月",
            color = colors.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        NavButton(text = "‹", onClick = onPrev)
        Spacer(Modifier.size(8.dp))
        NavButton(text = "›", onClick = onNext)
    }
}

@Composable
private fun NavButton(text: String, onClick: () -> Unit) {
    val colors = LocalLoveColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.stroke.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = colors.text, fontSize = 18.sp)
    }
}

@Composable
private fun WeekdayHeader() {
    val colors = LocalLoveColors.current
    Row(Modifier.fillMaxWidth()) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
            Text(
                text = day,
                color = colors.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    events: Map<LocalDate, List<DayEvent>>,
    periodDays: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit
) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val cellCount = offset + daysInMonth
    val rows = (cellCount + 6) / 7

    Column(Modifier.fillMaxWidth()) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - offset + 1
                    if (day in 1..daysInMonth) {
                        val date = month.atDay(day)
                        DayCell(
                            date = date,
                            eventCount = events[date].orEmpty().size,
                            isPeriod = date in periodDays,
                            isSelected = date == selected,
                            isToday = date == today,
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    eventCount: Int,
    isPeriod: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(colors.accentSoft)
                } else if (isToday) {
                    Modifier.border(1.dp, colors.accent.copy(alpha = 0.5f), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${date.dayOfMonth}",
                color = when {
                    isSelected -> colors.accent
                    isToday -> colors.accent
                    else -> colors.text
                },
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
            )
            if (eventCount > 0 || isPeriod) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (isPeriod) {
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(colors.heart)
                        )
                    }
                    if (eventCount > 0) {
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthEventList(
    month: YearMonth,
    events: Map<LocalDate, List<DayEvent>>,
    backdrop: Backdrop
) {
    val colors = LocalLoveColors.current
    if (events.isEmpty()) return

    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            text = "${month.monthValue}月的纪念日",
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        events.toSortedMap().forEach { (date, list) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text(date.cnShort(), color = colors.textSecondary, fontSize = 13.sp)
                Spacer(Modifier.size(10.dp))
                Text(
                    text = list.joinToString(" · ") { "${it.emoji} ${it.title}" },
                    color = colors.text,
                    fontSize = 13.sp
                )
            }
        }
    }
}