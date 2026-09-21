package com.fedo.modelpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fedo.modelpulse.ui.navigation.ModelPulseNavDisplay
import com.fedo.modelpulse.ui.theme.ModelPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModelPulseTheme {
                ModelPulseNavDisplay()
            }
        }
    }
}
