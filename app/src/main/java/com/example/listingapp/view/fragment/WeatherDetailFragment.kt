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
        binding.tvDateTime.text = "Date & Time: ${data?.datetime?.let { formatDateTime(it) }}"
        binding.tvSunSet.text = "Sun Set :" + " " + data?.sunset + " " + "PM"
        binding.tvSunRise.text = "Sun Rise :" + " " + data?.sunrise + " " + "AM"
        binding.tvTimeZone.text = "Time Zone :" + " " + data?.timezone
    }

    private fun formatDateTime(dateTime: String): String {

        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        )

        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(dateTime)
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