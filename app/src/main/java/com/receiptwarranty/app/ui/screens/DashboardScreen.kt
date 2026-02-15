package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.ui.components.CategoryStatRow
import com.receiptwarranty.app.ui.components.ExpiringSoonCard
import com.receiptwarranty.app.ui.components.StatCard
import com.receiptwarranty.app.viewmodel.ReceiptWarrantyUiState
import com.receiptwarranty.app.viewmodel.ReceiptWarrantyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: ReceiptWarrantyUiState,
    onNavigateToAdd: (ReceiptType) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToExpiringSoon: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExportCSV: () -> Unit,
    viewModel: ReceiptWarrantyViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickStatsSection(uiState)
            }

            item {
                ActionButtonsSection(
                    onAddReceipt = { onNavigateToAdd(ReceiptType.RECEIPT) },
                    onAddWarranty = { onNavigateToAdd(ReceiptType.WARRANTY) }
                )
            }

            if (uiState.expiringSoonItems.isNotEmpty()) {
                item {
                    ExpiringSoonSection(
                        items = uiState.expiringSoonItems,
                        onViewAll = onNavigateToExpiringSoon,
                        onItemClick = { }
                    )
                }
            }

            if (uiState.categoryStats.isNotEmpty()) {
                item {
                    CategoriesSection(
                        categories = uiState.categoryStats,
                        onCategoryClick = onNavigateToCategory
                    )
                }
            }

            item {
                QuickActionsSection(
                    onExportCSV = onExportCSV,
                    onViewAllItems = onNavigateToHome
                )
            }
        }
    }
}

@Composable
private fun QuickStatsSection(uiState: ReceiptWarrantyUiState) {
    Column {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Receipt,
                label = "Receipts",
                value = uiState.receiptCount.toString(),
                color = MaterialTheme.colorScheme.primary
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Verified,
                label = "Warranties",
                value = uiState.warrantyCount.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                label = "Active",
                value = uiState.activeWarrantyCount.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Warning,
                label = "Expiring",
                value = uiState.expiringSoonCount.toString(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onAddReceipt: () -> Unit,
    onAddWarranty: () -> Unit
) {
    Column {
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onAddReceipt,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Receipt")
            }

            FilledTonalButton(
                onClick = onAddWarranty,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Warranty")
            }
        }
    }
}

@Composable
private fun ExpiringSoonSection(
    items: List<com.receiptwarranty.app.data.ReceiptWarranty>,
    onViewAll: () -> Unit,
    onItemClick: (com.receiptwarranty.app.data.ReceiptWarranty) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Expiring Soon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onViewAll) {
                Text("View All")
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        items.take(3).forEach { item ->
            ExpiringSoonCard(
                item = item,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<com.receiptwarranty.app.data.CategoryCount>,
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "By Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            categories.take(5).forEach { categoryCount ->
                CategoryStatRow(
                    categoryCount = categoryCount,
                    onClick = { onCategoryClick(categoryCount.category) }
                )
            }

            if (categories.size > 5) {
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Categories")
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onExportCSV: () -> Unit,
    onViewAllItems: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onExportCSV,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export CSV")
            }

            FilledTonalButton(
                onClick = onViewAllItems,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View All")
            }
        }
    }
}
