package com.finanzas.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF315C4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7EFD0),
    onPrimaryContainer = Color(0xFF073727),
    secondary = Color(0xFF9A4F2C),
    secondaryContainer = Color(0xFFFFDBC9),
    background = Color(0xFFF9FBF7),
    surface = Color(0xFFF9FBF7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD4B6),
    onPrimary = Color(0xFF073727),
    primaryContainer = Color(0xFF1D4A39),
    onPrimaryContainer = Color(0xFFB7EFD0),
    secondary = Color(0xFFFFB596),
    background = Color(0xFF101512),
    surface = Color(0xFF101512)
)

@Composable
fun FinanzasTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
