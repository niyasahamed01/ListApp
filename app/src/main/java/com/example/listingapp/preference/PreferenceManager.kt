package com.example.listingapp.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.GsonBuilder


const val WEATHER_DATA = "weather_data"
const val THEME_MODE = "theme_mode"

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

    fun getThemeMode(): Int {
        return preferences.getInt(THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Saves theme mode into the SharedPreferences.
     *
     * @param themeMode Theme mode to save.
     */
    fun saveThemeMode(themeMode: Int) {
        preferences.edit().putInt(THEME_MODE, themeMode).apply()
    }
}