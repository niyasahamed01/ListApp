package com.example.listingapp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.listingapp.R
import com.example.listingapp.databinding.ActivityMainBinding
import com.example.listingapp.other.MyWorker
import com.example.listingapp.other.NotificationHelper
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.preference.WEATHER_DATA
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
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var isObserverSet = false

    private lateinit var binding: ActivityMainBinding

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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val CHANNEL_ID = "your_channel_id"
        private const val REQUEST_CHECK_SETTINGS = 101
    }

    private lateinit var notificationManager: NotificationManager

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        onClickListener()
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val workRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        requestLocationPermission()

        requestNotificationPermission()

//        setObserver()
    }


    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed with creating notification channel
            createNotificationChannel()
        } else {
            // Request permission
            requestPermissionNotification.launch(
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            )
        }
    }

    private val requestPermissionNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, create notification channel
                createNotificationChannel()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        // Show the notification
        showNotification()
    }

    private fun showNotification() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Notification Title")
                .setContentText("Notification Content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, builder.build())
            }
            Toast.makeText(this, "Permission  granted", Toast.LENGTH_SHORT).show()

        } else {
            // Handle the case where permission was not granted
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, check location settings
            checkLocationSettings()
        } else {
            // Request permission
            requestPermission.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, check location settings
                checkLocationSettings()
            } else {
                // Handle permission denied
            }
        }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // All location settings are satisfied. Start location updates.
            requestLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // Location settings dialog was shown and user enabled location
                requestLocationUpdates()
            } else {
                // User did not enable location, handle accordingly
            }
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            p0 ?: return
            for (location in p0.locations) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Use latitude and longitude
                mainViewModel.getWeather(
                    latitude,
                    longitude
                )
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
                    binding.progress.visibility = View.GONE
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
                    binding.progress.visibility = View.GONE
                    Toast.makeText(
                        this,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NetworkResult.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                }
            }
        }
    }
}