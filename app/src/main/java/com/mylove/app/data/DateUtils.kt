package com.mylove.app.data

import java.time.Duration
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit

fun LocalDate.cnDate(): String = "${year}年${monthValue}月${dayOfMonth}日"

fun LocalDate.cnWeekday(): String = when (dayOfWeek.value) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    else -> "周日"
}

fun LocalDate.cnShort(): String = "${monthValue}月${dayOfMonth}日"

fun MonthDay.cnShort(): String = "${monthValue}月${dayOfMonth}日"

fun daysTogether(start: LocalDate, today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(start, today)

fun daysUntil(date: LocalDate, today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, date)

fun MonthDay.nextOccurrence(today: LocalDate = LocalDate.now()): LocalDate {
    var date = atYear(today.year)
    if (date.isBefore(today)) date = atYear(today.year + 1)
    return date
}

/** 下一个整数百天纪念日。 */
fun nextHundredDay(start: LocalDate, today: LocalDate = LocalDate.now()): LocalDate {
    val days = daysTogether(start, today)
    val next = ((days / 100) + 1) * 100
    return start.plusDays(next)
}

fun formatClock(now: java.time.LocalDateTime): String {
    val h = now.hour.toString().padStart(2, '0')
    val m = now.minute.toString().padStart(2, '0')
    val s = now.second.toString().padStart(2, '0')
    return "$h:$m:$s"
}

fun formatElapsed(duration: Duration): String {
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    return "$hours 小时 $minutes 分 $seconds 秒"
}

fun formatAgo(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - timestamp) / 60000
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "$minutes 分钟前"
        minutes < 60 * 24 -> "${minutes / 60} 小时前"
        else -> "${minutes / (60 * 24)} 天前"
    }
}