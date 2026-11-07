package com.mylove.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PeriodPrediction(
    val lastStart: LocalDate?,
    val nextStart: LocalDate?,
    val ovulation: LocalDate?,
    val fertileStart: LocalDate?,
    val fertileEnd: LocalDate?,
    val daysUntilNext: Long?,
    val currentDay: Int?
)

fun buildPrediction(
    starts: List<LocalDate>,
    cycleLength: Int,
    periodLength: Int,
    today: LocalDate = LocalDate.now()
): PeriodPrediction {
    val sorted = starts.sorted()
    val last = sorted.lastOrNull() ?: return PeriodPrediction(
        lastStart = null,
        nextStart = null,
        ovulation = null,
        fertileStart = null,
        fertileEnd = null,
        daysUntilNext = null,
        currentDay = null
    )

    // 当前处于哪一次性经期内（允许跨一个月）
    val currentDay = sorted
        .map { ChronoUnit.DAYS.between(it, today).toInt() + 1 }
        .filter { it in 1..periodLength }
        .maxOrNull()

    var next = last.plusDays(cycleLength.toLong())
    while (next.isBefore(today)) {
        next = next.plusDays(cycleLength.toLong())
    }
    val ovulation = next.minusDays(14)
    return PeriodPrediction(
        lastStart = last,
        nextStart = next,
        ovulation = ovulation,
        fertileStart = ovulation.minusDays(5),
        fertileEnd = ovulation.plusDays(1),
        daysUntilNext = ChronoUnit.DAYS.between(today, next),
        currentDay = currentDay
    )
}

/** 某个月需要标记的经期日期（真实记录 + 往后预测）。 */
fun periodDaysInMonth(
    starts: List<LocalDate>,
    cycleLength: Int,
    periodLength: Int,
    month: java.time.YearMonth
): Set<LocalDate> {
    if (starts.isEmpty()) return emptySet()
    val sorted = starts.sorted()
    val result = mutableSetOf<LocalDate>()
    val monthStart = month.atDay(1)
    val monthEnd = month.atEndOfMonth()

    sorted.forEach { start ->
        for (offset in 0 until periodLength) {
            val day = start.plusDays(offset.toLong())
            if (!day.isBefore(monthStart) && !day.isAfter(monthEnd)) result.add(day)
        }
    }

    var predicted = sorted.last().plusDays(cycleLength.toLong())
    var guard = 0
    while (!predicted.isAfter(monthEnd) && guard < 48) {
        for (offset in 0 until periodLength) {
            val day = predicted.plusDays(offset.toLong())
            if (!day.isBefore(monthStart) && !day.isAfter(monthEnd)) result.add(day)
        }
        predicted = predicted.plusDays(cycleLength.toLong())
        guard += 1
    }
    return result
}