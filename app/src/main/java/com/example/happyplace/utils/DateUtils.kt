package com.example.happyplace.utils

import java.time.LocalDate
import java.time.ZoneId

fun LocalDate.startOfDayMillis() : Long {
    return this.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
}

fun LocalDate.lastMondayBeforeCurrentMonth() : LocalDate {
    val firstDayOfMonth = this.withDayOfMonth(1)
    val subtract = firstDayOfMonth.dayOfWeek.ordinal
    return firstDayOfMonth.minusDays(subtract.toLong())
}

fun LocalDate.firstSundayAfterCurrentMonth() : LocalDate {
    val lastDayOfMonth = this.plusMonths(1).minusDays(1)
    return lastDayOfMonth.plusDays((6-lastDayOfMonth.dayOfWeek.ordinal).toLong())
}