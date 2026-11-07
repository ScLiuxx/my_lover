package com.mylove.app.data

import org.json.JSONArray
import org.json.JSONObject

data class QuizQuestion(
    val id: String,
    val title: String,
    val options: List<String>
)

val QuizQuestions = listOf(
    QuizQuestion("q1", "TA 最喜欢的季节？", listOf("春天", "夏天", "秋天", "冬天")),
    QuizQuestion("q2", "TA 更喜欢的约会方式？", listOf("看电影", "散步", "吃饭", "旅行")),
    QuizQuestion("q3", "TA 生气时更希望你怎么做？", listOf("先抱抱", "讲道理", "让我静静", "带我吃好吃的")),
    QuizQuestion("q4", "TA 最想收到的礼物？", listOf("花", "实用的小东西", "手写卡片", "一起出去玩")),
    QuizQuestion("q5", "TA 的起床气？", listOf("很严重", "有一点", "基本没有", "看心情"))
)

fun parseQuiz(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    return runCatching {
        val json = JSONObject(raw)
        json.keys().asSequence().associateWith { key -> json.optString(key) }
    }.getOrDefault(emptyMap())
}

fun encodeQuiz(answers: Map<String, String>): String {
    val json = JSONObject()
    answers.forEach { (key, value) -> json.put(key, value) }
    return json.toString()
}

/** 双方都答过的题目里，答案一致的比例。没有共同题目时返回 null。 */
fun quizScore(mine: Map<String, String>, partner: Map<String, String>): Int? {
    val common = mine.keys.intersect(partner.keys)
    if (common.isEmpty()) return null
    val hits = common.count { mine[it] == partner[it] }
    return hits * 100 / common.size
}

fun encodeQuestions(questions: List<QuizQuestion>): String {
    val array = JSONArray()
    questions.forEach { question ->
        array.put(
            JSONObject().apply {
                put("id", question.id)
                put("title", question.title)
                put("options", JSONArray(question.options))
            }
        )
    }
    return array.toString()
}

fun decodeQuestions(raw: String): List<QuizQuestion> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val id = obj.optString("id")
            val title = obj.optString("title")
            val optionsArray = obj.optJSONArray("options") ?: return@mapNotNull null
            val options = (0 until optionsArray.length())
                .map { optionsArray.optString(it) }
                .filter { it.isNotBlank() }
            if (id.isBlank() || title.isBlank() || options.size < 2) {
                null
            } else {
                QuizQuestion(id, title, options)
            }
        }
    }.getOrDefault(emptyList())
}