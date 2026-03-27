package com.shambasmart.presentation.common.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Organic Dark Precision Color Scheme
private val OrganicDarkColorScheme = darkColorScheme(
    primary = Green400,
    onPrimary = Green950,
    primaryContainer = Green800,
    onPrimaryContainer = Green200,
    secondary = Earth400,
    onSecondary = Earth900,
    secondaryContainer = Earth800,
    onSecondaryContainer = Earth200,
    tertiary = Teal400,
    onTertiary = Teal600,
    tertiaryContainer = Teal600,
    onTertiaryContainer = Teal100,
    error = Red400,
    onError = Red100,
    errorContainer = Red600,
    onErrorContainer = Red200,
    background = SurfaceBase,
    onBackground = Neutral950,
    surface = SurfaceRaised,
    onSurface = Neutral950,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = Neutral800,
    outline = Neutral300,
    outlineVariant = Neutral200,
    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = Neutral950,
    inverseOnSurface = SurfaceBase,
    inversePrimary = Green600,
    surfaceDim = SurfaceSunken,
    surfaceBright = SurfaceOverlay,
    surfaceContainerLowest = SurfaceSunken,
    surfaceContainerLow = SurfaceBase,
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = SurfaceElevated,
    surfaceContainerHighest = SurfaceOverlay
)

// Light scheme (kept for compatibility but app will use dark by default)
private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Earth600,
    onSecondary = Color.White,
    secondaryContainer = Earth100,
    onSecondaryContainer = Earth900,
    tertiary = Teal500,
    onTertiary = Color.White,
    tertiaryContainer = Teal100,
    onTertiaryContainer = Teal600,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red600,
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF0D1210),
    surface = Color(0xFFF8FAF9),
    onSurface = Color(0xFF0D1210),
    surfaceVariant = Color(0xFFE8F0E8),
    onSurfaceVariant = Color(0xFF4A5C55),
    outline = Color(0xFF8A9E96)
)

@Composable
fun ShambaSmartTheme(
    darkTheme: Boolean = true, // Default to dark theme for Organic Dark Precision
    dynamicColor: Boolean = false, // Disable dynamic color to maintain design system
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> OrganicDarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SurfaceBase.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Extension properties for custom colors
val MaterialTheme.customColors: CustomColors
    @Composable
    get() = if (isSystemInDarkTheme()) DarkCustomColors else LightCustomColors

data class CustomColors(
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceElevated: Color,
    val surfaceOverlay: Color,
    val surfaceSunken: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val neutral950: Color,
    val neutral800: Color,
    val neutral600: Color,
    val neutral400: Color,
    val neutral300: Color,
    val neutral200: Color,
    val neutral100: Color
)

val DarkCustomColors = CustomColors(
    surfaceBase = SurfaceBase,
    surfaceRaised = SurfaceRaised,
    surfaceElevated = SurfaceElevated,
    surfaceOverlay = SurfaceOverlay,
    surfaceSunken = SurfaceSunken,
    success = Green400,
    warning = Amber400,
    info = Teal400,
    neutral950 = Neutral950,
    neutral800 = Neutral800,
    neutral600 = Neutral600,
    neutral400 = Neutral400,
    neutral300 = Neutral300,
    neutral200 = Neutral200,
    neutral100 = Neutral100
)

val LightCustomColors = CustomColors(
    surfaceBase = Color(0xFFF8FAF9),
    surfaceRaised = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F5F2),
    surfaceOverlay = Color(0xFFE8F0E8),
    surfaceSunken = Color(0xFFF5F8F6),
    success = Green600,
    warning = Amber500,
    info = Teal500,
    neutral950 = Color(0xFF0D1210),
    neutral800 = Color(0xFF4A5C55),
    neutral600 = Color(0xFF8A9E96),
    neutral400 = Color(0xFFC4CEC9),
    neutral300 = Color(0xFFD0E6DB),
    neutral200 = Color(0xFFE0F0E8),
    neutral100 = Color(0xFFF0F8F4)
)