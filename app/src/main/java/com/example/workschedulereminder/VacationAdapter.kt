package com.example.workschedulereminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class VacationAdapter(
    private val vacations: List<VacationDay>,
    private val onItemClick: (VacationDay) -> Unit
) : RecyclerView.Adapter<VacationAdapter.VacationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VacationViewHolder(view)
    }

    override fun onBindViewHolder(holder: VacationViewHolder, position: Int) {
        val vacation = vacations[position]
        holder.bind(vacation)
        holder.itemView.setOnClickListener { onItemClick(vacation) }
    }

    override fun getItemCount() = vacations.size

    class VacationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(vacation: VacationDay) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            text1.text = dateFormat.format(vacation.date.time)

            if (vacation.allDay) {
                text2.text = "All day" + if (vacation.reason.isNotEmpty()) " - ${vacation.reason}" else ""
            } else {
                val startTime = formatTime(vacation.startTime!!)
                val endTime = formatTime(vacation.endTime!!)
                text2.text = "$startTime - $endTime" + if (vacation.reason.isNotEmpty()) " - ${vacation.reason}" else ""
            }
        }

        private fun formatTime(time: Pair<Int, Int>): String {
            return String.format("%02d:%02d", time.first, time.second)
        }
    }
}