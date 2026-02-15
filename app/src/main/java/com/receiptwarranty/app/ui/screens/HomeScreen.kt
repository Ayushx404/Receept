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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.ui.components.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<ReceiptWarranty>,
    searchQuery: String,
    warrantyFilter: WarrantyFilter,
    onSearchQueryChange: (String) -> Unit,
    onWarrantyFilterChange: (WarrantyFilter) -> Unit,
    onItemClick: (ReceiptWarranty) -> Unit,
    onAddClick: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedItems: Set<Long> = emptySet(),
    onEnterSelectionMode: (ReceiptWarranty) -> Unit = {},
    onSelectItem: (Long) -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onDeleteSelected: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedItems.size} selected") },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = onExitSelectionMode) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isSelectionMode) {
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
                            onClick = { onItemClick(item) },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(item.id),
                            onLongClick = { onEnterSelectionMode(item) },
                            onSelect = { onSelectItem(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${selectedItems.size} items?") },
            text = { Text("This action cannot be undone. Items will be deleted from cloud storage as well.") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        onDeleteSelected()
                        showDeleteDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
