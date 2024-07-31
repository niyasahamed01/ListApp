package com.example.listingapp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.listingapp.R
import com.example.listingapp.databinding.ActivityMainBinding
import com.example.listingapp.other.MyWorker
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
import com.example.listingapp.response.WeatherResponse
import com.example.listingapp.util.ApiCallManager
import com.example.listingapp.util.LocationManager
import com.example.listingapp.util.NetworkResult
import com.example.listingapp.util.NotificationHelper
import com.example.listingapp.viewmodel.EmployeeViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val employeeViewModel: EmployeeViewModel by viewModels()
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment).navController
    }

    @Inject
    @JvmField
    internal var preferenceManager: PreferenceManager? = null

    private lateinit var apiCallManager: ApiCallManager
    private var locationManager: LocationManager? = null

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 101
    }

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initComponents()
        handleIntent(intent)
        setupObservers()
        setupClickListeners()
        applyTheme()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchWeatherData()
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeLocationUpdates()
    }

    private fun initComponents() {
        apiCallManager = ApiCallManager(this)
        NotificationHelper.initialize(this)
        setupWorkManager()
        checkAndRequestNotificationPermission()
        requestPermissions()
        locationManager = LocationManager(this) { latitude, longitude ->
            employeeViewModel.getWeather(latitude, longitude)
        }
    }

    private fun setupWorkManager() {
        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        )
    }

    private fun requestPermissions() {
        requestLocationPermission()
    }

    private fun handleIntent(intent: Intent) {
        intent.getStringExtra("navigate_to_fragment")?.let {
            if (it == "WeatherFragment") {
                navController.navigate(R.id.weatherFragment)
            }
        }
    }

    private fun setupObservers() {
        employeeViewModel.response.observe(this) { response ->
            when (response) {
                is NetworkResult.Success -> handleSuccess(response.data)
                is NetworkResult.Error -> handleApiError(response)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccess(data: WeatherResponse?) {
        binding.progress.visibility = View.GONE
        data?.data?.firstOrNull()?.let { weatherData ->
            preferenceManager?.storeModelValue(weatherData, WEATHER_DATA)
            binding.city.text = weatherData.cityName
            binding.temp.text = "${weatherData.appTemp} \u2103"
            binding.cloud.text = weatherData.weather?.description
            // Show notification with weather data
            CoroutineScope(Dispatchers.Main).launch {
                NotificationHelper.showNotification(this@MainActivity, weatherData)
            }
        } ?: run {
            binding.progress.visibility = View.GONE
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleApiError(response: NetworkResult.Error<WeatherResponse?>) {
        binding.progress.visibility = View.GONE
        val errorMessage = when (response.message) {
            "Your API key does not allow access to this endpoint." -> "Invalid API key or insufficient permissions."
            else -> "An error occurred: ${response.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        binding.toolbarActionbar.setOnClickListener {
            navController.navigate(R.id.weatherFragment)
        }
        binding.mode.setOnClickListener {
            showThemeDialog()
        }
    }

    private fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            when (preferenceManager?.getThemeMode()) {
                AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light Theme", "Dark Theme", "System Default Theme")
        val checkedItem = when (preferenceManager?.getThemeMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_theme_text))
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                preferenceManager?.saveThemeMode(selectedMode)
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                recreate()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun fetchWeatherData() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (apiCallManager.canMakeApiCall()) {
                locationManager?.requestLocationUpdates()
            } else {
                Toast.makeText(this, "API call limit reached for today", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationSettings()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkLocationSettings()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required for this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkLocationSettings() {
        locationManager?.let { lm ->
            val builder = LocationSettingsRequest.Builder().addLocationRequest(lm.locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                lm.requestLocationUpdates()
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        sendEx.printStackTrace()
                    }
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission is required.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
}