package com.schedule.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

data class IntentResult(
    val title: String,
    val description: String = "",
    val time: String,
    val date: String
)

data class ParseRequest(
    val input: String
)

class IntentParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val BASE_URL = RetrofitClient.BASE_URL

    suspend fun parse(input: String): Result<IntentResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = ParseRequest(input)
            val json = com.google.gson.Gson().toJson(requestBody)
            val body = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${BASE_URL}api/parse-intent")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Server error: ${response.code}"))
            }

            if (responseBody.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Empty response from server"))
            }

            val result = com.google.gson.Gson().fromJson(responseBody, IntentResult::class.java)
            if (result?.title == null || result.time == null || result.date == null) {
                return@withContext Result.failure(Exception("Invalid data format from server"))
            }
            return@withContext Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("IntentParser", "Failed to parse: ${e.message}")
            return@withContext Result.failure(e)
        }
    }
}
