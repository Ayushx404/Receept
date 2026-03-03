package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.background
import com.receiptwarranty.app.ui.theme.VaultShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.receiptwarranty.app.ui.theme.ThemeMode

@Composable
fun ThemePreviewCard(
    primaryColor: Color,
    backgroundColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "scale"
    )

    Column(
        modifier = modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f) // Phone aspect ratio (e.g. 10(W):16(H))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(VaultShape.medium)
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                    shape = VaultShape.medium
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                )
        ) {
            // "Screen" elements mock-up
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top header / search bar mock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(VaultShape.small)
                        .background(surfaceColor)
                ) {
                    // Title text line mock inside header
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .width(40.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(onSurfaceColor)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hero Card / Main feature mock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(VaultShape.small)
                        .background(primaryColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Primary" colored icon mock
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(primaryColor)
                        )
                    }
                }
            }

            // Bottom Navigation Pill mock
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(VaultShape.small)
                    .background(surfaceColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // FAB / Selected Tab mock
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(onSurfaceColor.copy(alpha = 0.5f)))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(onSurfaceColor.copy(alpha = 0.5f)))
                }
            }

            // Checkmark overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            lineHeight = 16.sp
        )
    }
}
