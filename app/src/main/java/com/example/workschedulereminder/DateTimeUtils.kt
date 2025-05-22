package com.example.workschedulereminder



object DateTimeUtils {
    fun formatTime(time: Pair<Int, Int>): String {
        return String.format("%02d:%02d", time.first, time.second)
    }
}