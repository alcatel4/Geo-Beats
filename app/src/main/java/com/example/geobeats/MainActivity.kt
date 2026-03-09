package com.example.geobeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.geobeats.ui.theme.GeoBeatsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeoBeatsTheme {
                var showMap by remember { mutableStateOf(false) }
                if (showMap) {
                    MapScreen()
                } else {
                    HomeScreen {
                        showMap = true
                    }
                }

            }
        }
    }
}