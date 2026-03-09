package com.example.geobeats

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.content.Context

@Composable
//Carga el mapa con la Api de googleMaps
fun MapScreen() {

    AndroidView(
        factory = { context: Context ->

            val mapView = MapView(context)

            mapView.onCreate(null)
            mapView.onResume()

            mapView.getMapAsync { googleMap: GoogleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
            }

            mapView
        }
    )
}