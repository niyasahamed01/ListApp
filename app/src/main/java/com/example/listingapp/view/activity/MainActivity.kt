package com.example.listingapp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

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

    private var locationManager: LocationManager? = null

    private var apiCallJob: Job? = null

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
        // Cancel the notification
        cancelNotification()
        // Mark notification as shown
        preferenceManager?.setNotificationShown(true)
    }

    override fun onResume() {
        super.onResume()
        if (preferenceManager?.hasNotificationBeenShown() == true) {
            // Reset the flag to allow fetching weather data next time
            preferenceManager?.setNotificationShown(false)
        } else {
            // Fetch weather data if notification wasn't clicked
            fetchWeatherData()
        }
    }


    override fun onPause() {
        super.onPause()
        locationManager?.removeLocationUpdates()
        apiCallJob?.cancel() // Cancel API call when the activity is paused
    }

    private fun initComponents() {
        NotificationHelper.initialize(this)
        setupWorkManager()
        checkAndRequestNotificationPermission()
        requestPermissions()
        locationManager = LocationManager(this) { latitude, longitude ->
            apiCallJob = CoroutineScope(Dispatchers.IO).launch {
                employeeViewModel.getWeather(latitude, longitude)
            }
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
        val navigateToFragment = intent.getStringExtra("navigate_to_fragment")
        val notificationClicked = intent.getBooleanExtra("notification_clicked", false)
        if (navigateToFragment == "WeatherFragment") {
            navController.navigate(R.id.weatherFragment)
        }
        if (notificationClicked) {
            preferenceManager?.setNotificationShown(true)
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
            CoroutineScope(Dispatchers.IO).launch {
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

        if (response.message == "Too many requests") {
            handleTooManyRequests(response)
        }
    }

    private fun handleTooManyRequests(response: NetworkResult.Error<WeatherResponse?>) {
        val retryAfter = response.headers?.get("Retry-After")?.toIntOrNull()
        if (retryAfter != null) {
            retryAfterDelay(retryAfter)
        } else {
            retryWithExponentialBackoff()
        }
        showErrorMessage("Too many requests. Please try again later.")
    }

    private fun retryAfterDelay(seconds: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            // Retry the API request
            fetchWeatherData()
        }, seconds * 1000L)
    }

    private fun retryWithExponentialBackoff() {
        var retryCount = 0
        val maxRetries = 5
        val baseDelay = 1000L // 1 second

        fun retry() {
            if (retryCount < maxRetries) {
                val delay = baseDelay * (2.0.pow(retryCount.toDouble())).toLong()
                Handler(Looper.getMainLooper()).postDelayed({
                    // Retry the API request
                    fetchWeatherData()
                    retryCount++
                    retry()
                }, delay)
            } else {
                showErrorMessage("Please try again later.")
            }
        }

        retry()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
            locationManager?.requestLocationUpdates()
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
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
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

    private fun cancelNotification() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NotificationHelper.NOTIFICATION_ID)
    }
}