package com.mylove.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.mylove.app.data.LoveStore
import com.mylove.app.data.cnDate
import com.mylove.app.data.cnShort
import com.mylove.app.data.cnWeekday
import com.mylove.app.data.daysTogether
import com.mylove.app.data.daysUntil
import com.mylove.app.data.formatElapsed
import com.mylove.app.data.nextHundredDay
import com.mylove.app.data.nextOccurrence
import com.mylove.app.ui.GlassCard
import com.mylove.app.ui.HairLine
import com.mylove.app.ui.LocalLoveColors
import com.mylove.app.ui.PageHeader
import com.mylove.app.ui.SectionTitle
import com.mylove.app.ui.rememberNow
import java.time.Duration
import java.time.LocalDate

@Composable
fun UsScreen(
    store: LoveStore,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val now = rememberNow()
    val today = now.toLocalDate()
    val start = store.startDate
    val days = daysTogether(start, today)
    val duration = Duration.between(start.atStartOfDay(), now)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(
                title = "我们",
                subtitle = "${today.cnDate()} · ${today.cnWeekday()}"
            )
        }
        item {
            HeroCard(
                days = days,
                elapsed = formatElapsed(duration),
                start = start,
                backdrop = backdrop
            )
        }
        item {
            CoupleCard(
                meName = store.meName,
                partnerName = store.partnerName,
                dayIndex = days + 1,
                backdrop = backdrop
            )
        }
        item {
            PresenceCard(
                meName = store.meName,
                backdrop = backdrop
            )
        }
        item {
            UpcomingCard(
                start = start,
                meName = store.meName,
                partnerName = store.partnerName,
                meBirthday = store.meBirthday.nextOccurrence(today),
                partnerBirthday = store.partnerBirthday.nextOccurrence(today),
                today = today,
                backdrop = backdrop
            )
        }
    }
}

@Composable
private fun HeroCard(
    days: Long,
    elapsed: String,
    start: LocalDate,
    backdrop: Backdrop
) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(22.dp)
    ) {
        Text(
            text = "我们在一起",
            color = colors.textSecondary,
            fontSize = 13.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$days",
                color = colors.accent,
                fontSize = 68.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "天",
                color = colors.accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
        }
        Text(
            text = elapsed,
            color = colors.textSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(14.dp))
        HairLine()
        Spacer(Modifier.height(12.dp))
        Text(
            text = "从 ${start.cnDate()} 开始 · 第 ${days + 1} 天",
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CoupleCard(
    meName: String,
    partnerName: String,
    dayIndex: Long,
    backdrop: Backdrop
) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PersonColumn(emoji = "🙂", name = meName, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "在一起第",
                    color = colors.textTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "$dayIndex 天",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            PersonColumn(emoji = "🙂", name = partnerName, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PersonColumn(
    emoji: String,
    name: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 30.sp)
        Text(
            text = name,
            color = colors.textSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PresenceCard(
    meName: String,
    backdrop: Backdrop
) {
    val colors = LocalLoveColors.current
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TA 上次打开",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "57 小时前",
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "今天待了",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "你不到 1 分钟 · TA 还没来",
                color = colors.text,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$meName 的数据仅保存在这台设备",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun UpcomingCard(
    start: LocalDate,
    meName: String,
    partnerName: String,
    meBirthday: LocalDate,
    partnerBirthday: LocalDate,
    today: LocalDate,
    backdrop: Backdrop
) {
    val colors = LocalLoveColors.current
    val anniversary = nextHundredDay(start, today)

    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        SectionTitle(
            title = "接下来",
            subtitle = "别错过重要的日子"
        )
        Spacer(Modifier.height(12.dp))
        UpcomingRow("💑", "在一起的日子", "${anniversary.cnShort()} · 还有 ${daysUntil(anniversary, today)} 天")
        HairLine(Modifier.padding(vertical = 10.dp))
        UpcomingRow("🎂", "${partnerName}的生日", "${partnerBirthday.cnShort()} · 还有 ${daysUntil(partnerBirthday, today)} 天")
        HairLine(Modifier.padding(vertical = 10.dp))
        UpcomingRow("🎂", "${meName}的生日", "${meBirthday.cnShort()} · 还有 ${daysUntil(meBirthday, today)} 天")
        Spacer(Modifier.height(6.dp))
        Text(
            text = "重要的日子都在「我的」里",
            color = colors.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun UpcomingRow(emoji: String, title: String, detail: String) {
    val colors = LocalLoveColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 18.sp)
        Text(
            text = title,
            color = colors.text,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        )
        Text(
            text = detail,
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}