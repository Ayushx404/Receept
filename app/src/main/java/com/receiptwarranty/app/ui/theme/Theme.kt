package com.receiptwarranty.app.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

sealed class ThemeMode {
    data object LIGHT : ThemeMode()
    data object DARK : ThemeMode()
    data object SYSTEM : ThemeMode()
}

// ─── Samsung-inspired POP palette ────────────────────────────────────────────

// Light Mode — Samsung One UI 8 inspired (Vibrant & Clean)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0381FE),             // One UI 8 Dynamic Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5F1FF),     // Soft blue accent
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFFFF6B35),            // Vibrant Coral
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE0CC),
    onSecondaryContainer = Color(0xFF331400),
    tertiary = Color(0xFF2ECD71),             // Samsung Zen Green
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6F5E1),
    onTertiaryContainer = Color(0xFF002108),
    error = Color(0xFFFF3B30),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAF9FF),           // Modern Off-white/Cool Gray
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),              // Pure White Surface
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF2F2F7),       // One UI Light Neutral
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    inverseOnSurface = Color(0xFFF0F0F3),
    inverseSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF9ECAFF),
    surfaceTint = Color(0xFF0381FE),
    outlineVariant = Color(0xFFC4C6D0),
    scrim = Color(0xFF000000)
)

// Dark Mode — Deep & Minimal
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0B84FE),              // Vibrant dark blue
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004A80),
    onPrimaryContainer = Color(0xFFE5F1FF),
    secondary = Color(0xFFFF8A5C),
    onSecondary = Color(0xFF4D1F00),
    secondaryContainer = Color(0xFF3E1C00),
    onSecondaryContainer = Color(0xFFFFE0CC),
    tertiary = Color(0xFF63E68A),
    onTertiary = Color(0xFF003913),
    tertiaryContainer = Color(0xFF003D17),
    onTertiaryContainer = Color(0xFFD6F5E1),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0A0B),           // Modern Deep Black
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1C1C1E),              // Elevated Deep Surface
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF242426),       // Secondary Surface
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    inverseOnSurface = Color(0xFF1A1C1E),
    inverseSurface = Color(0xFFE2E2E6),
    inversePrimary = Color(0xFF0381FE),
    surfaceTint = Color(0xFF0B84FE),
    outlineVariant = Color(0xFF44474F),
    scrim = Color(0xFF000000)
)

// AMOLED Dark — pure black for OLED screens (Even darker surface)
private val AmoledDarkColorScheme = DarkColorScheme.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),             // Total black integration
    surfaceVariant = Color(0xFF0A0A0A)
)

@Composable
fun ReceiptWarrantyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    primaryColor: String = "#0381FE",
    useDynamicColor: Boolean = false,
    useAmoledBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme && useAmoledBlack -> {
            val primaryColorParsed = try {
                Color(AndroidColor.parseColor(primaryColor))
            } catch (e: Exception) { AmoledDarkColorScheme.primary }
            AmoledDarkColorScheme.copy(
                primary = primaryColorParsed,
                primaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onPrimaryContainer = primaryColorParsed,
                secondaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onSecondaryContainer = primaryColorParsed
            )
        }
        darkTheme -> {
            val primaryColorParsed = try {
                Color(AndroidColor.parseColor(primaryColor))
            } catch (e: Exception) { DarkColorScheme.primary }
            DarkColorScheme.copy(
                primary = primaryColorParsed,
                primaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onPrimaryContainer = primaryColorParsed,
                secondaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onSecondaryContainer = primaryColorParsed
            )
        }
        else -> {
            val primaryColorParsed = try {
                Color(AndroidColor.parseColor(primaryColor))
            } catch (e: Exception) { LightColorScheme.primary }
            LightColorScheme.copy(
                primary = primaryColorParsed,
                primaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onPrimaryContainer = primaryColorParsed,
                secondaryContainer = primaryColorParsed.copy(alpha = 0.15f),
                onSecondaryContainer = primaryColorParsed
            )
        }
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
        shapes = AppShapes,
        content = content
    )
}

// ─── Semantic status colors (theme-independent) ─────────────────────────────

object WarrantyBadgeColors {
    val valid = Color(0xFF2ECD71)          // Samsung green
    val expiringSoon = Color(0xFFFF9500)   // Amber
    val expired = Color(0xFFFF3B30)        // Red
}

object VaultColors {
    val statusValid = Color(0xFF2ECD71)
    val statusValidBg = Color(0xFFD6F5E1)
    val statusExpiringSoon = Color(0xFFFF9500)
    val statusExpiringSoonBg = Color(0xFFFFF4E5)
    val statusExpired = Color(0xFFFF3B30)
    val statusExpiredBg = Color(0xFFFFE5E3)
    val statusOffline = Color(0xFF636366)
    val statusOfflineBg = Color(0xFFF2F2F7)
    val statusSyncing = Color(0xFF0381FE)
    val statusSyncingBg = Color(0xFFE5F1FF)
    val statusSyncError = Color(0xFFFF3B30)
    val statusSyncErrorBg = Color(0xFFFFE5E3)
}

// Accent color swatches for the Settings picker — One UI 8 Bold selection
val availableColors = listOf(
    "#0381FE",   // Dynamic Blue
    "#FF6B35",   // Vibrant Coral
    "#2ECD71",   // Zen Green
    "#FF3B30",   // Signal Red
    "#AF52DE",   // Soft Purple
    "#5856D6",   // Deep Indigo
    "#FF2D55",   // Punchy Pink
    "#00C7BE",   // Fresh Teal
    "#64D2FF",   // Sky Blue
    "#FFD60A"    // Electric Yellow
)
