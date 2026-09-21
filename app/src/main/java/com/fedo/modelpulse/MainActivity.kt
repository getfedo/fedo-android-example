package com.fedo.modelpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fedo.modelpulse.ui.models.ModelsRoute
import com.fedo.modelpulse.ui.theme.ModelPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModelPulseTheme {
                // ponytail: the list is the whole app until its.6 adds the
                // Navigation 3 back stack; this is the one line it replaces.
                ModelsRoute()
            }
        }
    }
}
