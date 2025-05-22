package com.example.workschedulereminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import android.view.View

class MainActivity : AppCompatActivity() {
    private lateinit var scheduleList: MutableList<Schedule>
    private lateinit var vacationList: MutableList<VacationDay>
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var vacationAdapter: VacationAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var currentWeekNumber = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

    companion object {
        val RV_SCHEDULES = R.id.rvSchedules
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("WorkSchedulePrefs", Context.MODE_PRIVATE)
        scheduleList = loadSchedules()
        vacationList = loadVacations()
        currentWeekNumber = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

        // Initialize schedule views
        val btnAddSchedule = findViewById<Button>(R.id.btnAddSchedule)
        val rvSchedules = findViewById<RecyclerView>(RV_SCHEDULES)
        val tvCurrentStatus = findViewById<TextView>(R.id.tvCurrentStatus)

        scheduleAdapter = ScheduleAdapter(scheduleList) { schedule ->
            showEditScheduleDialog(schedule)
        }

        rvSchedules.layoutManager = LinearLayoutManager(this)
        rvSchedules.adapter = scheduleAdapter

        btnAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }

        // Initialize vacation views
        val btnAddVacation = findViewById<Button>(R.id.btnAddVacation)
        val rvVacations = findViewById<RecyclerView>(R.id.rvVacations)

        vacationAdapter = VacationAdapter(vacationList) { vacation ->
            showEditVacationDialog(vacation)
        }

        rvVacations.layoutManager = LinearLayoutManager(this)
        rvVacations.adapter = vacationAdapter

        btnAddVacation.setOnClickListener {
            showAddVacationDialog()
        }

