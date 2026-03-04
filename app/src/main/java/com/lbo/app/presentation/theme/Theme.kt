package com.lbo.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue700,
    onPrimary = Color.White,
    primaryContainer = DeepBlue100,
    onPrimaryContainer = DeepBlue900,
    secondary = Orange500,
    onSecondary = Color.White,
    secondaryContainer = Orange100,
    onSecondaryContainer = Orange900,
    tertiary = DeepBlue400,
    onTertiary = Color.White,
    tertiaryContainer = DeepBlue50,
    onTertiaryContainer = DeepBlue800,
    background = OffWhite,
    onBackground = Gray900,
    surface = SurfaceWhite,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue300,
    onPrimary = DeepBlue900,
    primaryContainer = DeepBlue700,
    onPrimaryContainer = DeepBlue100,
    secondary = Orange400,
    onSecondary = Orange900,
    secondaryContainer = Orange700,
    onSecondaryContainer = Orange100,
    tertiary = DeepBlue200,
    onTertiary = DeepBlue800,
    tertiaryContainer = DeepBlue600,
    onTertiaryContainer = DeepBlue50,
    background = DarkBackground,
    onBackground = Gray100,
    surface = DarkSurface,
    onSurface = Gray100,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray700,
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = ErrorLight
)

@Composable
fun LBOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LBOTypography,
        shapes = LBOShapes,
        content = content
    )
}
