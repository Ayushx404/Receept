package com.receiptwarranty.app.ui.components

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import com.receiptwarranty.app.ui.theme.VaultShape

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
                    .background(MaterialTheme.colorScheme.primary, VaultShape.pill)
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
                    text = "$currencySymbol${String.format(Locale.ROOT, "%.0f", monthlyCost)} / mo",
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
