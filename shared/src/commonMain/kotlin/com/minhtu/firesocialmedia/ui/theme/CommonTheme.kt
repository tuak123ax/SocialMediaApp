package com.minhtu.firesocialmedia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Red theme colors (matching Android MyApplicationTheme)
private val Red80 = Color(0xFFFFCDD2)  // Red 100
private val RedGrey80 = Color(0xFFFFEBEE)  // Red 50
private val RedAccent80 = Color(0xFFFFEBEE)  // Red 50

private val Red40 = Color(0xFFD32F2F)  // Red 700 (primary)
private val RedGrey40 = Color(0xFFC62828)  // Red 800 (secondary)
private val RedAccent40 = Color(0xFFB71C1C)  // Red 900 (tertiary)

// Additional colors for containers and text
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFFFCDD2) // Red 100
private val OnPrimaryContainer = Color(0xFF410002)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFFFEBEE) // Red 50
private val OnSecondaryContainer = Color(0xFF410002)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFFEBEE)
private val OnTertiaryContainer = Color(0xFF410002)

private val DarkColorScheme = darkColorScheme(
    primary = Red80,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = RedGrey80,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = RedAccent80,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = RedGrey40,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = RedAccent40,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer
)

@Composable
fun FireSocialMediaCommonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
