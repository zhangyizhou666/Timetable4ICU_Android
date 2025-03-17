package com.zhangyizhou666.timetable4icu_ver2.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkBlue,
    secondary = Silver,
    tertiary = Pink80,
    background = CreamWhite,
    surface = CreamWhite,
    onPrimary = Silver,
    onSecondary = DarkBlue,
    onTertiary = CreamWhite,
    onBackground = DarkBlue,
    onSurface = DarkBlue
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    secondary = Silver,
    tertiary = Pink40,
    background = CreamWhite,
    surface = CreamWhite,
    onPrimary = Silver,
    onSecondary = DarkBlue,
    onTertiary = CreamWhite,
    onBackground = DarkBlue,
    onSurface = DarkBlue
)

@Composable
fun Timetable4ICU_ver2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}