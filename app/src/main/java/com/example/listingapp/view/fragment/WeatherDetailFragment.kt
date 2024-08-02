package com.example.listingapp.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listingapp.databinding.FragmentWeatherBinding
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
import com.example.listingapp.response.WeatherData
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WeatherDetailFragment : Fragment() {

    @Inject
    @JvmField
    internal var preferenceManager: PreferenceManager? = null

    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        setData()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {

        val data = preferenceManager?.getModelValue<WeatherData>(WEATHER_DATA)

        binding.tvTemp.text = "Temperature :" + " " + data?.appTemp.toString() + " " + "\u2103"
        binding.tvCity.text = "City :" + " " + data?.cityName
        binding.tvWeather.text = "Weather Report :" + " " + data?.weather?.description
        // Assuming 'data' is your object containing the 'datetime' field
        val formattedDateTime = data?.datetime?.let { formatDateTime(it) } ?: "Date not available"
        binding.tvDateTime.text = "Date & Time: $formattedDateTime"
        binding.tvSunSet.text = "Sun Set :" + " " + data?.sunset + " " + "PM"
        binding.tvSunRise.text = "Sun Rise :" + " " + data?.sunrise + " " + "AM"
        binding.tvTimeZone.text = "Time Zone :" + " " + data?.timezone
    }


    private fun formatDateTime(dateTime: String): String {
        // Define the formats you expect
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss", // Full format with seconds
            "yyyy-MM-dd HH:mm",    // Format with minutes only
            "yyyy-MM-dd HH",       // Format with hours only
            "yyyy-MM-dd",          // Date only
            "yyyy-MM-dd:HH"        // Custom format for cases with colons
        )

        // Normalize the input dateTime if it has a colon in place of a space
        val normalizedDateTime = dateTime.replace(":", " ", ignoreCase = true)

        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())

                // Parse the date
                val date = inputFormat.parse(normalizedDateTime)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return "Invalid date"
    }
}