        checkAndUpdateCurrentStatus()
        setupDailyReminders()
    }

    private fun showAddScheduleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            val workDays = mutableListOf<Int>()
            if (dialogView.findViewById<CheckBox>(R.id.cbMonday).isChecked) workDays.add(Calendar.MONDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbTuesday).isChecked) workDays.add(Calendar.TUESDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbWednesday).isChecked) workDays.add(Calendar.WEDNESDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbThursday).isChecked) workDays.add(Calendar.THURSDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbFriday).isChecked) workDays.add(Calendar.FRIDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbSaturday).isChecked) workDays.add(Calendar.SATURDAY)
            if (dialogView.findViewById<CheckBox>(R.id.cbSunday).isChecked) workDays.add(Calendar.SUNDAY)

            val startTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerStart)
            val endTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerEnd)
            val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerValidFrom)

            val startTime = Pair(startTimePicker.hour, startTimePicker.minute)
            val endTime = Pair(endTimePicker.hour, endTimePicker.minute)

            val calendar = Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            }

            if (workDays.isEmpty()) {
                Toast.makeText(this, "Please select at least one work day", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newSchedule = Schedule(
                workDays = workDays,
                startTime = startTime,
                endTime = endTime,
                validFrom = calendar
            )

            scheduleList.add(newSchedule)
            saveSchedules()
            scheduleAdapter.notifyDataSetChanged()
            checkAndUpdateCurrentStatus()
            setupDailyReminders()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditScheduleDialog(schedule: Schedule) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Schedule")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete", null)
            .create()

        // Initialize checkboxes with current values
        dialogView.findViewById<CheckBox>(R.id.cbMonday).isChecked = schedule.workDays.contains(Calendar.MONDAY)
        dialogView.findViewById<CheckBox>(R.id.cbTuesday).isChecked = schedule.workDays.contains(Calendar.TUESDAY)
        dialogView.findViewById<CheckBox>(R.id.cbWednesday).isChecked = schedule.workDays.contains(Calendar.WEDNESDAY)
        dialogView.findViewById<CheckBox>(R.id.cbThursday).isChecked = schedule.workDays.contains(Calendar.THURSDAY)
        dialogView.findViewById<CheckBox>(R.id.cbFriday).isChecked = schedule.workDays.contains(Calendar.FRIDAY)
        dialogView.findViewById<CheckBox>(R.id.cbSaturday).isChecked = schedule.workDays.contains(Calendar.SATURDAY)
        dialogView.findViewById<CheckBox>(R.id.cbSunday).isChecked = schedule.workDays.contains(Calendar.SUNDAY)

        // Initialize time pickers
        dialogView.findViewById<TimePicker>(R.id.timePickerStart).apply {
            hour = schedule.startTime.first
            minute = schedule.startTime.second
        }
        dialogView.findViewById<TimePicker>(R.id.timePickerEnd).apply {
            hour = schedule.endTime.first
            minute = schedule.endTime.second
        }

        // Initialize date pickers
        dialogView.findViewById<DatePicker>(R.id.datePickerValidFrom).apply {
            init(
                schedule.validFrom.get(Calendar.YEAR),
                schedule.validFrom.get(Calendar.MONTH),
                schedule.validFrom.get(Calendar.DAY_OF_MONTH),
                null
            )
        }

        // Initialize week pattern spinner
        val spinnerWeekPattern = dialogView.findViewById<Spinner>(R.id.spinnerWeekPattern)
        spinnerWeekPattern.setSelection(schedule.weekPattern.ordinal)

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            saveButton.setOnClickListener {
                // Get updated values
                val workDays = mutableListOf<Int>()
                if (dialogView.findViewById<CheckBox>(R.id.cbMonday).isChecked) workDays.add(Calendar.MONDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbTuesday).isChecked) workDays.add(Calendar.TUESDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbWednesday).isChecked) workDays.add(Calendar.WEDNESDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbThursday).isChecked) workDays.add(Calendar.THURSDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbFriday).isChecked) workDays.add(Calendar.FRIDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbSaturday).isChecked) workDays.add(Calendar.SATURDAY)
                if (dialogView.findViewById<CheckBox>(R.id.cbSunday).isChecked) workDays.add(Calendar.SUNDAY)

                val startTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerStart)
                val endTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerEnd)
                val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerValidFrom)

                val startTime = Pair(startTimePicker.hour, startTimePicker.minute)
                val endTime = Pair(endTimePicker.hour, endTimePicker.minute)
                val weekPattern = WeekPattern.values()[spinnerWeekPattern.selectedItemPosition]

                val calendar = Calendar.getInstance().apply {
                    set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                }

                if (workDays.isEmpty()) {
                    Toast.makeText(this, "Please select at least one work day", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update the schedule
                val updatedSchedule = schedule.copy(
                    workDays = workDays,
                    startTime = startTime,
                    endTime = endTime,
                    validFrom = calendar,
                    weekPattern = weekPattern
                )

                val index = scheduleList.indexOfFirst { it.id == schedule.id }
                if (index != -1) {
                    scheduleList[index] = updatedSchedule
                    saveSchedules()
                    scheduleAdapter.notifyDataSetChanged()
                    checkAndUpdateCurrentStatus()
                    setupDailyReminders()
                    dialog.dismiss()
                }
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Schedule")
                    .setMessage("Are you sure you want to delete this schedule?")
                    .setPositiveButton("Delete") { _, _ ->
                        scheduleList.removeAll { it.id == schedule.id }
                        saveSchedules()
                        scheduleAdapter.notifyDataSetChanged()
                        checkAndUpdateCurrentStatus()
                        setupDailyReminders()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun showAddVacationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_vacation, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Vacation/Leave Day")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        val switchAllDay = dialogView.findViewById<Switch>(R.id.switchAllDay)
        val timeContainer = dialogView.findViewById<LinearLayout>(R.id.timeContainer)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerVacation)

        switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            timeContainer.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                }

                val allDay = switchAllDay.isChecked
                val startTime = if (!allDay) {
                    val startTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerVacationStart)
                    Pair(startTimePicker.hour, startTimePicker.minute)
                } else {
                    null
                }

                val endTime = if (!allDay) {
                    val endTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerVacationEnd)
                    Pair(endTimePicker.hour, endTimePicker.minute)
                } else {
                    null
                }

                val reason = dialogView.findViewById<EditText>(R.id.etVacationReason).text.toString()

                val newVacation = VacationDay(
                    date = calendar,
                    allDay = allDay,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason
                )

                vacationList.add(newVacation)
                saveVacations()
                checkAndUpdateCurrentStatus()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEditVacationDialog(vacation: VacationDay) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_vacation, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Vacation")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete", null)
            .create()

        // Initialize with current values
        dialogView.findViewById<DatePicker>(R.id.datePickerVacation).apply {
            init(
                vacation.date.get(Calendar.YEAR),
                vacation.date.get(Calendar.MONTH),
                vacation.date.get(Calendar.DAY_OF_MONTH),
                null
            )
        }

        dialogView.findViewById<Switch>(R.id.switchAllDay).isChecked = vacation.allDay
        dialogView.findViewById<LinearLayout>(R.id.timeContainer).visibility =
            if (vacation.allDay) View.GONE else View.VISIBLE

        vacation.startTime?.let { (hour, minute) ->
            dialogView.findViewById<TimePicker>(R.id.timePickerVacationStart).apply {
                this.hour = hour
                this.minute = minute
            }
        }

        vacation.endTime?.let { (hour, minute) ->
            dialogView.findViewById<TimePicker>(R.id.timePickerVacationEnd).apply {
                this.hour = hour
                this.minute = minute
            }
        }

        dialogView.findViewById<EditText>(R.id.etVacationReason).setText(vacation.reason)

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            saveButton.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    set(
                        dialogView.findViewById<DatePicker>(R.id.datePickerVacation).year,
                        dialogView.findViewById<DatePicker>(R.id.datePickerVacation).month,
                        dialogView.findViewById<DatePicker>(R.id.datePickerVacation).dayOfMonth
                    )
                }

                val allDay = dialogView.findViewById<Switch>(R.id.switchAllDay).isChecked
                val startTime = if (!allDay) {
                    val startTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerVacationStart)
                    Pair(startTimePicker.hour, startTimePicker.minute)
                } else {
                    null
                }

                val endTime = if (!allDay) {
                    val endTimePicker = dialogView.findViewById<TimePicker>(R.id.timePickerVacationEnd)
                    Pair(endTimePicker.hour, endTimePicker.minute)
                } else {
                    null
                }

                val reason = dialogView.findViewById<EditText>(R.id.etVacationReason).text.toString()

                val updatedVacation = vacation.copy(
                    date = calendar,
                    allDay = allDay,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason
                )

                val index = vacationList.indexOfFirst { it.id == vacation.id }
                if (index != -1) {
                    vacationList[index] = updatedVacation
                    saveVacations()
                    checkAndUpdateCurrentStatus()
                    dialog.dismiss()
                }
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Vacation")
                    .setMessage("Are you sure you want to delete this vacation day?")
                    .setPositiveButton("Delete") { _, _ ->
                        vacationList.removeAll { it.id == vacation.id }
                        saveVacations()
                        checkAndUpdateCurrentStatus()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun saveSchedules() {
        val editor = sharedPreferences.edit()
        editor.putString("schedules", ScheduleSerializer.serialize(scheduleList))
        editor.apply()
    }

    private fun loadSchedules(): MutableList<Schedule> {
        val serialized = sharedPreferences.getString("schedules", null)
        return if (serialized != null) {
            ScheduleSerializer.deserialize(serialized).toMutableList()
        } else {
            mutableListOf()
        }
    }

    private fun saveVacations() {
        val editor = sharedPreferences.edit()
        editor.putString("vacations", VacationSerializer.serialize(vacationList))
        editor.apply()
    }

    private fun loadVacations(): MutableList<VacationDay> {
        val serialized = sharedPreferences.getString("vacations", null)
        return if (serialized != null) {
            VacationSerializer.deserialize(serialized).toMutableList()
        } else {
            mutableListOf()
        }
    }

    private fun isOnVacation(day: Int, hour: Int, minute: Int): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, day)
        }

        return vacationList.any { vacation ->
            val sameDate = vacation.date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    vacation.date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    vacation.date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            if (!sameDate) false else {
                if (vacation.allDay) true else {
                    val (startHour, startMinute) = vacation.startTime ?: return@any false
                    val (endHour, endMinute) = vacation.endTime ?: return@any false

                    (hour > startHour || (hour == startHour && minute >= startMinute)) &&
                            (hour < endHour || (hour == endHour && minute <= endMinute))
                }
            }
        }
    }

    private fun checkAndUpdateCurrentStatus() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

        // Check if today is a vacation day
        if (isOnVacation(today, currentHour, currentMinute)) {
            updateStatusView("Status: On Vacation - Enjoy your day off!", android.R.color.holo_green_dark)
            return
        }

        val activeSchedule = scheduleList.find { schedule ->
            schedule.isActive &&
                    schedule.workDays.contains(today) &&
                    Calendar.getInstance().after(schedule.validFrom) &&
                    (schedule.validTo == null || Calendar.getInstance().before(schedule.validTo)) &&
                    matchesWeekPattern(schedule.weekPattern, currentWeek)
        }

        if (activeSchedule != null) {
            val (startHour, startMinute) = activeSchedule.startTime
            val (endHour, endMinute) = activeSchedule.endTime

            val isWorkTime = (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) &&
                    (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute))

            if (isWorkTime) {
                updateStatusView("Status: WORK TIME - Hurry up!", android.R.color.holo_red_dark)
            } else {
                updateStatusView(
                    "Status: Today is your work day (${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)})",
                    android.R.color.holo_blue_dark
                )
            }
        } else {
            updateStatusView("Status: Today is your OFF day - Relax!", android.R.color.holo_green_dark)
        }
    }

    private fun matchesWeekPattern(pattern: WeekPattern, currentWeek: Int): Boolean {
        return when (pattern) {
            WeekPattern.ALL_WEEKS -> true
            WeekPattern.ODD_WEEKS -> currentWeek % 2 == 1
            WeekPattern.EVEN_WEEKS -> currentWeek % 2 == 0
            WeekPattern.WEEK_1 -> currentWeek % 4 == 1
            WeekPattern.WEEK_2 -> currentWeek % 4 == 2
            WeekPattern.WEEK_3 -> currentWeek % 4 == 3
            WeekPattern.WEEK_4 -> currentWeek % 4 == 0
        }
    }

    private fun updateStatusView(text: String, colorRes: Int) {
        val tvCurrentStatus = findViewById<TextView>(R.id.tvCurrentStatus)
        tvCurrentStatus.text = text
        tvCurrentStatus.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    private fun setupDailyReminders() {
        cancelAllAlarms()

        scheduleList.forEach { schedule ->
            if (schedule.isActive) {
                setAlarmForDay(
                    schedule.workDays,
                    schedule.startTime.first,
                    schedule.startTime.second,
                    "Work starts at ${formatTime(schedule.startTime.first, schedule.startTime.second)}!",
                    "work_start_${schedule.id}"
                )

                setAlarmForDay(
                    schedule.workDays,
                    schedule.endTime.first,
                    schedule.endTime.second,
                    "Work ends at ${formatTime(schedule.endTime.first, schedule.endTime.second)}!",
                    "work_end_${schedule.id}"
                )
            }
        }

        setDailyStatusCheckAlarm()
    }

    private fun setAlarmForDay(days: List<Int>, hour: Int, minute: Int, message: String, requestCode: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set alarm for each selected day
        days.forEach { day ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                // If the time has already passed today, set it for next week
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DATE, 7)
                }
            }

            // Check if this day is a vacation day
            if (!isOnVacation(day, hour, minute)) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7, // Repeat weekly
                    pendingIntent
                )
            }
        }
    }

    private fun setDailyStatusCheckAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyStatusReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            "daily_status".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAllAlarms() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        scheduleList.forEach { schedule ->
            val startIntent = Intent(this, AlarmReceiver::class.java)
            val startPendingIntent = PendingIntent.getBroadcast(
                this,
                "work_start_${schedule.id}".hashCode(),
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(startPendingIntent)

            val endIntent = Intent(this, AlarmReceiver::class.java)
            val endPendingIntent = PendingIntent.getBroadcast(
                this,
                "work_end_${schedule.id}".hashCode(),
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(endPendingIntent)
        }

        val statusIntent = Intent(this, DailyStatusReceiver::class.java)
        val statusPendingIntent = PendingIntent.getBroadcast(
            this,
            "daily_status".hashCode(),
            statusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(statusPendingIntent)
    }
}