package com.minhtu.firesocialmedia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light mode reds
private val Red40 = Color(0xFFD32F2F)
private val RedGrey40 = Color(0xFFC62828)
private val RedAccent40 = Color(0xFFB71C1C)

// Dark mode reds — lighter so they're visible on dark backgrounds
private val Red70 = Color(0xFFE57373)
private val RedGrey70 = Color(0xFFEF9A9A)
private val RedAccent70 = Color(0xFFFFCDD2)

// Shared
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val OnPrimaryDark  = Color(0xFF690005)

private val PrimaryContainerLight = Color(0xFFFFCDD2)
private val OnPrimaryContainerLight = Color(0xFF410002)
private val PrimaryContainerDark = Color(0xFF93000A)
private val OnPrimaryContainerDark = Color(0xFFFFDAD6)

private val SecondaryContainerLight = Color(0xFFFFEBEE)
private val OnSecondaryContainerLight = Color(0xFF410002)
private val SecondaryContainerDark = Color(0xFF7B1D1D)
private val OnSecondaryContainerDark = Color(0xFFFFDAD6)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = RedGrey40,
    onSecondary = OnPrimaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = RedAccent40,
    onTertiary = OnPrimaryLight,
    tertiaryContainer = Color(0xFFFFEBEE),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFFFFFFF),   // white main background
    onBackground = Color(0xFF111111),
    surface = Color(0xFFF5F5F5),      // light gray cards — visible on white background
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEDEBEB), // slightly deeper for chips, inputs, nav bar
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFD0CDD7),       // soft border, not too harsh
    outlineVariant = Color(0xFFE8E5EE),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Red70,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = RedGrey70,
    onSecondary = OnPrimaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = RedAccent70,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = Color(0xFF7B1D1D),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF0EDF4),   // bright near-white for max readability
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF0EDF4),      // bright near-white — strong contrast on dark cards
    surfaceVariant = Color(0xFF2C2C2E), // dark gray — for cards, input fields
    onSurfaceVariant = Color(0xFFDDD8E4), // much brighter than before for secondary text contrast
    outline = Color(0xFFB0ABB8),        // lighter so borders/dividers are visible in dark
    outlineVariant = Color(0xFF5C5768), // visible but subdued separator
    error = Color(0xFFCF6679),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
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