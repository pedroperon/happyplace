package com.example.happyplace.utils

import com.example.happyplace.Periodicity
import com.example.happyplace.Task
import java.time.LocalDate
import java.time.ZoneId

fun LocalDate.startOfDayMillis() : Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
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

fun LocalDate.containsDateTimeInMillis(timeInMillis:Long) : Boolean {
    return timeInMillis in (this.startOfDayMillis()..<this.plusDays(1).startOfDayMillis())
}

fun Task.isRecurrent() : Boolean {
    return (
            hasPeriodicity() &&
            periodicity!=null
            && periodicity.numberOfIntervals>0
            && periodicity.intervalType!=Periodicity.IntervalType.UNRECOGNIZED
            )
}