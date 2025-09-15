package com.example.wakemeup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wakemeup.databinding.ItemAlarmBinding

class AlarmAdapter(
    private val onToggleAlarm: (LocationAlarm) -> Unit,
    private val onDeleteAlarm: (LocationAlarm) -> Unit,
    private val onEditAlarm: (LocationAlarm) -> Unit
) : ListAdapter<LocationAlarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: LocationAlarm) {
            binding.apply {
                textViewAlarmName.text = alarm.name
                textViewLocation.text = "Lat: ${String.format("%.4f", alarm.latitude)}, Lng: ${String.format("%.4f", alarm.longitude)}"
                textViewRadius.text = "Rayon: ${alarm.radius.toInt()}m"

                switchActive.isChecked = alarm.isActive
                switchActive.setOnCheckedChangeListener { _, _ ->
                    onToggleAlarm(alarm)
                }

                buttonEdit.setOnClickListener {
                    onEditAlarm(alarm)
                }

                buttonDelete.setOnClickListener {
                    onDeleteAlarm(alarm)
                }

                // Mise à jour visuelle selon l'état
                root.alpha = if (alarm.isActive) 1.0f else 0.6f
            }
        }
    }
}

class AlarmDiffCallback : DiffUtil.ItemCallback<LocationAlarm>() {
    override fun areItemsTheSame(oldItem: LocationAlarm, newItem: LocationAlarm): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocationAlarm, newItem: LocationAlarm): Boolean {
        return oldItem == newItem
    }
}
