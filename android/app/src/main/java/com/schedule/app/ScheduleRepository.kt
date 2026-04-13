package com.schedule.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository {
    private val api = RetrofitClient.apiService

    suspend fun getSchedules(): Result<List<Schedule>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSchedules()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSchedule(schedule: Schedule): Result<Schedule> = withContext(Dispatchers.IO) {
        try {
            val response = api.createSchedule(schedule)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSchedule(id: Long, schedule: Schedule): Result<Schedule> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSchedule(id, schedule)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSchedule(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
