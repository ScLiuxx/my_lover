package com.mylove.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * 用系统 AlarmManager 每 15 分钟检查一次对方动态，避免额外引入 WorkManager。
 */
object PartnerPollScheduler {

    const val ACTION = "com.mylove.app.PARTNER_POLL"
    private const val INTERVAL_MS = 15 * 60 * 1000L

    fun update(context: Context, enabled: Boolean) {
        runCatching {
            val manager = context.getSystemService(AlarmManager::class.java) ?: return
            val pending = pendingIntent(context)
            if (!enabled) {
                manager.cancel(pending)
                return
            }
            manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + INTERVAL_MS,
                INTERVAL_MS,
                pending
            )
        }
    }

    fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PartnerPollReceiver::class.java).setAction(ACTION)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class PartnerPollReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("my_love", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("notify_enabled", false) &&
            prefs.getBoolean("configured", false)
        if (!enabled) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            PartnerPollScheduler.update(context, true)
            return
        }

        val pendingResult = goAsync()
        Thread {
            runCatching { runBlocking { PartnerSync.check(context) } }
            pendingResult.finish()
        }.start()
    }
}