package com.example.workschedulereminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.example.workschedulereminder.DateTimeUtils

class ScheduleAdapter(
    private val schedules: List<Schedule>,
    private val onItemClick: (Schedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.bind(schedule)
        holder.itemView.setOnClickListener { onItemClick(schedule) }
    }

    override fun getItemCount() = schedules.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(schedule: Schedule) {
            val days = schedule.workDays.map { day ->
                when (day) {
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    Calendar.SATURDAY -> "Sat"
                    Calendar.SUNDAY -> "Sun"
                    else -> ""
                }
            }.joinToString(", ")

            // Corrected: Using DateTimeUtils.formatTime()
            val timeRange = "${DateTimeUtils.formatTime(schedule.startTime)} - ${DateTimeUtils.formatTime(schedule.endTime)}"

            val validFrom = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(schedule.validFrom.time)

            val validTo = schedule.validTo?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.time)
            } ?: "No end date"

            val weekPattern = when (schedule.weekPattern) {
                WeekPattern.ALL_WEEKS -> "All weeks"
                WeekPattern.ODD_WEEKS -> "Odd weeks"
                WeekPattern.EVEN_WEEKS -> "Even weeks"
                WeekPattern.WEEK_1 -> "Week 1"
                WeekPattern.WEEK_2 -> "Week 2"
                WeekPattern.WEEK_3 -> "Week 3"
                WeekPattern.WEEK_4 -> "Week 4"
            }

            text1.text = "Days: $days | Time: $timeRange | $weekPattern"
            text2.text =
                "Valid: $validFrom to $validTo | ${if (schedule.isActive) "Active" else "Inactive"}"
        }
    }
}