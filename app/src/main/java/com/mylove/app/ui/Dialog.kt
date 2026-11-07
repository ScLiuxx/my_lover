package com.mylove.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop

/**
 * 弹窗在独立窗口里无法取样背景，用接近玻璃的纯色卡片代替。
 */
@Composable
fun rememberDialogBackdrop(): Backdrop = rememberCanvasBackdrop { }
@Composable
fun LoveDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalLoveColors.current
    val shape = RoundedCornerShape(28.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (colors.isDark) Color(0xFF2A211F) else Color(0xFFFFFCFA))
                .border(1.dp, colors.stroke, shape)
                .padding(22.dp),
            content = content
        )
    }
}

@Composable
fun DialogTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = LocalLoveColors.current.text,
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun DialogBody(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = LocalLoveColors.current.textSecondary,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
}

@Composable
fun LoveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    val colors = LocalLoveColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.stroke.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = colors.textTertiary,
                fontSize = 14.sp
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
            cursorBrush = SolidColor(colors.accent),
            maxLines = 4
        )
    }
}