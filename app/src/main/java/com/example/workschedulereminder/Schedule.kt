package com.example.workschedulereminder

import java.util.*

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val workDays: List<Int>, // Calendar day constants
    val startTime: Pair<Int, Int>, // hour, minute
    val endTime: Pair<Int, Int>, // hour, minute
    val validFrom: Calendar,
    val validTo: Calendar? = null, // Optional end date for schedule
    val weekPattern: WeekPattern = WeekPattern.ALL_WEEKS, // For different weekly schedules
    var isActive: Boolean = true
)

enum class WeekPattern {
    ALL_WEEKS,
    ODD_WEEKS,  // Week numbers 1, 3, 5...
    EVEN_WEEKS, // Week numbers 2, 4, 6...
    WEEK_1,     // Specific week numbers
    WEEK_2,
    WEEK_3,
    WEEK_4
}

data class VacationDay(
    val id: String = UUID.randomUUID().toString(),
    val date: Calendar,
    val allDay: Boolean = true,
    val startTime: Pair<Int, Int>? = null, // For partial day vacations
    val endTime: Pair<Int, Int>? = null,
    val reason: String = ""
)