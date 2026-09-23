package com.fedo.modelpulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dynamic colour covers API 31+, which is most devices this app will ever run
// on. Below that the Material baseline schemes are used as they ship: an
// example app inventing a palette would teach nothing, and every colour it
// hardcoded would be one the theme could no longer control.
private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour needs API 31; minSdk is 29, hence the version check below.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Expressive: the app uses the expressive shape, motion and type scales,
    // so every screen gets them by default.
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}