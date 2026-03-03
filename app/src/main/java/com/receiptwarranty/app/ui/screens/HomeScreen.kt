package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyFilter
import com.receiptwarranty.app.data.sync.SyncStatus
import com.receiptwarranty.app.ui.components.BannerType
import com.receiptwarranty.app.ui.components.DestructiveConfirmDialog
import com.receiptwarranty.app.ui.components.DefaultTopBar
import com.receiptwarranty.app.ui.components.EmptyStateType
import com.receiptwarranty.app.ui.components.EmptyStateView
import com.receiptwarranty.app.ui.components.ItemCard
import com.receiptwarranty.app.ui.components.SelectionTopBar
import com.receiptwarranty.app.ui.components.SyncStatusBanner
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.viewmodel.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onWarrantyFilterChange: (WarrantyFilter) -> Unit,
    onTagToggle: (String) -> Unit,
    onItemClick: (ReceiptWarranty) -> Unit,
    onEnterSelectionMode: (ReceiptWarranty) -> Unit,
    onEnterSelectionModeEmpty: () -> Unit,
    onSelectItem: (Long) -> Unit,
    onExitSelectionMode: () -> Unit,
    onDeleteSelected: () -> Unit,
    onShareSelected: (() -> Unit)? = null,
    onRetrySync: (() -> Unit)? = null,
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val bannerVisible = uiState.isOffline ||
        uiState.syncStatus is SyncStatus.Syncing ||
        uiState.syncStatus is SyncStatus.Error
    val bannerType = when {
        uiState.syncStatus is SyncStatus.Error -> BannerType.SYNC_ERROR
        uiState.syncStatus is SyncStatus.Syncing -> BannerType.SYNCING
        else -> BannerType.OFFLINE
    }
    val bannerMsg = when (val s = uiState.syncStatus) {
        is SyncStatus.Error -> s.message.ifBlank { "Sync failed" }
        else -> ""
    }

    val isSyncing = uiState.syncStatus is SyncStatus.Syncing
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onSync()
        }
    }

    LaunchedEffect(isSyncing) {
        if (!isSyncing) pullRefreshState.endRefresh()
    }

    Scaffold(
        topBar = {
            when {
                uiState.isSelectionMode -> SelectionTopBar(
                    selectedCount = uiState.selectedIds.size,
                    onDelete = { showDeleteDialog = true },
                    onShare = if (uiState.selectedIds.size == 1) onShareSelected else null,
                    onExitSelection = onExitSelectionMode
                )
                showSearch -> HomeSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClose = {
                        showSearch = false
                        onSearchQueryChange("")
                    }
                )
                else -> DefaultTopBar(
                    title = "Vault",
                    onSearchClick = { showSearch = true },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item {
                    SyncStatusBanner(
                        visible = bannerVisible,
                        type = bannerType,
                        message = bannerMsg,
                        onRetry = if (bannerType == BannerType.SYNC_ERROR) onRetrySync else null
                    )
                }

                if (!uiState.isSelectionMode) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            WarrantyFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = uiState.warrantyFilter == filter,
                                    onClick = { onWarrantyFilterChange(filter) },
                                    label = { Text(filter.displayName) }
                                )
                            }
                        }
                    }

                }

                if (uiState.items.isEmpty()) {
                    item {
                        EmptyStateView(
                            type = if (uiState.searchQuery.isNotBlank() || uiState.warrantyFilter != WarrantyFilter.ALL)
                                EmptyStateType.NO_SEARCH_RESULTS
                            else EmptyStateType.EMPTY,
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                } else {
                    items(uiState.items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = uiState.selectedIds.contains(item.id),
                            onLongClick = { onEnterSelectionMode(item) },
                            onSelect = { onSelectItem(item.id) }
                        )
                    }
                }
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    if (showDeleteDialog) {
        DestructiveConfirmDialog(
            title = "Delete ${uiState.selectedIds.size} items?",
            itemDescription = "${uiState.selectedIds.size} selected item(s)",
            onConfirm = {
                onDeleteSelected()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        placeholder = { Text("Search receipts, warranties, bills…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        shape = VaultShape.large,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}

private val WarrantyFilter.displayName: String
    get() = when (this) {
        WarrantyFilter.ALL -> "All"
        WarrantyFilter.ALL_RECEIPTS -> "Receipts"
        WarrantyFilter.ALL_WARRANTIES -> "Warranties"
        WarrantyFilter.ALL_BILLS -> "Bills"
        WarrantyFilter.EXPIRING_SOON -> "Expiring / Due Soon"
        WarrantyFilter.EXPIRED -> "Expired"
        WarrantyFilter.SUBSCRIPTIONS -> "Subscriptions"
    }
