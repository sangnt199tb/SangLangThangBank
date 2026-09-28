package com.example.hydro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandRedLight,
    onPrimary = Color(0xFF690005),
    primaryContainer = BrandRedDark,
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF3B2A29),
    onSecondaryContainer = BrandRedLight,
    background = DarkBackground,
    surface = DarkSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = BrandRedContainer,
    onPrimaryContainer = BrandRedDark,
    secondaryContainer = BrandRedContainer,
    onSecondaryContainer = BrandRedDark,
    background = PageBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = InkSecondary,
    outline = OutlineGrey,
)

@Composable
fun HydroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Tắt màu động (Android 12+) để app luôn giữ màu thương hiệu
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}