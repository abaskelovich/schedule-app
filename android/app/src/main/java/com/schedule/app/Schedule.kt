package com.schedule.app

import com.google.gson.annotations.SerializedName

data class Schedule(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("time")
    val time: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("created_at")
    val createdAt: String = ""
)
