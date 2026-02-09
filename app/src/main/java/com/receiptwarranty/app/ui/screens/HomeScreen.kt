package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.ui.components.ItemCard

@Composable
fun HomeScreen(
    items: List<ReceiptWarranty>,
    searchQuery: String,
    warrantyFilter: WarrantyFilter,
    onSearchQueryChange: (String) -> Unit,
    onWarrantyFilterChange: (WarrantyFilter) -> Unit,
    onItemClick: (ReceiptWarranty) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by title, company, or notes...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = warrantyFilter == WarrantyFilter.ALL,
                    onClick = { onWarrantyFilterChange(WarrantyFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = warrantyFilter == WarrantyFilter.ALL_RECEIPTS,
                    onClick = { onWarrantyFilterChange(WarrantyFilter.ALL_RECEIPTS) },
                    label = { Text("Receipts") }
                )
                FilterChip(
                    selected = warrantyFilter == WarrantyFilter.ALL_WARRANTIES,
                    onClick = { onWarrantyFilterChange(WarrantyFilter.ALL_WARRANTIES) },
                    label = { Text("Warranties") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = warrantyFilter == WarrantyFilter.EXPIRING_SOON,
                    onClick = { onWarrantyFilterChange(WarrantyFilter.EXPIRING_SOON) },
                    label = { Text("Expiring Soon") }
                )
                FilterChip(
                    selected = warrantyFilter == WarrantyFilter.EXPIRED,
                    onClick = { onWarrantyFilterChange(WarrantyFilter.EXPIRED) },
                    label = { Text("Expired") }
                )
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results found" else "No items yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "Try a different search term"
                            } else {
                                "Tap + to add your first receipt or warranty"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}
