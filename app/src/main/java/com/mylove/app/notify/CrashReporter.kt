package com.mylove.app.notify

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 把上一次崩溃堆栈上报到自己的后端，方便远程定位。
 */
object CrashReporter {

    private const val ENDPOINT = "http://123.249.80.35/api/crash"

    fun uploadPending(context: Context) {
        val file = File(context.filesDir, "last_crash.txt")
        if (!file.exists()) return
        val stack = runCatching { file.readText() }.getOrNull().orEmpty()
        if (stack.isBlank()) return

        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()

        val body = JSONObject().apply {
            put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("android", "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
            put("version", version)
            put("stack", stack.take(8000))
        }

        var connection: HttpURLConnection? = null
        val ok = runCatching {
            connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6000
                readTimeout = 6000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            connection!!.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            connection!!.responseCode in 200..299
        }.getOrDefault(false)

        runCatching { connection?.disconnect() }
        if (ok) runCatching { file.delete() }
    }
}