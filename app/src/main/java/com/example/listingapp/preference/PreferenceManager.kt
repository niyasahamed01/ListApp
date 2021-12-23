package com.example.listingapp.preference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder


const val WEATHER_DATA = "weather_data"

/**
 * Preference manager class to store and retrieve data
 * @param context is required to the shared Preference
 * */
class PreferenceManager(context: Context) {
    val preferences: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    fun <T> storeModelValue(objectData: T, key: String) {
        // Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(objectData)
        // Save that String in SharedPreferences
        preferences.edit().putString(key, jsonString).apply()
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> getModelValue(key: String): T {
        val value = preferences.getString(key, null)
        return GsonBuilder().create().fromJson(value, T::class.java)
    }
}