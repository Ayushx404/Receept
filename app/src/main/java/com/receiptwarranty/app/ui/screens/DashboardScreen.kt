package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.ActionPillButton
import com.receiptwarranty.app.ui.components.AttentionCard
import com.receiptwarranty.app.ui.components.CoverageGrid
import com.receiptwarranty.app.ui.components.MonthlyOverviewCard
import com.receiptwarranty.app.ui.components.QuickStatCard
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.viewmodel.DashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    currencySymbol: String,
    onNavigateToExpiringSoon: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onItemClick: (ReceiptWarranty) -> Unit,
    onAddClick: (ReceiptType) -> Unit,
    onExportCSV: () -> Unit,
    onPayClick: (ReceiptWarranty) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Overview", 
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // 1. Coverage Grid (4-type breakdown)
            item {
                CoverageGrid(
                    receiptCount = uiState.receiptCount,
                    warrantyCount = uiState.warrantyCount,
                    billCount = uiState.billCount,
                    subscriptionCount = uiState.subscriptionCount
                )
            }

            // 2. Attention Card (merged expiring/due)
            if (uiState.upcomingRenewals.isNotEmpty()) {
                item {
                    AttentionCard(
                        items = uiState.upcomingRenewals,
                        currencySymbol = currencySymbol,
                        onItemClick = onItemClick,
                        onViewAllClick = onNavigateToExpiringSoon,
                        onPayClick = onPayClick
                    )
                }
            }

            // 3. Monthly Overview Card (subscriptions)
            item {
                MonthlyOverviewCard(
                    activeSubscriptions = uiState.subscriptionCount,
                    monthlyCost = uiState.monthlySubscriptionCost,
                    currencySymbol = currencySymbol,
                    onClick = onNavigateToStats
                )
            }

            // 4. By Category
            if (uiState.categoryStats.isNotEmpty()) {
                item {
                    Column {
                        SectionHeader("By Category")
                        Spacer(Modifier.height(Spacing.sm))
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
                                uiState.categoryStats.forEachIndexed { index, stat ->
                                    CategoryRow(
                                        name = stat.category,
                                        count = stat.count,
                                        onClick = { onNavigateToCategory(stat.category) }
                                    )
                                    if (index < uiState.categoryStats.size - 1) {
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
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    name: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(2.dp)
                        .background(MaterialTheme.colorScheme.primary, VaultShape.pill)
                )
                Spacer(Modifier.width(Spacing.md))
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$count ${if (count == 1) "item" else "items"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Spacing.sm))
                Icon(
                    Icons.Default.ChevronRight, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
