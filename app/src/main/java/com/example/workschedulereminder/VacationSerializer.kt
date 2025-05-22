package com.example.workschedulereminder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object VacationSerializer {
    private val gson = Gson()

    fun serialize(vacations: List<VacationDay>): String {
        val serializableVacations = vacations.map { vacation ->
            SerializableVacation(
                vacation.id,
                vacation.date.timeInMillis,
                vacation.allDay,
                vacation.startTime?.first,
                vacation.startTime?.second,
                vacation.endTime?.first,
                vacation.endTime?.second,
                vacation.reason
            )
        }
        return gson.toJson(serializableVacations)
    }

    fun deserialize(json: String): List<VacationDay> {
        val type = object : TypeToken<List<SerializableVacation>>() {}.type
        val serializableVacations: List<SerializableVacation> = gson.fromJson(json, type)

        return serializableVacations.map { serializable ->
            VacationDay(
                id = serializable.id,
                date = Calendar.getInstance().apply { timeInMillis = serializable.dateMillis },
                allDay = serializable.allDay,
                startTime = if (serializable.startHour != null && serializable.startMinute != null) {
                    Pair(serializable.startHour, serializable.startMinute)
                } else {
                    null
                },
                endTime = if (serializable.endHour != null && serializable.endMinute != null) {
                    Pair(serializable.endHour, serializable.endMinute)
                } else {
                    null
                },
                reason = serializable.reason
            )
        }
    }

    private data class SerializableVacation(
        val id: String,
        val dateMillis: Long,
        val allDay: Boolean,
        val startHour: Int?,
        val startMinute: Int?,
        val endHour: Int?,
        val endMinute: Int?,
        val reason: String
    )
}