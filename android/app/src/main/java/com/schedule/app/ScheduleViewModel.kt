package com.schedule.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private val repository = ScheduleRepository()

    private val _schedules = MutableLiveData<List<Schedule>>()
    val schedules: LiveData<List<Schedule>> = _schedules

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _loading.value = true
            repository.getSchedules()
                .onSuccess { _schedules.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun createSchedule(title: String, description: String, time: String, date: String) {
        viewModelScope.launch {
            _loading.value = true
            val schedule = Schedule(title = title, description = description, time = time, date = date)
            repository.createSchedule(schedule)
                .onSuccess { loadSchedules() }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun updateSchedule(id: Long, title: String, description: String, time: String, date: String) {
        viewModelScope.launch {
            _loading.value = true
            val schedule = Schedule(id = id, title = title, description = description, time = time, date = date)
            repository.updateSchedule(id, schedule)
                .onSuccess { loadSchedules() }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            _loading.value = true
            repository.deleteSchedule(id)
                .onSuccess { loadSchedules() }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
