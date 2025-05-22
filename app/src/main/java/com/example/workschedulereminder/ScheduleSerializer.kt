package com.example.workschedulereminder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object ScheduleSerializer {
    private val gson = Gson()

    fun serialize(schedules: List<Schedule>): String {
        val serializableSchedules = schedules.map { schedule ->
            SerializableSchedule(
                schedule.id,
                schedule.workDays,
                schedule.startTime.first,
                schedule.startTime.second,
                schedule.endTime.first,
                schedule.endTime.second,
                schedule.validFrom.timeInMillis,
                schedule.isActive
            )
        }
        return gson.toJson(serializableSchedules)
    }

    fun deserialize(json: String): List<Schedule> {
        val type = object : TypeToken<List<SerializableSchedule>>() {}.type
        val serializableSchedules: List<SerializableSchedule> = gson.fromJson(json, type)

        return serializableSchedules.map { serializable ->
            Schedule(
                id = serializable.id,
                workDays = serializable.workDays,
                startTime = Pair(serializable.startHour, serializable.startMinute),
                endTime = Pair(serializable.endHour, serializable.endMinute),
                validFrom = Calendar.getInstance().apply { timeInMillis = serializable.validFromMillis },
                isActive = serializable.isActive
            )
        }
    }

    private data class SerializableSchedule(
        val id: String,
        val workDays: List<Int>,
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int,
        val validFromMillis: Long,
        val isActive: Boolean
    )
}