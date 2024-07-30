package com.example.listingapp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.listingapp.R
import com.example.listingapp.databinding.ActivityMainBinding
import com.example.listingapp.other.ApiCallManager
import com.example.listingapp.other.MyWorker
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
import com.example.listingapp.response.Data
import com.example.listingapp.response.WeatherResponse
import com.example.listingapp.util.NetworkResult
import com.example.listingapp.viewmodel.EmployeeViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.intuit.sdp.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: EmployeeViewModel by viewModels()
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment).navController
    }

    @Inject
    @JvmField
    internal var preferenceManager: PreferenceManager? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var apiCallManager: ApiCallManager

    companion object {
        private const val CHANNEL_ID = "your_channel_id"
        private const val REQUEST_CHECK_SETTINGS = 101
    }

    private lateinit var notificationManager: NotificationManager

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMaxUpdates(1)
            .build()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                mainViewModel.getWeather(latitude, longitude)
                apiCallManager.recordApiCall()
            }
        }
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun initComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        apiCallManager = ApiCallManager(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setupWorkManager()
        checkAndRequestNotificationPermission()
        requestPermissions()
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
        mainViewModel.response.observe(this) { response ->
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
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
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
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            requestLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Handle the exception
                }
            }
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            createNotificationChannel()
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                createNotificationChannel()
            } else {
                Toast.makeText(this, "Notification permission is required.", Toast.LENGTH_LONG)
                    .show()
                openAppSettings()
            }
        }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Your Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Your Channel Description"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationChannel", "Notification channel created")
        }
        showNotification()
    }

    private fun showNotification() {
        // Check if notification permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Notification", "Notification permission not granted")
            return
        }

        // Fetch weather data in a safe manner
        CoroutineScope(Dispatchers.Main).launch {
            val weatherData = withContext(Dispatchers.IO) {
                preferenceManager?.getModelValue<Data>(WEATHER_DATA)
            } ?: run {
                Log.d("Notification", "No weather data available")
                return@launch
            }

            val cityName = weatherData.cityName ?: "Unknown City"
            val temperature = weatherData.appTemp?.let { "$it \u2103" } ?: "Unknown Temperature"
            val condition = weatherData.weather?.description ?: "Unknown Condition"

            val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                putExtra("navigate_to_fragment", "WeatherFragment")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    this@MainActivity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            val contentText = "Current weather in $cityName: $temperature, $condition"

            val builder = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Weather Update")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                NotificationManagerCompat.from(this@MainActivity).notify(1, builder.build())
                Log.d("Notification", "Notification sent")
            } catch (e: Exception) {
                Log.e("Notification", "Error sending notification: ${e.message}")
            }
        }
    }
}