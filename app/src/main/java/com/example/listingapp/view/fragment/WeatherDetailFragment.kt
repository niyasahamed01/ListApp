package com.example.listingapp.view.fragment

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listingapp.databinding.FragmentWeatherBinding
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
import com.example.listingapp.response.Data
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeatherDetailFragment : Fragment() {

    @Inject
    @JvmField
    internal var preferenceManager: PreferenceManager? = null

    var locationManager: LocationManager? = null
    var gpsStatus = false
    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentWeatherBinding.inflate(inflater, container, false)

        val data = preferenceManager?.getModelValue<Data>(WEATHER_DATA)

        binding.tvTemp.text = "Temperature :" + " " + data?.appTemp.toString() + " " + "\u2103"
        binding.tvCity.text = "City :" + " " + data?.cityName
        binding.tvWeather.text = "Weather Report :" + " " + data?.weather?.description

        //checkGpsStatus()
        return binding.root
    }

    private fun checkGpsStatus() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        gpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) {
            binding.tvGps.text = "GPS Is Enabled"
        } else {
            binding.tvGps.text = "GPS Is Disabled"
        }
    }
}