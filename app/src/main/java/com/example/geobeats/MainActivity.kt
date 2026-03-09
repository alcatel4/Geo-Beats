package com.example.geobeats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geobeats.ui.theme.GeoBeatsTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {

    // Cliente de ubicación de Google
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()

        setContent {
            GeoBeatsTheme {

                var showMap by remember { mutableStateOf(false) }
                var userLocation by remember { mutableStateOf<LatLng?>(null) }

                /*
                 * Obtiene la última ubicación conocida del usuario
                 */
                LaunchedEffect(Unit) {

                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                userLocation = LatLng(location.latitude, location.longitude)
                            }
                        }

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

    /*
     * Solicita permiso de ubicación en tiempo de ejecución
     */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }
}