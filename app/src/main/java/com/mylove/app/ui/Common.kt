package com.mylove.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun rememberNow(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }
    return now
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = colors.text,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = colors.textSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
fun HairLine(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalLoveColors.current.stroke)
    )
}

@Composable
fun StatBar(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalLoveColors.current.accent
) {
    val colors = LocalLoveColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 2.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(colors.stroke.copy(alpha = 0.6f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(value / 100f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.85f))
            )
        }
        Text(
            text = "$value",
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}