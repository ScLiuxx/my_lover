package com.mylove.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.mylove.app.data.LoveStore
import com.mylove.app.data.SyncClient
import com.mylove.app.notify.LoveNotifications
import com.mylove.app.data.cnDate
import com.mylove.app.data.cnShort
import com.mylove.app.data.daysTogether
import com.mylove.app.data.nextHundredDay
import com.mylove.app.ui.DialogBody
import com.mylove.app.ui.DialogTitle
import com.mylove.app.ui.DateStepperDialog
import com.mylove.app.ui.GlassButton
import com.mylove.app.ui.GlassCard
import com.mylove.app.ui.HairLine
import com.mylove.app.ui.LocalLoveColors
import com.mylove.app.ui.LoveDialog
import com.mylove.app.ui.LovePalettes
import com.mylove.app.ui.LoveTextField
import com.mylove.app.ui.MonthDayDialog
import com.mylove.app.ui.PageHeader
import com.mylove.app.ui.rememberDialogBackdrop
import kotlinx.coroutines.launch

private val themeOptions = listOf("跟随系统", "浅色", "深色")

@Composable
fun MeScreen(
    store: LoveStore,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val colors = LocalLoveColors.current
    val days = daysTogether(store.startDate)
    var editingNames by remember { mutableStateOf(false) }
    var confirmingReset by remember { mutableStateOf(false) }
    var syncDialog by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var syncing by remember { mutableStateOf(false) }
    var editingAnniversary by remember { mutableStateOf(false) }
    var editingMyBirthday by remember { mutableStateOf(false) }
    var editingPartnerBirthday by remember { mutableStateOf(false) }
    var showCrashLog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            store.notificationsEnabled = true
            LoveNotifications.update(context, store)
            notificationMessage = "已开启实况通知"
        } else {
            notificationMessage = "没有拿到通知权限，可以在系统设置里开启"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(title = "我的", subtitle = "这台设备上的两个人")
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🙂", fontSize = 34.sp)
                        Text(store.meName, color = colors.text, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "在一起第",
                            color = colors.textTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${days + 1} 天",
                            color = colors.accent,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = store.startDate.cnDate(),
                            color = colors.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🙂", fontSize = 34.sp)
                        Text(store.partnerName, color = colors.text, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                GlassButton(
                    backdrop = backdrop,
                    onClick = { editingNames = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("编辑昵称", color = colors.accent, fontSize = 14.sp)
                }
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "外观",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeOptions.forEachIndexed { index, label ->
                        val selected = store.themeMode == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(Capsule())
                                .background(
                                    if (selected) colors.accentSoft
                                    else colors.stroke.copy(alpha = 0.4f)
                                )
                                .then(
                                    if (selected) Modifier.border(1.dp, colors.accent, Capsule())
                                    else Modifier
                                )
                                .clickable { store.themeMode = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) colors.accent else colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "配色",
                    color = colors.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LovePalettes.forEach { palette ->
                        val selected = store.paletteId == palette.id
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) colors.accentSoft
                                    else colors.stroke.copy(alpha = 0.35f)
                                )
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            1.dp,
                                            colors.accent,
                                            RoundedCornerShape(14.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { store.paletteId = palette.id }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(palette.light.accent)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = palette.name,
                                color = if (selected) colors.accent else colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "配色会同时影响背景光斑、卡片和强调色",
                    color = colors.textTertiary,
                    fontSize = 11.sp
                )
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "实况通知",
                        color = colors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .clip(Capsule())
                            .background(
                                if (store.notificationsEnabled) colors.accentSoft
                                else colors.stroke.copy(alpha = 0.4f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (store.notificationsEnabled) "已开启" else "未开启",
                            color = if (store.notificationsEnabled) colors.accent else colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "在通知栏常驻显示在一起的天数和小宠物状态；Android 16 上会以实况通知（Live Updates）呈现。",
                    color = colors.textTertiary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                GlassButton(
                    backdrop = backdrop,
                    onClick = {
                        if (store.notificationsEnabled) {
                            store.notificationsEnabled = false
                            LoveNotifications.cancel(context)
                            notificationMessage = "已关闭实况通知"
                        } else if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            store.notificationsEnabled = true
                            LoveNotifications.update(context, store)
                            notificationMessage = "已开启实况通知"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (store.notificationsEnabled) "关闭通知" else "开启通知",
                        color = colors.accent,
                        fontSize = 13.sp
                    )
                }
                if (notificationMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = notificationMessage!!,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "重要的日子",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                EditableInfoRow("纪念日", store.startDate.cnDate()) {
                    editingAnniversary = true
                }
                HairLine(Modifier.padding(vertical = 10.dp))
                EditableInfoRow(
                    "${store.partnerName} 生日",
                    store.partnerBirthday?.cnShort() ?: "未设置"
                ) {
                    editingPartnerBirthday = true
                }
                HairLine(Modifier.padding(vertical = 10.dp))
                EditableInfoRow(
                    "${store.meName} 生日",
                    store.meBirthday?.cnShort() ?: "未设置"
                ) {
                    editingMyBirthday = true
                }
                HairLine(Modifier.padding(vertical = 10.dp))
                InfoRow(
                    "下一个百天",
                    nextHundredDay(store.startDate).cnDate()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "点一下就能修改",
                    color = colors.textTertiary,
                    fontSize = 11.sp
                )
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "云同步",
                        color = colors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .clip(Capsule())
                            .background(
                                if (store.isSynced) colors.accentSoft
                                else colors.stroke.copy(alpha = 0.4f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (store.isSynced) "已连接" else "本地模式",
                            color = if (store.isSynced) colors.accent else colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (store.isSynced) {
                        "房间 ${store.syncInviteCode} · 角色 ${if (store.syncRole == "a") "A" else "B"}"
                    } else {
                        "把两个人的数据放到服务器，换手机也能接着用"
                    },
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        backdrop = backdrop,
                        onClick = { syncDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (store.isSynced) "重新连接" else "连接服务器",
                            color = colors.accent,
                            fontSize = 13.sp
                        )
                    }
                    if (store.isSynced) {
                        GlassButton(
                            backdrop = backdrop,
                            enabled = !syncing,
                            onClick = {
                                scope.launch {
                                    syncing = true
                                    val result = SyncClient.push(
                                        store.syncServer,
                                        store.syncToken,
                                        store.buildRemoteState()
                                    )
                                    syncMessage = if (result.success) {
                                        "已上传到服务器"
                                    } else {
                                        "上传失败：${result.message}"
                                    }
                                    syncing = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("上传", color = colors.textSecondary, fontSize = 13.sp)
                        }
                        GlassButton(
                            backdrop = backdrop,
                            enabled = !syncing,
                            onClick = {
                                scope.launch {
                                    syncing = true
                                    val result = SyncClient.pull(store.syncServer, store.syncToken)
                                    val body = result.body
                                    if (result.success && body != null) {
                                        body.optJSONObject("state")?.let { store.applyRemoteState(it) }
                                        syncMessage = "已拉取云端数据"
                                    } else {
                                        syncMessage = "拉取失败：${result.message}"
                                    }
                                    syncing = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("拉取", color = colors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }
                if (syncMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = syncMessage!!,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
                if (store.isSynced) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "断开连接",
                        color = colors.textTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            store.disconnectSpace()
                            syncMessage = "已断开"
                        }
                    )
                }
            }
        }
        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "关于",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                InfoRow("版本", "1.0")
                HairLine(Modifier.padding(vertical = 10.dp))
                InfoRow("界面", "Liquid Glass · Backdrop")
                HairLine(Modifier.padding(vertical = 10.dp))
                InfoRow("数据", "只保存在本机")
                HairLine(Modifier.padding(vertical = 10.dp))
                EditableInfoRow("崩溃日志", "查看") { showCrashLog = true }
                Spacer(Modifier.height(16.dp))
                GlassButton(
                    backdrop = backdrop,
                    onClick = { store.guideSeen = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重看新手引导", color = colors.textSecondary, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                GlassButton(
                    backdrop = backdrop,
                    onClick = { confirmingReset = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重置小毛球", color = colors.textSecondary, fontSize = 13.sp)
                }
            }
        }
    }

    if (editingNames) {
        NamesDialog(
            store = store,
            onDismiss = { editingNames = false }
        )
    }

    if (confirmingReset) {
        LoveDialog(onDismiss = { confirmingReset = false }) {
            DialogTitle("重置小毛球")
            Spacer(Modifier.height(6.dp))
            DialogBody("饱食、开心、活力、成长和金币都会回到初始值。")
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                    backdrop = rememberDialogBackdrop(),
                    onClick = { confirmingReset = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("算了", color = colors.textSecondary, fontSize = 14.sp)
                }
                GlassButton(
                    backdrop = rememberDialogBackdrop(),
                    onClick = {
                        store.resetPet()
                        confirmingReset = false
                    },
                    modifier = Modifier.weight(1f),
                    tint = colors.accent
                ) {
                    Text("重置", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }

    if (syncDialog) {
        SyncDialog(
            store = store,
            onResult = { syncMessage = it },
            onDismiss = { syncDialog = false }
        )
    }

    if (editingAnniversary) {
        DateStepperDialog(
            title = "在一起的纪念日",
            initial = store.startDate,
            onDismiss = { editingAnniversary = false },
            onConfirm = {
                store.startDate = it
                editingAnniversary = false
            }
        )
    }

    if (editingMyBirthday) {
        MonthDayDialog(
            title = "${store.meName} 的生日",
            initial = store.meBirthday,
            onDismiss = { editingMyBirthday = false },
            onConfirm = {
                store.meBirthday = it
                editingMyBirthday = false
            }
        )
    }

    if (editingPartnerBirthday) {
        MonthDayDialog(
            title = "${store.partnerName} 的生日",
            initial = store.partnerBirthday,
            onDismiss = { editingPartnerBirthday = false },
            onConfirm = {
                store.partnerBirthday = it
                editingPartnerBirthday = false
            }
        )
    }

    if (showCrashLog) {
        val crash = remember {
            runCatching {
                java.io.File(context.filesDir, "last_crash.txt").readText()
            }.getOrNull()
        }
        LoveDialog(onDismiss = { showCrashLog = false }) {
            DialogTitle("崩溃日志")
            Spacer(Modifier.height(6.dp))
            if (crash.isNullOrBlank()) {
                DialogBody("没有记录到崩溃。")
            } else {
                Text(
                    text = crash.takeLast(1500),
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(14.dp))
            GlassButton(
                backdrop = rememberDialogBackdrop(),
                onClick = { showCrashLog = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("关闭", color = colors.accent, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SyncDialog(
    store: LoveStore,
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalLoveColors.current
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf(store.syncServer) }
    var name by remember { mutableStateOf(store.meName) }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("连接服务器")
        Spacer(Modifier.height(6.dp))
        DialogBody("留空邀请码 = 创建新空间；填对方的邀请码 = 加入。")
        Spacer(Modifier.height(14.dp))
        Text("服务器", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LoveTextField(
            value = server,
            onValueChange = { server = it },
            placeholder = "http://服务器地址:8000"
        )
        Spacer(Modifier.height(10.dp))
        Text("我的昵称", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LoveTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "昵称"
        )
        Spacer(Modifier.height(10.dp))
        Text("邀请码（可选）", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LoveTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            placeholder = "例如 E9RBR7"
        )
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = message!!, color = colors.textTertiary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton(
                backdrop = rememberDialogBackdrop(),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消", color = colors.textSecondary, fontSize = 14.sp)
            }
            GlassButton(
                backdrop = rememberDialogBackdrop(),
                enabled = !busy && server.isNotBlank() && name.isNotBlank(),
                onClick = {
                    busy = true
                    message = null
                    scope.launch {
                        val joined = code.isNotBlank()
                        val result = if (joined) {
                            SyncClient.join(server, code.trim(), name.trim())
                        } else {
                            SyncClient.create(server, name.trim())
                        }
                        val body = result.body
                        if (result.success && body != null) {
                            store.connectToSpace(
                                server = server.trim().trimEnd('/'),
                                token = body.optString("token"),
                                coupleId = body.optString("couple_id"),
                                inviteCode = body.optString("invite_code"),
                                role = body.optString("role")
                            )
                            if (joined) {
                                body.optJSONObject("state")?.let { store.applyRemoteState(it) }
                                onResult("已加入房间 ${body.optString("invite_code")}")
                            } else {
                                SyncClient.push(
                                    store.syncServer,
                                    store.syncToken,
                                    store.buildRemoteState()
                                )
                                onResult("已创建房间 ${body.optString("invite_code")}，把邀请码发给 TA")
                            }
                            onDismiss()
                        } else {
                            message = result.message
                        }
                        busy = false
                    }
                },
                modifier = Modifier.weight(1f),
                tint = colors.accent
            ) {
                Text(
                    text = when {
                        busy -> "连接中…"
                        code.isNotBlank() -> "加入"
                        else -> "创建"
                    },
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun EditableInfoRow(label: String, value: String, onClick: () -> Unit) {
    val colors = LocalLoveColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = colors.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "›",
            color = colors.textTertiary,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NamesDialog(store: LoveStore, onDismiss: () -> Unit) {
    val colors = LocalLoveColors.current
    var me by remember { mutableStateOf(store.meName) }
    var partner by remember { mutableStateOf(store.partnerName) }

    LoveDialog(onDismiss = onDismiss) {
        DialogTitle("编辑昵称")
        Spacer(Modifier.height(14.dp))
        Text("我", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LoveTextField(value = me, onValueChange = { me = it }, placeholder = "我的昵称")
        Spacer(Modifier.height(12.dp))
        Text("TA", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LoveTextField(value = partner, onValueChange = { partner = it }, placeholder = "TA 的昵称")
        Spacer(Modifier.height(18.dp))
        GlassButton(
            backdrop = rememberDialogBackdrop(),
            onClick = {
                if (me.isNotBlank()) store.meName = me.trim()
                if (partner.isNotBlank()) store.partnerName = partner.trim()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent
        ) {
            Text("保存", color = Color.White, fontSize = 14.sp)
        }
    }
}