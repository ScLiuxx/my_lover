package com.mylove.app

import android.app.Application
import android.util.Log
import com.mylove.app.notify.CrashReporter
import java.io.File

/**
 * 记录最后一次崩溃堆栈，方便在「我的 → 崩溃日志」里查看。
 */
class MyLoveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val file = File(filesDir, "last_crash.txt")
                file.writeText(
                    "time: ${System.currentTimeMillis()}\n" +
                        Log.getStackTraceString(throwable)
                )
            }
            previous?.uncaughtException(thread, throwable)
        }

        Thread {
            runCatching { CrashReporter.uploadPending(this) }
        }.start()
    }
}