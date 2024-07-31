package com.example.listingapp.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiCallManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ApiCallPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val API_CALL_COUNT_KEY = "api_call_count"
    private val LAST_CALL_DATE_KEY = "last_call_date"
    private val MAX_CALLS_PER_DAY = 10

    fun canMakeApiCall(): Boolean {
        val currentDate = getCurrentDate()
        val lastCallDate = sharedPreferences.getString(LAST_CALL_DATE_KEY, "")
        val apiCallCount = sharedPreferences.getInt(API_CALL_COUNT_KEY, 0)

        return if (currentDate != lastCallDate) {
            // It's a new day, reset the count
            resetApiCallCount(currentDate)
            true
        } else {
            // Check if the count has exceeded the max limit
            apiCallCount < MAX_CALLS_PER_DAY
        }
    }

    fun recordApiCall() {
        val currentDate = getCurrentDate()
        val apiCallCount = sharedPreferences.getInt(API_CALL_COUNT_KEY, 0) + 1
        editor.putString(LAST_CALL_DATE_KEY, currentDate)
        editor.putInt(API_CALL_COUNT_KEY, apiCallCount)
        editor.apply()
    }

    private fun resetApiCallCount(currentDate: String) {
        editor.putString(LAST_CALL_DATE_KEY, currentDate)
        editor.putInt(API_CALL_COUNT_KEY, 0)
        editor.apply()
    }

    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
}