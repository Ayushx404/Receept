package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiptwarranty.app.data.CategoryCount
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.*
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.viewmodel.MonthlySpending
import com.receiptwarranty.app.viewmodel.StatsViewModel
import com.receiptwarranty.app.viewmodel.ValueFilter
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateToTimeline: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSpending by viewModel.totalSpending.collectAsStateWithLifecycle()
    val upcomingBillsTotal by viewModel.upcomingBillsTotal.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val activeWarrantyCount by viewModel.activeWarrantyCount.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val last6Months by viewModel.last6MonthsSpending.collectAsStateWithLifecycle()
    val distributionByType by viewModel.distributionByType.collectAsStateWithLifecycle()
    val upcomingRenewalsList by viewModel.upcomingRenewalsList.collectAsStateWithLifecycle()
    val totalValueProtected by viewModel.totalValueFiltered.collectAsStateWithLifecycle()
    val valueFilter by viewModel.valueFilter.collectAsStateWithLifecycle()
    val valueByType by viewModel.valueByType.collectAsStateWithLifecycle()
    val expiredWarrantyCount by viewModel.expiredWarrantyCount.collectAsStateWithLifecycle()
    val recentPayments by viewModel.recentPayments.collectAsStateWithLifecycle()
    val expiringSoonCount by viewModel.expiringSoonCount.collectAsStateWithLifecycle()
    val monthlySubscriptionCost by viewModel.monthlySubscriptionCost.collectAsStateWithLifecycle()
    val spendingTrend by viewModel.spendingTrend.collectAsStateWithLifecycle()
    val averageItemValue by viewModel.averageItemValue.collectAsStateWithLifecycle()
    val topCategoryName by viewModel.topCategoryName.collectAsStateWithLifecycle()
    val totalItemsCount by viewModel.totalItemsCount.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showInfoSheet by remember { mutableStateOf<Pair<String, String>?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            
            // 1. Quick Asset Summary (Now with average value and grouped tooltip)
            item {
                StatHeader(
                    title = "Asset Overview",
                    icon = Icons.Default.Security,
                    onInfoClick = {
                        showInfoSheet = "Asset Overview" to "Key statistics about your vault:\n\n• Items Protected: Total number of records tracked.\n• Monthly Subscription: Recurring costs across all services.\n• Top Category: The category with the most items."
                    }
                )
                AssetSummaryCard(
                    itemsCount = totalItemsCount,
                    monthlyBurn = monthlySubscriptionCost ?: 0.0,
                    currencySymbol = currencySymbol,
                    topCategory = topCategoryName
                )
            }

            // 2. Coverage Health (New!)
            item {
                StatHeader(
                    title = "Coverage Status",
                    icon = Icons.Default.CheckCircle,
                    onInfoClick = {
                        showInfoSheet = "Coverage Status" to "Breakdown of your warranty health:\n\n• Active: Under full protection.\n• Expiring: Needs attention soon.\n• Expired: No longer protected."
                    }
                )
                WarrantyHealthCard(
                    active = activeWarrantyCount,
                    expiring = expiringSoonCount,
                    expired = expiredWarrantyCount
                )
            }

            // 3. Value Distribution
            item {
                ValueDistributionCard(
                    valueByType = valueByType,
                    currencySymbol = currencySymbol
                )
            }

            // 4. Coverage Distribution
            item {
                StatHeader(
                    title = "Item Types",
                    icon = Icons.Default.Category,
                    onInfoClick = { 
                        showInfoSheet = "Item Distribution" to "Shows the relative breakdown of your receipts, warranties, and subscriptions by quantity."
                    }
                )
                Card(
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
                        PremiumDonutChart(distribution = distributionByType)
                    }
                }
            }

            // 4. Monthly Spending
            item {
                StatHeader(
                    title = "Monthly Spending",
                    icon = Icons.Default.TrendingUp,
                    onInfoClick = {
                        showInfoSheet = "Monthly Spending" to "Visualizes your spending habits over the last 6 months to help you track trends."
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
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
                    Column(modifier = Modifier.padding(Spacing.lg).fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Previous Months",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Total Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                             Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format(Locale.ROOT, "%.0f", totalSpending ?: 0.0)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isUp = spendingTrend > 0
                                    val isDown = spendingTrend < 0
                                    if (isUp || isDown) {
                                        Icon(
                                            imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (isUp) MaterialTheme.colorScheme.error else VaultColors.statusValid,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(Spacing.xs))
                                        Text(
                                            text = "${String.format(Locale.ROOT, "%.1f", Math.abs(spendingTrend))}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isUp) MaterialTheme.colorScheme.error else VaultColors.statusValid
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Wait for a small spacing
                        Spacer(Modifier.height(Spacing.xl))
                        
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            PremiumSpendingChart(
                                data = last6Months, 
                                primaryColor = MaterialTheme.colorScheme.primary,
                                currencySymbol = currencySymbol
                            )
                        }
                    }
                }
            }

            // 4b. Monthly Subscription Cost (Burn Rate)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Monthly Subscription Burn",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Total recurring monthly cost",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$currencySymbol${String.format(Locale.ROOT, "%.2f", monthlySubscriptionCost ?: 0.0)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 5. Timeline Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    onClick = onNavigateToTimeline
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), VaultShape.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Timeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "View upcoming expirations",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Timeline",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. Upcoming Renewals
            if (upcomingRenewalsList.isNotEmpty()) {
                item {
                    SectionHeader("Upcoming Renewals")
                    Spacer(Modifier.height(Spacing.sm))
                    Card(
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
                        Column {
                            upcomingRenewalsList.take(5).forEachIndexed { index, item ->
                                UpcomingRenewalRow(item = item, currencySymbol = currencySymbol)
                                if (index < upcomingRenewalsList.take(5).size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = Spacing.lg),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Recent Payments
            if (recentPayments.isNotEmpty()) {
                item {
                    StatHeader(
                        title = "Recent Payments",
                        onInfoClick = {
                            showInfoSheet = "Recent Payments" to "A log of your most recent paid bills and subscription renewals confirmed in the app."
                        }
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                        Column {
                            recentPayments.forEachIndexed { index, payment ->
                                RecentPaymentItem(payment, currencySymbol)
                                if (index < recentPayments.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = Spacing.lg),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Info Bottom Sheet Overlay
        if (showInfoSheet != null) {
            InfoBottomSheet(
                title = showInfoSheet!!.first,
                description = showInfoSheet!!.second,
                sheetState = sheetState,
                onDismiss = { showInfoSheet = null }
            )
        }
    }
}

@Composable
fun RecentPaymentItem(payment: ReceiptWarranty, currencySymbol: String) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(VaultColors.statusValid.copy(alpha = 0.1f), VaultShape.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = VaultColors.statusValid
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(payment.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(
                "Paid on ${payment.lastPaidDate?.let { dateFormatter.format(Date(it)) } ?: "unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "$currencySymbol${String.format(Locale.ROOT, "%.0f", payment.price ?: 0.0)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = Spacing.xs)
    )
}


// Note: DonutChart and SpendingChart moved to AnalyticsComponents.kt

@Composable
fun UpcomingRenewalRow(
    item: ReceiptWarranty,
    currencySymbol: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Due ${formatDaysUntilStat(item.warrantyExpiryDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = VaultColors.statusExpiringSoon
            )
        }
        Text(
            text = "$currencySymbol${String.format(Locale.ROOT, "%.2f", item.price ?: 0.0)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDaysUntilStat(timestamp: Long?): String {
    if (timestamp == null) return ""
    val now = System.currentTimeMillis()
    val days = ((timestamp - now) / (24 * 60 * 60 * 1000)).toInt()
    return when {
        days < 0 -> "${-days} days ago"
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        else -> "in $days days"
    }
}
