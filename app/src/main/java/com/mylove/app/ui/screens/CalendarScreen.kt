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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mylove.app.data.cnDate
import com.mylove.app.data.cnShort
import com.mylove.app.ui.GlassCard
import com.mylove.app.ui.HairLine
import com.mylove.app.ui.LocalLoveColors
import com.mylove.app.ui.PageHeader
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
    meBirthday?.let { add(it.atYear(month.year), DayEvent("🎂", "$meName 生日")) }
    partnerBirthday?.let { add(it.atYear(month.year), DayEvent("🎂", "$partnerName 生日")) }

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
            if (eventCount > 0) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                )
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