package com.receiptwarranty.app.util.share

import androidx.core.graphics.toColorInt
import android.graphics.Typeface
import android.graphics.Color

enum class ShareTheme {
    CLEAN_WHITE,
    DARK_ELEGANT,
    BRANDED_GRADIENT,
    MINIMAL,
    ONEUI_LAVENDER,
    ONEUI_MINT,
    ONEUI_OCEAN,
    ONEUI_CORAL
}

object ShareCardStyle {
    // Sizing & Spacing (in pixels, assuming width around 1080px base)
    const val CARD_WIDTH = 1080f
    const val PADDING_OUTER = 64f
    const val PADDING_INNER = 48f
    const val SECTION_SPACING = 56f
    
    const val RADIUS_CARD = 64f
    const val RADIUS_IMAGE = 40f
    const val RADIUS_CHIP = 24f
    const val RADIUS_GRID_ITEM = 32f

    // Typography Sizes
    const val TEXT_SIZE_TITLE = 56f
    const val TEXT_SIZE_SUBTITLE = 36f
    const val TEXT_SIZE_BODY = 32f
    const val TEXT_SIZE_CAPTION = 28f
    const val TEXT_SIZE_SECTION_HEADER = 24f

    fun getColors(theme: ShareTheme): ThemeColors {
        return when (theme) {
            ShareTheme.CLEAN_WHITE -> ThemeColors(
                background = "#F8F9FA".toColorInt(), 
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#F1F3F5".toColorInt(), 
                textPrimary = "#1C1C1E".toColorInt(),
                textSecondary = "#636366".toColorInt(),
                textTertiary = "#8E8E93".toColorInt(),
                accent = "#1259C3".toColorInt(), 
                divider = "#E5E5EA".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt()
            )
            ShareTheme.DARK_ELEGANT -> ThemeColors(
                background = "#000000".toColorInt(), 
                surface = "#1C1C1E".toColorInt(), 
                surfaceVariant = "#2C2C2E".toColorInt(), 
                textPrimary = "#FFFFFF".toColorInt(),
                textSecondary = "#EBEBF5".toColorInt(),
                textTertiary = "#8E8E93".toColorInt(),
                accent = "#4B9CFA".toColorInt(), 
                divider = "#38383A".toColorInt(),
                successBg = "#1B4527".toColorInt(), 
                successText = "#81C784".toColorInt(), 
                warningBg = "#5A3102".toColorInt(),
                warningText = "#FFB74D".toColorInt(),
                errorBg = "#5C1A1A".toColorInt(),
                errorText = "#E57373".toColorInt()
            )
            ShareTheme.BRANDED_GRADIENT -> ThemeColors(
                background = "#0F2027".toColorInt(),
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#F8F9FA".toColorInt(),
                textPrimary = "#1C1C1E".toColorInt(),
                textSecondary = "#636366".toColorInt(),
                textTertiary = "#8E8E93".toColorInt(),
                accent = "#1259C3".toColorInt(),
                divider = "#E5E5EA".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt(),
                isGradient = true,
                gradientColors = intArrayOf("#141E30".toColorInt(), "#243B55".toColorInt())
            )
            ShareTheme.MINIMAL -> ThemeColors(
                background = "#FFFFFF".toColorInt(),
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#FFFFFF".toColorInt(), 
                textPrimary = "#000000".toColorInt(),
                textSecondary = "#333333".toColorInt(),
                textTertiary = "#666666".toColorInt(),
                accent = "#000000".toColorInt(), 
                divider = "#E0E0E0".toColorInt(),
                successBg = "#F5F5F5".toColorInt(),
                successText = "#000000".toColorInt(),
                warningBg = "#F5F5F5".toColorInt(),
                warningText = "#000000".toColorInt(),
                errorBg = "#F5F5F5".toColorInt(),
                errorText = "#000000".toColorInt()
            )
            ShareTheme.ONEUI_LAVENDER -> ThemeColors(
                background = "#DCD4F5".toColorInt(), // Lavender bg
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#F4F1FA".toColorInt(), 
                textPrimary = "#271E3A".toColorInt(),
                textSecondary = "#64597A".toColorInt(),
                textTertiary = "#8A829D".toColorInt(),
                accent = "#7D5ECA".toColorInt(), // Deep lavender
                divider = "#E0D9ED".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt()
            )
            ShareTheme.ONEUI_MINT -> ThemeColors(
                background = "#C1F0E4".toColorInt(), // Mint bg
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#EDFBF7".toColorInt(), 
                textPrimary = "#0C362A".toColorInt(),
                textSecondary = "#346658".toColorInt(),
                textTertiary = "#618A7F".toColorInt(),
                accent = "#13A37F".toColorInt(), 
                divider = "#D4EBE4".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt()
            )
            ShareTheme.ONEUI_OCEAN -> ThemeColors(
                background = "#C1E3FC".toColorInt(), // Ocean blue bg
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#F0F7FD".toColorInt(), 
                textPrimary = "#092F4D".toColorInt(),
                textSecondary = "#376182".toColorInt(),
                textTertiary = "#698BA7".toColorInt(),
                accent = "#1A73E8".toColorInt(), 
                divider = "#D4E6F5".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt()
            )
            ShareTheme.ONEUI_CORAL -> ThemeColors(
                background = "#FADFD2".toColorInt(), // Coral/Peach bg
                surface = "#FFFFFF".toColorInt(),
                surfaceVariant = "#FDF4F0".toColorInt(),
                textPrimary = "#4A1B1F".toColorInt(),
                textSecondary = "#80363C".toColorInt(),
                textTertiary = "#A36A6E".toColorInt(),
                accent = "#E53946".toColorInt(),
                divider = "#F5D2D5".toColorInt(),
                successBg = "#E8F5E9".toColorInt(),
                successText = "#2E7D32".toColorInt(),
                warningBg = "#FFF3E0".toColorInt(),
                warningText = "#E65100".toColorInt(),
                errorBg = "#FFEBEE".toColorInt(),
                errorText = "#C62828".toColorInt()
            )
        }
    }
}

data class ThemeColors(
    val background: Int,
    val surface: Int,
    val surfaceVariant: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val textTertiary: Int,
    val accent: Int,
    val divider: Int,
    val successBg: Int,
    val successText: Int,
    val warningBg: Int,
    val warningText: Int,
    val errorBg: Int,
    val errorText: Int,
    val isGradient: Boolean = false,
    val gradientColors: IntArray? = null
)
