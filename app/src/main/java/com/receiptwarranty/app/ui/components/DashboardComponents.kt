package com.receiptwarranty.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.twotone.Shield
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiptwarranty.app.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptwarranty.app.data.IconPackStyle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyStatus
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import com.receiptwarranty.app.ui.theme.VaultShape
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.geometry.Size

@Composable
fun ProtectionSnapshotCard(
    totalItems: Int,
    needsAttention: Int,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appearanceSettings by settingsViewModel.appearanceSettings.collectAsStateWithLifecycle()
    val iconStyle = appearanceSettings.iconStyle

    val shieldIcon = when (iconStyle) {
        IconPackStyle.LUCIDE -> Icons.Outlined.Shield
        IconPackStyle.PHOSPHOR_DUOTONE -> Icons.Rounded.Shield
        IconPackStyle.MATERIAL_TWOTONE -> Icons.TwoTone.Shield
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snapshot")

    // Slow gradient phase (15s cycle) — Samsung-style calm shift
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    // Breathing glow for attention badge (if items need attention)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Slow shield rotation (20s full turn)
    val shieldRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shield"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val targetStartColor = if (needsAttention > 0) VaultColors.statusExpiringSoon else primaryColor
    val targetEndColor = if (needsAttention > 0) VaultColors.statusExpired else tertiaryColor
    
    val startColor by animateColorAsState(targetValue = targetStartColor, label = "startColor")
    val endColor by animateColorAsState(targetValue = targetEndColor, label = "endColor")

    // Smooth gradient that changes based on state
    val gradient = Brush.linearGradient(
        colors = listOf(
            startColor,
            lerp(startColor, endColor, phase),
            endColor.copy(alpha = 0.8f)
        ),
        start = Offset(0f, 0f),
        end = Offset(1200f, 800f)
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "snapshotScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple for custom scale
                onClick = { /* No-op, just visual feedback */ }
            ),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed hardcoded drop shadow
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(Spacing.xl)
        ) {
            // Slowly rotating shield watermark (top-right)
            Icon(
                imageVector = shieldIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer { rotationZ = shieldRotation },
                tint = onPrimaryColor.copy(alpha = 0.08f)
            )

            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    "Protection Snapshot",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(Spacing.sm))
                Row(verticalAlignment = Alignment.Bottom) {
                    DigitTicker(
                        value = totalItems,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "items tracked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (needsAttention > 0) {
                    Spacer(Modifier.height(Spacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Breathing glow dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(VaultColors.statusExpiringSoon.copy(alpha = glowAlpha))
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            "$needsAttention need${if (needsAttention == 1) "s" else ""} attention",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DigitTicker(
    value: Int,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val valueString = value.toString()
        valueString.forEachIndexed { index, char ->
            Digit(char, style, fontWeight, color)
        }
    }
}

@Composable
private fun Digit(
    char: Char,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight,
    color: Color
) {
    AnimatedContent(
        targetState = char,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            } else {
                (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
            }
        },
        label = "digitTicker"
    ) { targetChar ->
        Text(
            text = targetChar.toString(),
            style = style,
            fontWeight = fontWeight,
            color = color,
            softWrap = false
        )
    }
}

@Composable
fun ActionPillButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = VaultShape.pill,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    val categoryColor = CategoryDefaults.getCategoryColor(category)
    Surface(
        shape = VaultShape.small,
        color = categoryColor.copy(alpha = 0.12f)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            color = categoryColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ExpiringSoonCard(
    item: ReceiptWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = VaultShape.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (item.category != null) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val daysRemaining = item.warrantyExpiryDate?.let { expiry ->
                val now = System.currentTimeMillis()
                ((expiry - now) / (24 * 60 * 60 * 1000)).toInt()
            } ?: 0

            Surface(
                shape = VaultShape.small,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = "$daysRemaining days",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryStatRow(
    categoryCount: CategoryCount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                text = categoryCount.category,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${categoryCount.count} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StatusChip(
    status: WarrantyStatus,
    labelOverride: String? = null,
    colorOverride: Color? = null,
    bgColorOverride: Color? = null
) {
    val (defaultText, defaultColor, defaultBgColor) = when (status) {
        WarrantyStatus.VALID -> Triple("Active", VaultColors.statusValid, VaultColors.statusValidBg)
        WarrantyStatus.EXPIRING_SOON -> Triple("Expiring Soon", VaultColors.statusExpiringSoon, VaultColors.statusExpiringSoonBg)
        WarrantyStatus.EXPIRED -> Triple("Expired", VaultColors.statusExpired, VaultColors.statusExpiredBg)
        WarrantyStatus.NO_WARRANTY -> Triple("Receipt", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
    }

    val text = labelOverride ?: defaultText
    val color = colorOverride ?: defaultColor
    val bgColor = bgColorOverride ?: (colorOverride?.copy(alpha = 0.12f) ?: defaultBgColor)

    Surface(
        shape = VaultShape.small,
        color = bgColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AssetSummaryCard(
    itemsCount: Int,
    monthlyBurn: Double,
    currencySymbol: String,
    topCategory: String? = null,
    modifier: Modifier = Modifier
) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Items Protected",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$itemsCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Subscription",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.0f", monthlyBurn)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (topCategory != null) {
                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(Spacing.lg))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Top Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = topCategory,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCounter(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, VaultShape.pill)
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CoverageGrid(
    receiptCount: Int,
    warrantyCount: Int,
    billCount: Int,
    subscriptionCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            CoverageActionCard(
                count = receiptCount,
                label = "Receipts",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            CoverageActionCard(
                count = warrantyCount,
                label = "Warranties",
                icon = Icons.Outlined.Shield,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            CoverageActionCard(
                count = billCount,
                label = "Bills",
                icon = Icons.Default.CreditCard,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            CoverageActionCard(
                count = subscriptionCount,
                label = "Subscriptions",
                icon = Icons.Default.Refresh,
                color = VaultColors.statusSyncing,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CoverageActionCard(
    count: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), VaultShape.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttentionCard(
    items: List<ReceiptWarranty>,
    currencySymbol: String,
    onItemClick: (ReceiptWarranty) -> Unit,
    onViewAllClick: () -> Unit,
    onPayClick: (ReceiptWarranty) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = VaultColors.statusExpiringSoon.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = VaultColors.statusExpiringSoon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = "Attention Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onViewAllClick) {
                    Text("View All")
                }
            }
            
            items.take(3).forEach { item ->
                AttentionItemRow(
                    item = item,
                    currencySymbol = currencySymbol,
                    onClick = { onItemClick(item) },
                    onPayClick = { onPayClick(item) }
                )
                if (item != items.take(3).last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttentionItemRow(
    item: ReceiptWarranty,
    currencySymbol: String,
    onClick: () -> Unit,
    onPayClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.type) {
                            ReceiptType.WARRANTY -> Icons.Outlined.Shield
                            ReceiptType.BILL -> Icons.Default.CreditCard
                            ReceiptType.SUBSCRIPTION -> Icons.Default.Refresh
                            else -> Icons.Default.Assignment
                        },
                        contentDescription = null,
                        tint = VaultColors.statusExpiringSoon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = when (item.type) {
                        ReceiptType.WARRANTY -> "Expires ${formatDaysUntil(item.warrantyExpiryDate)}"
                        ReceiptType.BILL, ReceiptType.SUBSCRIPTION -> "Due ${formatDaysUntil(item.warrantyExpiryDate)}"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = VaultColors.statusExpiringSoon
                )
            }
            
            AssistChip(
                onClick = onPayClick,
                label = { 
                    Text(
                        text = when (item.type) {
                            ReceiptType.WARRANTY -> "Renew"
                            else -> "Pay"
                        }
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = VaultColors.statusExpiringSoon.copy(alpha = 0.15f),
                    labelColor = VaultColors.statusExpiringSoon
                ),
                border = null,
                shape = VaultShape.small
            )
        }
    }
}

@Composable
fun MonthlyOverviewCard(
    activeSubscriptions: Int,
    monthlyCost: Double,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
        Row(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(VaultColors.statusSyncing.copy(alpha = 0.1f), VaultShape.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = VaultColors.statusSyncing,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(Modifier.width(Spacing.lg))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${String.format("%.0f", monthlyCost)} / mo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$activeSubscriptions active subscriptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDaysUntil(timestamp: Long?): String {
    if (timestamp == null) return ""
    val now = System.currentTimeMillis()
    val days = ((timestamp - now) / (24 * 60 * 60 * 1000)).toInt()
    return when {
        days < 0 -> "${-days} days ago"
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        days <= 7 -> "in $days days"
        else -> "in $days days"
    }
}

@Composable
fun ProtectionOverviewCard(
    activeCount: Int,
    expiredCount: Int,
    expiringCount: Int,
    totalWorth: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Protection Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(Modifier.height(Spacing.md))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewCell(
                    value = activeCount.toString(),
                    label = "Active",
                    color = VaultColors.statusValid
                )
                OverviewCell(
                    value = expiredCount.toString(),
                    label = "Expired",
                    color = VaultColors.statusExpired
                )
            }
            
            Spacer(Modifier.height(Spacing.md))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewCell(
                    value = expiringCount.toString(),
                    label = "Expiring",
                    color = VaultColors.statusExpiringSoon
                )
                OverviewCell(
                    value = "$currencySymbol${String.format("%.0f", totalWorth)}",
                    label = "Worth",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OverviewCell(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ValueDistributionCard(
    valueByType: Map<String, Double>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = "Value Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(Spacing.lg))
            
            if (valueByType.isNotEmpty()) {
                val maxValue = valueByType.values.maxOrNull() ?: 1.0
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    valueByType.forEach { (type, value) ->
                        if (value > 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$currencySymbol${String.format("%.0f", value)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                val animatedProgress by animateFloatAsState(
                                    targetValue = (value / maxValue).toFloat(),
                                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                                    label = "progress"
                                )
                                
                                LinearProgressIndicator(
                                    progress = animatedProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(VaultShape.pill),
                                    color = when(type) {
                                        "Warranty" -> MaterialTheme.colorScheme.primary
                                        "Billing" -> MaterialTheme.colorScheme.secondary
                                        "Subscription" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp,
    primaryColor: Color,
    trackColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Canvas(modifier = modifier) {
        val sweepAngle = animatedProgress * 360f
        val stroke = strokeWidth.toPx()
        val diameter = size.minDimension - stroke
        
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            size = Size(diameter, diameter)
        )
        
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            size = Size(diameter, diameter)
        )
    }
}

@Composable
fun CoveragePieChart(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (distribution.isEmpty()) return
    
    val total = distribution.values.sum()
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        VaultColors.statusSyncing
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Coverage by Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(Modifier.height(Spacing.md))
            
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                distribution.entries.forEachIndexed { index, entry ->
                    val percentage = (entry.value.toFloat() / total) * 100
                    val color = colors.getOrElse(index) { Color.Gray }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(80.dp)
                        )
                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(VaultShape.small),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f),
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "${entry.value} (${percentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentShelf(
    items: List<ReceiptWarranty>,
    onItemClick: (ReceiptWarranty) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Document Shelf", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(Spacing.md))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                ShelfItemCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun ShelfItemCard(
    item: ReceiptWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                
                // Type badge
                Surface(
                    modifier = Modifier.padding(Spacing.xs).align(Alignment.TopEnd),
                    shape = VaultShape.pill,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        item.type.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(Spacing.sm)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (item.category != null) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

