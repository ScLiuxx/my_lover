package com.mylove.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 极简 HTTP 客户端，只依赖 Android 自带的 HttpURLConnection 与 org.json。
 */
object SyncClient {

    data class Result(
        val success: Boolean,
        val message: String,
        val body: JSONObject? = null
    )

    suspend fun create(server: String, name: String): Result =
        request(server, "POST", "/api/couples", JSONObject().put("name", name), null)

    suspend fun join(server: String, code: String, name: String): Result =
        request(
            server,
            "POST",
            "/api/couples/join",
            JSONObject().put("invite_code", code).put("name", name),
            null
        )

    suspend fun pull(server: String, token: String): Result =
        request(server, "GET", "/api/couples/current", null, token)

    suspend fun push(server: String, token: String, state: JSONObject): Result =
        request(
            server,
            "PUT",
            "/api/couples/current/state",
            JSONObject().put("state", state),
            token
        )

    suspend fun miss(server: String, token: String): Result =
        request(server, "POST", "/api/couples/current/miss", JSONObject(), token)

    private suspend fun request(
        server: String,
        method: String,
        path: String,
        body: JSONObject?,
        token: String?
    ): Result = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val base = server.trim().trimEnd('/')
            if (!base.startsWith("http://") && !base.startsWith("https://")) {
                return@withContext Result(false, "地址需要以 http:// 或 https:// 开头")
            }
            connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (token != null) setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    doOutput = true
                    outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code in 200..299) {
                Result(true, "ok", JSONObject(text))
            } else {
                Result(false, "HTTP $code ${text.take(100)}")
            }
        } catch (e: Exception) {
            Result(false, e.message ?: "网络错误")
        } finally {
            connection?.disconnect()
        }
    }
}