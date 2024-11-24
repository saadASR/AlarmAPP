package com.example.alarmapp

data class AlarmItem(
    var id: Long = 0,
    var time: String,
    var isActive: Boolean = true,
    var hour: Int = 0,
    var minute: Int = 0
)