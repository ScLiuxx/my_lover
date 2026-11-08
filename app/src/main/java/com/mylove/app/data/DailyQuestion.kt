package com.mylove.app.data

import java.time.LocalDate

private val dailyQuestions = listOf(
    "今天最想对我抱怨的？",
    "今天最想被我夸什么？",
    "今天最想和我一起做什么？",
    "今天有没有偷偷想我？",
    "今天最想对我说的一句话？",
    "今天什么事让你最开心？",
    "今天有没有生我的气？",
    "今天最想收到我的什么消息？",
    "今天最想和我分享的小事？",
    "今天最想让我知道的心情？"
)

/** 按自然日轮换，保证两台设备同一天看到同一题。 */
fun dailyQuestionFor(date: LocalDate = LocalDate.now()): String {
    val size = dailyQuestions.size
    val index = ((date.toEpochDay() % size) + size) % size
    return dailyQuestions[index.toInt()]
}