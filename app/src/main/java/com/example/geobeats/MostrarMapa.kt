package com.example.geobeats

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

// Estructura para representar una zona musical
data class ZonaMusical(
    val nombre: String,
    val coordenadas: LatLng,
    val playlistUri: String,
    val color: Int // Color en formato Hex ARGB
)

@Composable
fun MapScreen(userLocation: LatLng?) {
    // Lista de playlists disponibles para elegir
    val playlistsDisponibles = listOf(
        "Urbano" to "spotify:playlist:37i9dQZF1DXcBWIGoYBMm1",
        "Rock" to "spotify:playlist:37i9dQZF1DWXRqgorJjKmq",
        "Pop" to "spotify:playlist:37i9dQZF1DX1Ng397pN6pZ",
        "Chill" to "spotify:playlist:37i9dQZF1DX4sWvAiTneOx"
    )

    // Estado para las zonas musicales activas
    val zonasMusicales = remember { mutableStateListOf<ZonaMusical>() }
    
    // Inicializar con la Basílica como zona predeterminada
    LaunchedEffect(Unit) {
        if (zonasMusicales.isEmpty()) {
            zonasMusicales.add(
                ZonaMusical(
                    "Basílica (Urbano)",
                    LatLng(9.8644, -83.9194),
                    "spotify:playlist:37i9dQZF1DXcBWIGoYBMm1",
                    0x550000FF // Azul
                )
            )
        }
    }

    // Estados para el diálogo de selección
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var tempLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    // Control de reproducción
    var zonaActivaNombre by remember { mutableStateOf<String?>(null) }
    var isMusicTriggered by remember { mutableStateOf(false) }

    // Referencias del mapa
    var googleMapState by remember { mutableStateOf<GoogleMap?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    val marcadoresZonas = remember { mutableMapOf<String, Marker>() }
    val circulosZonas = remember { mutableMapOf<String, Circle>() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    onResume()
                    getMapAsync { map ->
                        googleMapState = map
                        map.uiSettings.isZoomControlsEnabled = true
                        
                        // Click en el mapa para crear nueva zona
                        map.setOnMapClickListener { latLng ->
                            tempLatLng = latLng
                            showPlaylistDialog = true
                        }

                        map.setOnMarkerClickListener { marker ->
                            val zona = zonasMusicales.find { it.nombre == marker.title }
                            zona?.let { abrirSpotify(ctx, it.playlistUri) }
                            false
                        }
                    }
                }
            },
            update = { view ->
                googleMapState?.let { map ->
                    // 1. Dibujar/Actualizar Zonas Musicales
                    zonasMusicales.forEach { zona ->
                        if (!marcadoresZonas.containsKey(zona.nombre)) {
                            val marker = map.addMarker(
                                MarkerOptions()
                                    .position(zona.coordenadas)
                                    .title(zona.nombre)
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                        if (zona.color == 0x550000FF) BitmapDescriptorFactory.HUE_BLUE else BitmapDescriptorFactory.HUE_MAGENTA
                                    ))
                            )
                            marker?.let { marcadoresZonas[zona.nombre] = it }

                            val circle = map.addCircle(
                                CircleOptions()
                                    .center(zona.coordenadas)
                                    .radius(100.0)
                                    .strokeWidth(2f)
                                    .strokeColor(zona.color)
                                    .fillColor(zona.color and 0x22FFFFFF)
                            )
                            circle?.let { circulosZonas[zona.nombre] = it }
                        }
                    }

                    // 2. Actualizar Usuario
                    userLocation?.let { location ->
                        if (userMarker == null) {
                            userMarker = map.addMarker(
                                MarkerOptions().position(location).title("Tu ubicación")
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        } else {
                            userMarker?.position = location
                        }

                        // 3. Lógica de Proximidad Dinámica
                        var algunaZonaCerca = false
                        zonasMusicales.forEach { zona ->
                            val distancia = calcularDistancia(
                                location.latitude, location.longitude,
                                zona.coordenadas.latitude, zona.coordenadas.longitude
                            )

                            if (distancia < 100) {
                                algunaZonaCerca = true
                                if (!isMusicTriggered || zonaActivaNombre != zona.nombre) {
                                    Toast.makeText(view.context, "Entrando a: ${zona.nombre}", Toast.LENGTH_SHORT).show()
                                    abrirSpotify(view.context, zona.playlistUri)
                                    isMusicTriggered = true
                                    zonaActivaNombre = zona.nombre
                                }
                            }
                        }

                        if (!algunaZonaCerca) {
                            isMusicTriggered = false
                            zonaActivaNombre = null
                        }
                    }
                }
            }
        )

        // Diálogo para elegir playlist al crear zona
        if (showPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showPlaylistDialog = false },
                title = { Text("Nueva Zona Musical") },
                text = {
                    Column {
                        Text("Selecciona el género para este lugar:")
                        Spacer(modifier = Modifier.height(8.dp))
                        playlistsDisponibles.forEach { (genero, uri) ->
                            Button(
                                onClick = {
                                    tempLatLng?.let { latLng ->
                                        zonasMusicales.add(
                                            ZonaMusical(
                                                "Zona $genero ${zonasMusicales.size}",
                                                latLng,
                                                uri,
                                                0x55FF00FF // Magenta para personalizadas
                                            )
                                        )
                                    }
                                    showPlaylistDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(genero)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPlaylistDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun abrirSpotify(context: Context, uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://${context.packageName}"))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Spotify no instalado", Toast.LENGTH_LONG).show()
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music"))
        try { context.startActivity(playStoreIntent) } catch (ex: Exception) {}
    }
}

fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}
