package com.padi.warehouse.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
const val dateRegex = "^\\d{4}-(0[1-9]|1[012])-?(0[1-9]|[12][0-9]|3[01])?\$"

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun Long.fromEpochMilli(): LocalDate {
    return LocalDate.ofEpochDay(this / (1000 * 60 * 60 * 24))
}

fun String.toLocalDate(): LocalDate? {
    return try {
        LocalDate.parse(
            this,
            dateFormatter
        )
    } catch (ex: DateTimeParseException) {
        try {
            LocalDate.parse(
                "$this-01",
                dateFormatter
            ).with(TemporalAdjusters.lastDayOfMonth())
        } catch (ex: DateTimeParseException) {
            null
        }
    }
}

fun String.formatDate(): String? {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val date = this.toLocalDate()
    return date?.format(dateFormatter)
}