package com.mylove.app.notify

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * 每 15 分钟拉一次云端，检查 TA 有没有新动态。
 */
class PartnerPollWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("my_love", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notify_enabled", false)) return Result.success()
        if (!prefs.getBoolean("configured", false)) return Result.success()
        runCatching { PartnerSync.check(applicationContext) }
        return Result.success()
    }
}

object PartnerPollScheduler {

    private const val WORK_NAME = "partner_poll"

    fun update(context: Context, enabled: Boolean) {
        runCatching {
            val manager = WorkManager.getInstance(context)
            if (!enabled) {
                manager.cancelUniqueWork(WORK_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<PartnerPollWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            manager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}