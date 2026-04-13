package com.schedule.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.app.databinding.ActivityMainBinding
import com.schedule.app.databinding.DialogScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            onItemClick = { schedule -> showEditDialog(schedule) },
            onItemLongClick = { schedule -> showDeleteDialog(schedule) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.schedules.observe(this) { schedules ->
            adapter.submitList(schedules)
            binding.emptyView.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogScheduleBinding.inflate(layoutInflater)
        var selectedTime = ""
        var selectedDate = ""

        dialogBinding.etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                dialogBinding.etTime.setText(selectedTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        dialogBinding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                dialogBinding.etDate.setText(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.add_schedule)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val description = dialogBinding.etDescription.text.toString()
                if (title.isNotBlank() && selectedTime.isNotBlank() && selectedDate.isNotBlank()) {
                    viewModel.createSchedule(title, description, selectedTime, selectedDate)
                } else {
                    Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditDialog(schedule: Schedule) {
        val dialogBinding = DialogScheduleBinding.inflate(layoutInflater)
        dialogBinding.etTitle.setText(schedule.title)
        dialogBinding.etDescription.setText(schedule.description)
        dialogBinding.etTime.setText(schedule.time)
        dialogBinding.etDate.setText(schedule.date)
        var selectedTime = schedule.time
        var selectedDate = schedule.date

        dialogBinding.etTime.setOnClickListener {
            val parts = schedule.time.split(":")
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                dialogBinding.etTime.setText(selectedTime)
            }, parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0, true).show()
        }

        dialogBinding.etDate.setOnClickListener {
            val parts = schedule.date.split("-")
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                dialogBinding.etDate.setText(selectedDate)
            }, parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1,
                parts.getOrNull(2)?.toIntOrNull() ?: 1).show()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_schedule)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val description = dialogBinding.etDescription.text.toString()
                if (title.isNotBlank() && selectedTime.isNotBlank() && selectedDate.isNotBlank()) {
                    viewModel.updateSchedule(schedule.id, title, description, selectedTime, selectedDate)
                } else {
                    Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteDialog(schedule: Schedule) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Delete \"${schedule.title}\"?")
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSchedule(schedule.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
