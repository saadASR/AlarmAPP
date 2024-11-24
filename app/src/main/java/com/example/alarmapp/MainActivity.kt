package com.example.alarmapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlarmAdapter
    private lateinit var alarmScheduler: AlarmScheduler
    private val alarmsList = mutableListOf<AlarmItem>()

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmScheduler = AlarmScheduler(this)

        // Request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestAlarmPermission()
        }

        setupRecyclerView()
        setupAddAlarmButton()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.alarmsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AlarmAdapter(
            alarmsList,
            onToggleListener = { position, isActive -> toggleAlarm(position, isActive) },
            onDeleteListener = { position -> deleteAlarm(position) },
            onEditListener = { position -> showTimePickerDialog(position) }
        )
        recyclerView.adapter = adapter
    }

    private fun setupAddAlarmButton() {
        findViewById<FloatingActionButton>(R.id.fabAddAlarm).setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }
    }

    private fun showTimePickerDialog(position: Int = -1) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                if (position == -1) {
                    addAlarm(time, hourOfDay, minute)
                } else {
                    editAlarm(position, time, hourOfDay, minute)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun addAlarm(time: String, hour: Int, minute: Int) {
        val alarm = AlarmItem(
            id = System.currentTimeMillis(),
            time = time,
            hour = hour,
            minute = minute
        )
        alarmsList.add(alarm)
        adapter.notifyItemInserted(alarmsList.size - 1)
        scheduleAlarm(alarm)
    }

    private fun editAlarm(position: Int, time: String, hour: Int, minute: Int) {
        val alarm = alarmsList[position]
        alarmScheduler.cancelAlarm(alarm)

        alarm.time = time
        alarm.hour = hour
        alarm.minute = minute

        scheduleAlarm(alarm)
        adapter.notifyItemChanged(position)
    }

    private fun deleteAlarm(position: Int) {
        val alarm = alarmsList[position]
        alarmScheduler.cancelAlarm(alarm)
        alarmsList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun toggleAlarm(position: Int, isActive: Boolean) {
        val alarm = alarmsList[position]
        alarm.isActive = isActive

        if (isActive) {
            scheduleAlarm(alarm)
        } else {
            alarmScheduler.cancelAlarm(alarm)
            Toast.makeText(this, "Alarm deactivated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleAlarm(alarm: AlarmItem) {
        if (alarm.isActive) {
            try {
                alarmScheduler.scheduleAlarm(alarm)
                Toast.makeText(
                    this,
                    "Alarm scheduled for ${alarm.time}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: SecurityException) {
                Toast.makeText(
                    this,
                    "Please grant permission to schedule exact alarms",
                    Toast.LENGTH_LONG
                ).show()
                requestAlarmPermission()
            }
        }
    }
}