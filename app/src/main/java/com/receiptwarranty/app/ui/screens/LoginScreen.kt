package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import com.receiptwarranty.app.R
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape

@Composable
fun MeshGradientBackground(modifier: Modifier = Modifier) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    
    // Color Palettes
    val colors = if (isSystemInDarkTheme) {
        // Midnight Aurora
        listOf(
            Color(0xFF0F172A), // Deep Blueprint/Navy
            Color(0xFF311B92), // Deep Purple
            Color(0xFF006064), // Cyan/Teal
            Color(0xFF0A0F24)  // Darker base
        )
    } else {
        // Vibrant Frosted Mint
        listOf(
            Color(0xFF80CBC4), // Stronger Mint
            Color(0xFF4DB6AC), // Stronger Teal
            Color(0xFFA5D6A7), // Vibrant Green
            Color(0xFFE0F2F1)  // Subtle Mint Base
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "phase2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "phase3"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Base Layer
        drawRect(color = colors[3])

        // Blob 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors[0].copy(alpha = 0.7f), colors[0].copy(alpha = 0f)),
                center = Offset(width * phase1, height * 0.2f),
                radius = width * 0.9f
            ),
            radius = width * 0.9f,
            center = Offset(width * phase1, height * 0.2f)
        )

        // Blob 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors[1].copy(alpha = 0.6f), colors[1].copy(alpha = 0f)),
                center = Offset(width * 0.8f, height * phase2),
                radius = width * 1.0f
            ),
            radius = width * 1.0f,
            center = Offset(width * 0.8f, height * phase2)
        )

        // Blob 3
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors[2].copy(alpha = 0.5f), colors[2].copy(alpha = 0f)),
                center = Offset(width * phase3, height * 0.8f),
                radius = width * 1.1f
            ),
            radius = width * 1.1f,
            center = Offset(width * phase3, height * 0.8f)
        )
    }
}

/**
 * Premium minimalist login screen with Mesh Gradient and Glassmorphism.
 */
@Composable
fun LoginScreen(
    onSignInClick: () -> Unit,
    onSkipLogin: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1A1A1A)
    val mutedTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF1A1A1A).copy(alpha = 0.6f)
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MeshGradientBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Branding & Whitespace
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.padding(top = 80.dp)
            ) {
                // Removed Surface/Box, using just the Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_letter_v),
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color.Unspecified,
                    modifier = Modifier.size(72.dp)
                )
                
                Spacer(Modifier.height(Spacing.xxl))
                
                Text(
                    "Vault",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = textColor,
                    letterSpacing = (-1.5).sp,
                    fontSize = 56.sp
                )
                
                Text(
                    "Receipt and Warranty Tracker",
                    style = MaterialTheme.typography.bodyLarge,
                    color = mutedTextColor,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            // Bottom Section: Sign In
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (errorMessage != null) {
                    Text(
                        errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                }

                // Glassmorphic Google Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(VaultShape.pill)
                        .clickable(enabled = !isLoading, onClick = onSignInClick),
                    shape = VaultShape.pill,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = textColor
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(Spacing.md))
                                Text(
                                    "Continue with Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                // Understated Skip Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(VaultShape.small)
                        .clickable(enabled = !isLoading, onClick = onSkipLogin)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                ) {
                    Text(
                        "Use without signing in",
                        style = MaterialTheme.typography.labelLarge,
                        color = mutedTextColor
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = mutedTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
