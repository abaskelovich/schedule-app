package com.schedule.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(
    private val onItemClick: (Schedule) -> Unit,
    private val onItemLongClick: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: android.widget.TextView = itemView.findViewById(R.id.tvTime)
        private val tvDate: android.widget.TextView = itemView.findViewById(R.id.tvDate)
        private val tvTitle: android.widget.TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: android.widget.TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(schedule: Schedule) {
            tvTime.text = schedule.time
            tvDate.text = schedule.date
            tvTitle.text = schedule.title
            tvDescription.text = schedule.description
            tvDescription.visibility = if (schedule.description.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE

            itemView.setOnClickListener { onItemClick(schedule) }
            itemView.setOnLongClickListener {
                onItemLongClick(schedule)
                true
            }
        }
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }
}
