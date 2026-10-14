package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisDarkBackground,
    primaryContainer = JarvisDarkSurfaceVariant,
    onPrimaryContainer = JarvisCyanBright,
    secondary = JarvisElectricBlue,
    onSecondary = JarvisDarkBackground,
    secondaryContainer = JarvisBlueDark,
    onSecondaryContainer = JarvisBlueLight,
    tertiary = JarvisNeonMint,
    onTertiary = JarvisDarkBackground,
    background = JarvisDarkBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisDarkSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisDarkSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisDarkBorder,
    outlineVariant = JarvisDarkBorderGlow,
    error = JarvisNeonPink,
    onError = JarvisDarkBackground
)

private val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisElectricBlue,
    onPrimary = JarvisLightBackground,
    primaryContainer = JarvisLightSurfaceVariant,
    onPrimaryContainer = JarvisBlueDark,
    secondary = JarvisCyanDeep,
    onSecondary = JarvisLightBackground,
    secondaryContainer = JarvisLightSurfaceVariant,
    onSecondaryContainer = JarvisCyanDeep,
    tertiary = JarvisNeonMint,
    onTertiary = JarvisDarkBackground,
    background = JarvisLightBackground,
    onBackground = JarvisLightTextPrimary,
    surface = JarvisLightSurface,
    onSurface = JarvisLightTextPrimary,
    surfaceVariant = JarvisLightSurfaceVariant,
    onSurfaceVariant = JarvisLightTextSecondary,
    outline = JarvisLightBorder,
    outlineVariant = JarvisCyan,
    error = JarvisNeonPink,
    onError = JarvisLightBackground
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = true, // Default to futuristic dark HUD mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
