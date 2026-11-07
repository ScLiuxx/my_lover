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
 * 常驻实况通知：Android 16+ 使用 Live Updates（setRequestPromotedOngoing），低版本降级为普通常驻通知。
 */
object LoveNotifications {

    const val CHANNEL_ID = "love_live"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "恋爱实况",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示在一起的天数和小宠物状态"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun update(context: Context, store: LoveStore) {
        if (!store.isConfigured || !canPost(context)) return
        ensureChannel(context)

        val days = daysTogether(store.startDate)
        val pet = store.petSpec
        val title = "在一起第 ${days + 1} 天"
        val text = "${pet.emoji} ${pet.name} Lv.${store.petLevel} · 想你 ${store.missMe + store.missTa} 次"

        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= 36) {
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
        } else {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_heart)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pending)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}