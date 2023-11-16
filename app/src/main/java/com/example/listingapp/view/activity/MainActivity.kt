package com.example.listingapp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.example.listingapp.R
import com.example.listingapp.databinding.ActivityMainBinding
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
import com.example.listingapp.util.NetworkResult
import com.example.listingapp.viewmodel.EmployeeViewModel
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PERMISSION_ID = 1010

    private val mainViewModel by viewModels<EmployeeViewModel>()

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
    }
    private val navController by lazy {
        navHostFragment.navController
    }

    @Inject
    @JvmField
    internal var preferenceManager: PreferenceManager? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        onClickListener()
        //setObserver()
        requestPermission()
        getLastLocation()

    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        newLocationData()
                    } else {
                        Log.d("Debug:", "Your Location:" + location.longitude)
                        mainViewModel.getWeather(
                            location.latitude,
                            location.longitude
                        )
                        setObserver()
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermission()
        }
    }

    private fun newLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:", "your last last location: " + lastLocation.longitude.toString())

            mainViewModel.getWeather(
                lastLocation.latitude,
                lastLocation.longitude
            )
            setObserver()
        }
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug:", "You have the Permission")
            }
        }
    }

    private fun onClickListener() {
        binding.toolbarActionbar.setOnClickListener {
            navController.navigate(R.id.weatherFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        mainViewModel.response.observe(this) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    response.data.let { res ->
                        if (res?.count == 1) {
                            res.data.let { it1 ->
                                if (it1 != null) {
                                    for (item in it1) {
                                        preferenceManager?.storeModelValue(item, WEATHER_DATA)
                                        binding.city.text = "${item.cityName}"
                                        binding.temp.text = item.appTemp.toString() + " " + "\u2103"
                                        binding.cloud.text = "${item.weather?.description}"
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(
                        this,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NetworkResult.Loading -> {
                }
            }
        }
    }
}