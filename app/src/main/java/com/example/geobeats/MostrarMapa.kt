package com.example.geobeats

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun MapScreen(userLocation: LatLng?) {

    AndroidView(
        factory = { context: Context ->

            val mapView = MapView(context)

            mapView.onCreate(null)
            mapView.onResume()

            mapView.getMapAsync { googleMap: GoogleMap ->

                googleMap.uiSettings.isZoomControlsEnabled = true


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


                val basilica = LatLng(9.8644, -83.9194)

                googleMap.addMarker(
                    MarkerOptions()
                        .position(basilica)
                        .title("Zona musical")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        ))
                )


                googleMap.setOnMarkerClickListener { marker ->
                    Toast.makeText(context,marker.title, Toast.LENGTH_SHORT).show()
                    true
                }


                userLocation?.let {

                    val distancia = calcularDistancia(
                        it.latitude, it.longitude,
                        basilica.latitude, basilica.longitude
                    )

                    if (distancia < 100) {
                        Toast.makeText(context, "Entraste a la zona musical", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            mapView
        }
    )
}
fun calcularDistancia(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {

    val R = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) *
            Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return R * c
}