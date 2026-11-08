package com.mylove.app.notify

import android.content.Context
import com.mylove.app.data.SyncClient
import com.mylove.app.data.parseQuiz
import com.mylove.app.data.quizScore
import org.json.JSONObject

/**
 * 轮询云端，比较对方侧字段有没有更新，有就发一条通知。
 * 第一次运行只记录快照，不打扰。
 */
object PartnerSync {

    private const val PREFS = "my_love"
    private const val KEY_SNAPSHOT = "partner_snapshot"

    suspend fun check(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString("sync_token", "") ?: ""
        if (token.isBlank()) return
        val server = prefs.getString("sync_server", "http://123.249.80.35") ?: return
        val role = prefs.getString("sync_role", "a") ?: "a"
        val partnerKey = if (role == "b") "a" else "b"

        val result = SyncClient.pull(server, token)
        val state = result.body?.optJSONObject("state") ?: return

        val miss = state.optJSONObject("miss")?.optInt(partnerKey, 0) ?: 0
        val quiz = state.optJSONObject("quiz")?.optJSONObject(partnerKey)?.toString() ?: ""
        val daily = state.optJSONObject("daily")?.optString(partnerKey) ?: ""
        val mood = state.optJSONObject("moods")?.optString(partnerKey)
            ?.takeIf { it.isNotBlank() && it != "null" } ?: ""
        val note = state.optJSONObject("notes")?.optString(partnerKey) ?: ""

        val current = JSONObject().apply {
            put("miss", miss)
            put("quiz", quiz)
            put("daily", daily)
            put("mood", mood)
            put("note", note)
        }

        val raw = prefs.getString(KEY_SNAPSHOT, "") ?: ""
        if (raw.isBlank()) {
            prefs.edit().putString(KEY_SNAPSHOT, current.toString()).apply()
            return
        }
        val old = runCatching { JSONObject(raw) }.getOrNull()
        if (old == null) {
            prefs.edit().putString(KEY_SNAPSHOT, current.toString()).apply()
            return
        }

        if (miss > old.optInt("miss", 0)) {
            LoveNotifications.notifyEvent(
                context,
                2001,
                "TA 想你了",
                "已经想 TA $miss 次，戳一下回应吧"
            )
        }

        if (quiz.isNotBlank() && quiz != old.optString("quiz")) {
            val mine = parseQuiz(prefs.getString("my_quiz", "") ?: "")
            val partner = parseQuiz(quiz)
            val score = quizScore(mine, partner)
            LoveNotifications.notifyEvent(
                context,
                2002,
                "TA 完成了默契问答",
                if (score != null) "你们的默契度 $score 分" else "打开看看 TA 的选择"
            )
        }

        if (daily.isNotBlank() && daily != old.optString("daily")) {
            LoveNotifications.notifyEvent(
                context,
                2003,
                "TA 回答了每日一问",
                daily.take(40)
            )
        }

        if (mood.isNotBlank() && mood != old.optString("mood")) {
            LoveNotifications.notifyEvent(
                context,
                2004,
                "TA 记录了今日心情",
                mood
            )
        }

        if (note.isNotBlank() && note != old.optString("note")) {
            LoveNotifications.notifyEvent(
                context,
                2005,
                "TA 给你留了一句情话",
                note.take(40)
            )
        }

        prefs.edit().putString(KEY_SNAPSHOT, current.toString()).apply()
    }
}