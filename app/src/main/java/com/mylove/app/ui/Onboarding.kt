package com.mylove.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.mylove.app.data.LoveStore
import com.mylove.app.data.cnDate
import java.time.LocalDate
import java.time.MonthDay
import java.time.YearMonth

/**
 * 首次启动的填写引导：不预置任何数据。
 */
@Composable
fun OnboardingScreen(
    store: LoveStore,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    var me by remember { mutableStateOf(store.meName) }
    var partner by remember { mutableStateOf(store.partnerName) }
    var start by remember { mutableStateOf(LocalDate.now()) }
    var showDate by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = "我们",
            color = colors.text,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "先写下你们的信息，之后都可以在「我的」里修改。",
            color = colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp)
        ) {
            FieldLabel("我的昵称")
            LoveTextField(value = me, onValueChange = { me = it }, placeholder = "怎么称呼你")
            Spacer(Modifier.height(12.dp))
            FieldLabel("TA 的昵称")
            LoveTextField(value = partner, onValueChange = { partner = it }, placeholder = "怎么称呼 TA")
            Spacer(Modifier.height(12.dp))
            FieldLabel("在一起的第一天")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Capsule())
                    .background(colors.stroke.copy(alpha = 0.35f))
                    .clickable { showDate = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = start.cnDate(),
                    color = colors.text,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "修改", color = colors.accent, fontSize = 12.sp)
            }
        }

        GlassButton(
            backdrop = backdrop,
            onClick = { store.completeSetup(me, partner, start) },
            enabled = me.isNotBlank() && partner.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent
        ) {
            Text(
                text = "开始",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "生日可以稍后在「我的 → 重要的日子」里补上",
            color = colors.textTertiary,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    if (showDate) {
        DateStepperDialog(
            title = "在一起的第一天",
            initial = start,
            onDismiss = { showDate = false },
            onConfirm = {
                start = it
                showDate = false
            }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = LocalLoveColors.current.textSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun DateStepperDialog(
    title: String,
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.monthValue) }
    var day by remember { mutableIntStateOf(initial.dayOfMonth) }
    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    LaunchedEffect(maxDay) {
        if (day > maxDay) day = maxDay
    }

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle(title)
        Spacer(Modifier.height(14.dp))
        StepperRow(
            label = "年",
            value = year.toString(),
            onMinus = { year -= 1 },
            onPlus = { year += 1 }
        )
        StepperRow(
            label = "月",
            value = month.toString(),
            onMinus = { month = if (month == 1) 12 else month - 1 },
            onPlus = { month = if (month == 12) 1 else month + 1 }
        )
        StepperRow(
            label = "日",
            value = day.toString(),
            onMinus = { day = if (day == 1) maxDay else day - 1 },
            onPlus = { day = if (day == maxDay) 1 else day + 1 }
        )
        Spacer(Modifier.height(18.dp))
        DialogActions(
            onCancel = onDismiss,
            onConfirm = { onConfirm(LocalDate.of(year, month, day.coerceAtMost(maxDay))) }
        )
    }
}

@Composable
fun MonthDayDialog(
    title: String,
    initial: MonthDay?,
    onDismiss: () -> Unit,
    onConfirm: (MonthDay) -> Unit
) {
    val base = initial ?: MonthDay.of(1, 1)
    var month by remember { mutableIntStateOf(base.monthValue) }
    var day by remember { mutableIntStateOf(base.dayOfMonth) }
    // 用闰年计算，允许选择 2 月 29 日
    val maxDay = YearMonth.of(2024, month).lengthOfMonth()
    LaunchedEffect(maxDay) {
        if (day > maxDay) day = maxDay
    }

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle(title)
        Spacer(Modifier.height(14.dp))
        StepperRow(
            label = "月",
            value = month.toString(),
            onMinus = { month = if (month == 1) 12 else month - 1 },
            onPlus = { month = if (month == 12) 1 else month + 1 }
        )
        StepperRow(
            label = "日",
            value = day.toString(),
            onMinus = { day = if (day == 1) maxDay else day - 1 },
            onPlus = { day = if (day == maxDay) 1 else day + 1 }
        )
        Spacer(Modifier.height(18.dp))
        DialogActions(
            onCancel = onDismiss,
            onConfirm = { onConfirm(MonthDay.of(month, day.coerceAtMost(maxDay))) }
        )
    }
}

@Composable
private fun DialogActions(onCancel: () -> Unit, onConfirm: () -> Unit) {
    val colors = LocalLoveColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GlassButton(
            backdrop = rememberDialogBackdrop(),
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("取消", color = colors.textSecondary, fontSize = 14.sp)
        }
        GlassButton(
            backdrop = rememberDialogBackdrop(),
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            tint = colors.accent
        ) {
            Text("确定", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val colors = LocalLoveColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(36.dp)
        )
        RoundStep(text = "−", onClick = onMinus)
        Text(
            text = value,
            color = colors.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        RoundStep(text = "+", onClick = onPlus)
    }
}

@Composable
private fun RoundStep(text: String, onClick: () -> Unit) {
    val colors = LocalLoveColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(colors.stroke.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = colors.text, fontSize = 16.sp)
    }
}