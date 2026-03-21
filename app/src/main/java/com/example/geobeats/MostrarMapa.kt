package com.example.geobeats

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

@Composable
fun MapScreen(userLocation: LatLng?) {
    // Puntos de interés predefinidos
    val basilica = LatLng(9.8644, -83.9194)
    val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGoYBMm1"
    
    // Estado para un punto seleccionado manualmente por el usuario
    var puntoSeleccionado by remember { mutableStateOf<LatLng?>(null) }
    
    // Flag para evitar que Spotify se abra repetidamente
    var isMusicTriggered by remember { mutableStateOf(false) }

    // Referencias para el mapa y marcadores
    var googleMapState by remember { mutableStateOf<GoogleMap?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    var interestZoneCircle by remember { mutableStateOf<Circle?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(null)
                onResume()
                getMapAsync { map ->
                    googleMapState = map
                    map.uiSettings.isZoomControlsEnabled = true
                    
                    // Marcador fijo inicial (Basílica)
                    map.addMarker(
                        MarkerOptions()
                            .position(basilica)
                            .title("Zona Musical: Basílica")
                            .snippet("Toca para abrir Spotify")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                    
                    // Dibujar zona de interés inicial (100 metros)
                    map.addCircle(
                        CircleOptions()
                            .center(basilica)
                            .radius(100.0)
                            .strokeWidth(2f)
                            .strokeColor(0x550000FF) // Azul semi-transparente
                            .fillColor(0x220000FF)   // Relleno muy transparente
                    )

                    // Listener para seleccionar un nuevo punto en el mapa
                    map.setOnMapClickListener { latLng ->
                        puntoSeleccionado = latLng
                        isMusicTriggered = false // Resetear trigger para el nuevo punto
                        Toast.makeText(ctx, "Nuevo punto de interés seleccionado", Toast.LENGTH_SHORT).show()
                    }

                    map.setOnMarkerClickListener { marker ->
                        if (marker.position == basilica || marker.position == puntoSeleccionado) {
                            abrirSpotify(ctx, playlistUri)
                        }
                        false
                    }
                }
            }
        },
        update = { mapView ->
            googleMapState?.let { map ->
                userLocation?.let { location ->
                    // 1. Actualizar marcador del usuario
                    if (userMarker == null) {
                        userMarker = map.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title("Tu ubicación")
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    } else {
                        userMarker?.position = location
                    }

                    // 2. Gestionar punto seleccionado manualmente
                    puntoSeleccionado?.let { sel ->
                        // Actualizar o crear marcador de destino
                        if (destinationMarker == null) {
                            destinationMarker = map.addMarker(
                                MarkerOptions()
                                    .position(sel)
                                    .title("Destino Personalizado")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                            )
                        } else {
                            destinationMarker?.position = sel
                        }

                        // Actualizar o crear círculo de zona de interés (100m)
                        if (interestZoneCircle == null) {
                            interestZoneCircle = map.addCircle(
                                CircleOptions()
                                    .center(sel)
                                    .radius(100.0)
                                    .strokeWidth(2f)
                                    .strokeColor(0x55FF00FF) // Magenta semi-transparente
                                    .fillColor(0x22FF00FF)   // Relleno muy transparente
                            )
                        } else {
                            interestZoneCircle?.center = sel
                        }
                    }

                    // 3. Lógica de proximidad (Geofencing)
                    // Comprobar cercanía a la Basílica O al punto seleccionado
                    val distBasilica = calcularDistancia(location.latitude, location.longitude, basilica.latitude, basilica.longitude)
                    val distSeleccionado = puntoSeleccionado?.let { 
                        calcularDistancia(location.latitude, location.longitude, it.latitude, it.longitude)
                    } ?: Double.MAX_VALUE

                    if ((distBasilica < 100 || distSeleccionado < 100) && !isMusicTriggered) {
                        Toast.makeText(mapView.context, "¡Entrando en zona musical!", Toast.LENGTH_SHORT).show()
                        abrirSpotify(mapView.context, playlistUri)
                        isMusicTriggered = true
                    } else if (distBasilica > 120 && distSeleccionado > 120) {
                        isMusicTriggered = false
                    }
                }
            }
        }
    )
}

/**
 * Abre Spotify mediante un Intent Implícito.
 */
fun abrirSpotify(context: Context, uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://${context.packageName}"))

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Spotify no instalado", Toast.LENGTH_LONG).show()
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")))
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")))
        }
    }
}

fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Radio de la Tierra en metros
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}
