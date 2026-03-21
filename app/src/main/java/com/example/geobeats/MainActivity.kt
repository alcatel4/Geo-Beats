package com.example.geobeats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geobeats.ui.theme.GeoBeatsTheme
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var locationCallback: LocationCallback
    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()

        setContent {
            GeoBeatsTheme {
                var showMap by remember { mutableStateOf(false) }
                var userLocation by remember { mutableStateOf<LatLng?>(null) }

                // Configurar actualizaciones de ubicación en tiempo real
                DisposableEffect(Unit) {
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            for (location in locationResult.locations) {
                                userLocation = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }

                    startLocationUpdates()

                    onDispose {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }

                if (showMap) {
                    MapScreen(userLocation)
                } else {
                    HomeScreen {
                        showMap = true
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }
}