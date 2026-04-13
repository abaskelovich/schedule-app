package com.schedule.app

import retrofit2.Response
import retrofit2.http.*

interface ScheduleApiService {
    @GET("api/schedules")
    suspend fun getSchedules(): Response<List<Schedule>>

    @GET("api/schedules/{id}")
    suspend fun getSchedule(@Path("id") id: Long): Response<Schedule>

    @POST("api/schedules")
    suspend fun createSchedule(@Body schedule: Schedule): Response<Schedule>

    @PUT("api/schedules/{id}")
    suspend fun updateSchedule(@Path("id") id: Long, @Body schedule: Schedule): Response<Schedule>

    @DELETE("api/schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: Long): Response<Schedule>
}
