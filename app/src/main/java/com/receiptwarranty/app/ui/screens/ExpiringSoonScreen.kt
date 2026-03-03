package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.ChildTopBar
import com.receiptwarranty.app.ui.components.EmptyStateType
import com.receiptwarranty.app.ui.components.EmptyStateView
import com.receiptwarranty.app.ui.components.ItemCard
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.viewmodel.DashboardViewModel

/**
 * Shows all warranties with EXPIRING_SOON status sorted by warrantyExpiryDate ascending.
 * Navigated to from DashboardScreen's "Expiring Soon" metric card or section header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiringSoonScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onItemClick: (ReceiptWarranty) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val items = uiState.expiringSoonItems.sortedBy { it.warrantyExpiryDate ?: Long.MAX_VALUE }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChildTopBar(
                title = "Expiring Soon", 
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            EmptyStateView(
                type = EmptyStateType.EMPTY,
                customTitle = "All clear!",
                customSubtitle = "No warranties are expiring soon",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
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
