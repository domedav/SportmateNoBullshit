package com.domedav.sportmatenobullshit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Material 3 Expressive Shapes
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue80,
    onPrimary = NeutralBlack,
    primaryContainer = DeepBlue40,
    onPrimaryContainer = DeepBlue80,

    secondary = SkyBlue80,
    onSecondary = NeutralBlack,
    secondaryContainer = SkyBlue40,
    onSecondaryContainer = SkyBlue80,

    tertiary = Coral80,
    onTertiary = NeutralBlack,
    tertiaryContainer = Coral40,
    onTertiaryContainer = Coral80,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = NeutralBlack,
    onBackground = Color(0xFFE3E2E6),

    surface = SurfaceDark,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC5C6D0),

    outline = Color(0xFF8F9099),
    outlineVariant = Color(0xFF44474E),

    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = NeutralBlack,
    inversePrimary = DeepBlue40,

    surfaceTint = DeepBlue80,
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FD),
    onPrimaryContainer = Color(0xFF001C38),

    secondary = SkyBlue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF001A41),

    tertiary = Coral40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = NeutralWhite,
    onBackground = NeutralBlack,

    surface = SurfaceLight,
    onSurface = NeutralBlack,
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),

    outline = Color(0xFF75777F),
    outlineVariant = Color(0xFFC5C6D0),

    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = DeepBlue80,

    surfaceTint = DeepBlue40,
    scrim = Color(0xFF000000)
)

@Composable
fun SportmateNoBullshitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
