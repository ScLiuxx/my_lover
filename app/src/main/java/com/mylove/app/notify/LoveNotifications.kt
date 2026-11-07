package com.mylove.app.notify

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mylove.app.MainActivity
import com.mylove.app.R
import com.mylove.app.data.LoveStore
import com.mylove.app.data.daysTogether

/**
 * 常驻实况通知 + 对方动态通知。
 * 所有厂商/系统差异都做了兜底，任何一步失败都不会让界面崩溃。
 */
object LoveNotifications {

    const val CHANNEL_ID = "love_live_v2"
    const val EVENTS_CHANNEL_ID = "partner_events"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val live = NotificationChannel(
            CHANNEL_ID,
            "恋爱实况",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "显示在一起的天数和小宠物状态"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }
        val events = NotificationChannel(
            EVENTS_CHANNEL_ID,
            "TA 的动态",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "想你了、每日一问、默契问答、情话和心情"
        }
        manager.createNotificationChannels(listOf(live, events))
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun update(context: Context, store: LoveStore) {
        if (!store.isConfigured || !canPost(context)) return
        runCatching {
            ensureChannels(context)
            val days = daysTogether(store.startDate)
            val pet = store.petSpec
            val title = "在一起第 ${days + 1} 天"
            val text = "${pet.emoji} ${pet.name} Lv.${store.petLevel} · 想你 ${store.missMe + store.missTa} 次"
            val pending = contentIntent(context)

            val fallback = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_heart)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pending)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            val notification = if (Build.VERSION.SDK_INT >= 36) {
                runCatching {
                    Notification.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_heart)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(pending)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setShortCriticalText("$days 天")
                        .setRequestPromotedOngoing(true)
                        .build()
                }.getOrDefault(fallback)
            } else {
                fallback
            }

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun notifyEvent(context: Context, id: Int, title: String, text: String) {
        if (!canPost(context)) return
        runCatching {
            ensureChannels(context)
            val notification = NotificationCompat.Builder(context, EVENTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_heart)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    fun cancel(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }
}