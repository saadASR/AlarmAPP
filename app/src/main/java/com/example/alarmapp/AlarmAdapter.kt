package com.example.alarmapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarms: List<AlarmItem>,
    private val onToggleListener: (Int, Boolean) -> Unit,
    private val onDeleteListener: (Int) -> Unit,
    private val onEditListener: (Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.textTime)
        val toggleSwitch: Switch = view.findViewById(R.id.switchAlarm)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonDelete)
        val editButton: ImageButton = view.findViewById(R.id.buttonEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeText.text = alarm.time
        holder.toggleSwitch.isChecked = alarm.isActive

        holder.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            onToggleListener(position, isChecked)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteListener(position)
        }

        holder.editButton.setOnClickListener {
            onEditListener(position)
        }
    }

    override fun getItemCount() = alarms.size
}
