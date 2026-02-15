package com.receiptwarranty.app.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

sealed class ThemeMode {
    data object LIGHT : ThemeMode()
    data object DARK : ThemeMode()
    data object SYSTEM : ThemeMode()
}

private val Teal = Color(0xFF079992)
private val TealDark = Color(0xFF047A74)
private val Amber = Color(0xFFFFB74D)
private val Red = Color(0xFFE57373)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF004D49),
    secondary = TealDark,
    onSecondary = Color.White,
    tertiary = Color(0xFF00897B),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    error = Red,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF003733),
    primaryContainer = TealDark,
    onPrimaryContainer = Color(0xFF80CBC4),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003733),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

@Composable
fun ReceiptWarrantyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    primaryColor: String = "#079992",
    secondaryColor: String = "#047A74",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val primaryColorParsed = try {
        Color(AndroidColor.parseColor(primaryColor))
    } catch (e: Exception) {
        Teal
    }

    val secondaryColorParsed = try {
        Color(AndroidColor.parseColor(secondaryColor))
    } catch (e: Exception) {
        TealDark
    }

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = primaryColorParsed,
            secondary = secondaryColorParsed,
            tertiary = primaryColorParsed.copy(alpha = 0.8f)
        )
    } else {
        LightColorScheme.copy(
            primary = primaryColorParsed,
            secondary = secondaryColorParsed,
            tertiary = primaryColorParsed.copy(alpha = 0.8f)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object WarrantyBadgeColors {
    val valid = Color(0xFF4CAF50)
    val expiringSoon = Amber
    val expired = Red
}

val availableColors = listOf(
    "#079992",
    "#E55039",
    "#6A89CC",
    "#78E08F",
    "#F8C291",
    "#EAB543",
    "#9B59B6",
    "#3498DB",
    "#1ABC9C",
    "#E74C3C"
)
