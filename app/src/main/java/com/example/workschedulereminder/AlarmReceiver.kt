package com.example.workschedulereminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: return

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, "work_schedule_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Work Schedule Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(message.hashCode(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Work Schedule Reminders"
        val descriptionText = "Notifications for work schedule reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("work_schedule_channel", name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}