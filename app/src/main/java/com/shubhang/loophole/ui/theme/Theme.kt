package com.shubhang.loophole.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = TerminalGreen80,
    onPrimary = TerminalGreenGrey10,
    primaryContainer = TerminalGreen30,
    onPrimaryContainer = TerminalGreen90,
    secondary = Slate80,
    tertiary = Amber80
)

private val LightColorScheme = lightColorScheme(
    primary = TerminalGreen40,
    onPrimary = TerminalGreen90,
    primaryContainer = TerminalGreen90,
    onPrimaryContainer = TerminalGreenGrey10,
    secondary = Slate40,
    tertiary = Amber40
)

// Expressive: generously rounded shapes across the board.
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

/**
 * Material 3 theme with an expressive design language: the device's dynamic
 * color on Android 12+, an on-brand terminal-green palette otherwise, and large
 * rounded shapes. (Material 3's dedicated Expressive theme APIs are still
 * internal in the current stable Compose BOM, so the expressive look is applied
 * on top of the stable M3 theme.)
 */
@Composable
fun LoopholeTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
