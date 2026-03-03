package com.receiptwarranty.app.ui.components

import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.viewmodel.MonthlySpending

/**
 * A reusable header for analytics sections with a OneUI 8 styled "Info" button.
 */
@Composable
fun StatHeader(
    title: String,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), VaultShape.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(Spacing.md))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Info button with mandatory 48dp minimum touch target
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * A visual overview of warranty statuses (Active, Expiring, Expired).
 */
@Composable
fun WarrantyHealthCard(
    active: Int,
    expiring: Int,
    expired: Int,
    modifier: Modifier = Modifier
) {
    val total = (active + expiring + expired).coerceAtLeast(1)
    val activeRatio = active.toFloat() / total
    val expiringRatio = expiring.toFloat() / total
    val expiredRatio = expired.toFloat() / total

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Coverage Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Status of $total items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { activeRatio + expiringRatio },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${((activeRatio + expiringRatio) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(Spacing.xl))
            
            // Segments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(VaultShape.pill)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                if (activeRatio > 0) {
                    val actualRatio = if (expiringRatio == 0f && expiredRatio == 0f) 1f else activeRatio
                    Box(modifier = Modifier.weight(actualRatio).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
                if (expiringRatio > 0) {
                    Box(modifier = Modifier.weight(expiringRatio).fillMaxHeight().background(Color(0xFFFFA500)))
                }
                if (expiredRatio > 0) {
                    Box(modifier = Modifier.weight(expiredRatio).fillMaxHeight().background(MaterialTheme.colorScheme.error))
                }
            }
            
            Spacer(Modifier.height(Spacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthLegendItemRes("Active", active, MaterialTheme.colorScheme.primary)
                HealthLegendItemRes("Expiring", expiring, Color(0xFFFFA500))
                HealthLegendItemRes("Expired", expired, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun HealthLegendItemRes(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A highly-rounded bottom sheet for displaying metric information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoBottomSheet(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = VaultShape.extraLarge, // OneUI 8 40.dp
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxxl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.xl))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = VaultShape.medium
            ) {
                Text("Got it")
            }
        }
    }
}

/**
 * An optimized donut chart with scale-in animations and radial gradients.
 */
@Composable
fun PremiumDonutChart(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF0381FE), // Vault Blue
        MaterialTheme.colorScheme.error
    )
) {
    val total = distribution.values.sum().coerceAtLeast(1)
    
    // Scale-in animation on launch
    var animationPlayed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer { // GPU-accelerated scaling
                    scaleX = scale
                    scaleY = scale
                    alpha = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                distribution.entries.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value.toFloat() / total) * 360f
                    if (sweepAngle > 0) {
                        val color = colors.getOrElse(index) { Color.Gray }
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(
                                width = 18.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.width(Spacing.xl))
        
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            distribution.entries.forEachIndexed { index, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors.getOrElse(index) { Color.Gray }, CircleShape)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = "${entry.key} (${entry.value})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Premium spending chart with vertical grow animations and primary glow.
 */
@Composable
fun PremiumSpendingChart(
    data: List<MonthlySpending>,
    primaryColor: Color,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recent history", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    val normalizedMax = if (maxAmount == 0.0) 1.0 else maxAmount
    
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tooltip Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedIndex != -1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (selectedIndex != -1 && selectedIndex < data.size) {
                    val item = data[selectedIndex]
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        shape = VaultShape.pill
                    ) {
                        Text(
                            text = "${item.monthLabel}: $currencySymbol${String.format(Locale.ROOT, "%.0f", item.amount)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, item ->
                val barHeightRatio = (item.amount / normalizedMax).coerceIn(0.05, 1.0)
                val isSelected = selectedIndex == index
                
                // Sequential grow animation
                var animationStarted by remember { mutableStateOf(false) }
                val animatedHeight by animateFloatAsState(
                    targetValue = if (animationStarted) barHeightRatio.toFloat() else 0f,
                    animationSpec = tween(
                        durationMillis = 800,
                        delayMillis = index * 100,
                        easing = FastOutSlowInEasing
                    ),
                    label = "barHeight"
                )

                LaunchedEffect(Unit) {
                    animationStarted = true
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedIndex = if (isSelected) -1 else index }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(animatedHeight)
                            .graphicsLayer {
                                shadowElevation = if (isSelected) 8.dp.toPx() else 2.dp.toPx()
                                shape = VaultShape.small
                                clip = true
                                scaleX = if (isSelected) 1.1f else 1.0f
                                scaleY = if (isSelected) 1.05f else 1.0f
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    if (isSelected) {
                                        listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                                    } else {
                                        listOf(primaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.3f))
                                    }
                                )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = VaultShape.small
                            )
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = item.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
