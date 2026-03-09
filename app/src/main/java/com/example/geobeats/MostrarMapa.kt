package com.example.geobeats

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Context

@Composable
// Recibe la ubicación del usuario desde MainActivity
fun MapScreen(userLocation: LatLng?) {

    AndroidView(
        factory = { context: Context ->

            val mapView = MapView(context)

            mapView.onCreate(null)
            mapView.onResume()

            mapView.getMapAsync { googleMap: GoogleMap ->

                // Habilita controles de zoom
                googleMap.uiSettings.isZoomControlsEnabled = true

                // Si tenemos ubicación del usuario, centramos el mapa y agregamos marcador
                userLocation?.let {

                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(it, 15f)
                    )

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(it)
                            .title("Tu ubicación")
                    )
                }
            }

            mapView
        }
    )
}