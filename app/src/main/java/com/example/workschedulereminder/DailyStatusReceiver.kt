package com.example.workschedulereminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import android.os.Build
import java.util.*

class DailyStatusReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "work_schedule_channel"
        private const val CHANNEL_NAME = "Work Schedule Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val message = getTodaysStatusMessage(context)

        showNotification(context, message)
    }

    private fun getTodaysStatusMessage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("WorkSchedulePrefs", Context.MODE_PRIVATE)
        val serialized = sharedPreferences.getString("schedules", null)
        val schedules = serialized?.let { ScheduleSerializer.deserialize(it) } ?: emptyList()

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val currentSchedule = schedules.find { schedule ->
            schedule.isActive && schedule.workDays.contains(today)
        }

        return if (currentSchedule != null) {
            "Today is a WORK day (${formatTime(currentSchedule.startTime)} - ${formatTime(currentSchedule.endTime)})"
        } else {
            "Today is your OFF day - Enjoy!"
        }
    }

    private fun showNotification(context: Context, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Today's Work Status")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Work Schedule Notifications"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(time: Pair<Int, Int>): String {
        return String.format("%02d:%02d", time.first, time.second)
    }
}