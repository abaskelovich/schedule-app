package com.schedule.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.app.databinding.ActivityMainBinding
import com.schedule.app.databinding.DialogScheduleBinding
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter
    private var isInputExpanded = false

    private val intentParser = IntentParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        setupQuickInput()
        setupBottomNav()
        setupDayView()
        observeViewModel()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.dayViewLayout.root.visibility = View.GONE
                    binding.emptyView.visibility = if (viewModel.schedules.value?.isEmpty() == true) View.VISIBLE else View.GONE
                    true
                }
                R.id.nav_day -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.dayViewLayout.root.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    updateDayView()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDayView() {
        val hourContainer = binding.dayViewLayout.hourContainer
        val inflater = layoutInflater
        
        for (hour in 8..22) {
            val hourView = inflater.inflate(android.R.layout.simple_list_item_1, hourContainer, false)
            val textView = hourView.findViewById<android.widget.TextView>(android.R.id.text1)
            textView.text = String.format("%02d:00", hour)
            textView.setPadding(0, 32, 0, 32)
            
            val hourWrapper = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                tag = hour
            }
            
            hourWrapper.addView(hourView)
            
            val line = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setBackgroundColor(android.graphics.Color.LTGRAY)
            }
            hourWrapper.addView(line)
            
            hourContainer.addView(hourWrapper)
        }
    }

    private fun updateDayView() {
        val today = java.time.LocalDate.now().toString()
        val schedules = viewModel.schedules.value ?: emptyList()
        val todaySchedules = schedules.filter { it.date == today }
        
        binding.dayViewLayout.currentDayTitle.text = "Today: $today"
        
        for (i in 0 until binding.dayViewLayout.hourContainer.childCount) {
            val wrapper = binding.dayViewLayout.hourContainer.getChildAt(i) as? android.widget.LinearLayout ?: continue
            if (wrapper.tag is Int) {
                // Удаляем старые события (индексы выше 1, так как 0 - текст часа, 1 - линия)
                while (wrapper.childCount > 2) {
                    wrapper.removeViewAt(2)
                }
                
                val hour = wrapper.tag as Int
                todaySchedules.filter { 
                    val h = it.time.split(":").firstOrNull()?.toIntOrNull()
                    h == hour
                }.forEach { schedule ->
                    val eventView = android.widget.TextView(this).apply {
                        text = "${schedule.time} - ${schedule.title}"
                        setBackgroundColor(android.graphics.Color.parseColor("#E1BEE7"))
                        setPadding(16, 8, 16, 8)
                        setTextColor(android.graphics.Color.BLACK)
                        val lp = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.setMargins(64, 4, 16, 4)
                        layoutParams = lp
                    }
                    wrapper.addView(eventView)
                }
            }
        }
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
            if (isInputExpanded) {
                collapseInput()
            } else {
                expandInput()
            }
        }
    }

    private fun setupQuickInput() {
        binding.btnSend.setOnClickListener {
            val input = binding.etQuickInput.text.toString()
            if (input.isNotBlank()) {
                processQuickInput(input)
            }
        }

        binding.etQuickInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val input = binding.etQuickInput.text.toString()
                if (input.isNotBlank()) {
                    processQuickInput(input)
                }
                true
            } else {
                false
            }
        }
    }

    private fun expandInput() {
        isInputExpanded = true
        binding.inputCard.visibility = View.VISIBLE
        binding.inputCard.alpha = 0f
        binding.etQuickInput.requestFocus()
        binding.fab.visibility = View.INVISIBLE

        val fadeIn = ObjectAnimator.ofFloat(binding.inputCard, View.ALPHA, 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(binding.inputCard, View.TRANSLATION_Y, 50f, 0f)

        AnimatorSet().apply {
            playTogether(fadeIn, slideUp)
            duration = 250
            start()
        }
    }

    private fun collapseInput() {
        isInputExpanded = false

        val fadeOut = ObjectAnimator.ofFloat(binding.inputCard, View.ALPHA, 1f, 0f)
        val slideDown = ObjectAnimator.ofFloat(binding.inputCard, View.TRANSLATION_Y, 0f, 50f)

        AnimatorSet().apply {
            playTogether(fadeOut, slideDown)
            duration = 200
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    binding.inputCard.visibility = View.GONE
                    binding.etQuickInput.text?.clear()
                    binding.fab.visibility = View.VISIBLE
                    binding.inputCard.translationY = 0f
                }
            })
            start()
        }
    }

    private var isProcessing = false

    private fun processQuickInput(input: String) {
        if (isProcessing) return
        isProcessing = true
        
        binding.progressBar.isVisible = true
        binding.btnSend.isEnabled = false

        lifecycleScope.launch {
            intentParser.parse(input)
                .onSuccess { result ->
                    viewModel.createSchedule(
                        result.title,
                        result.description,
                        result.time,
                        result.date
                    )
                    collapseInput()
                    Toast.makeText(this@MainActivity, "Schedule created: ${result.title}", Toast.LENGTH_SHORT).show()
                }
                .onFailure { e ->
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            binding.progressBar.isVisible = false
            binding.btnSend.isEnabled = true
            isProcessing = false
        }
    }

    private fun observeViewModel() {
        viewModel.schedules.observe(this) { schedules ->
            adapter.submitList(schedules)
            
            val selectedId = binding.bottomNav.selectedItemId
            if (selectedId == R.id.nav_list || selectedId == 0) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.dayViewLayout.root.visibility = View.GONE
                binding.emptyView.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE
            } else if (selectedId == R.id.nav_day) {
                updateDayView()
            }
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
