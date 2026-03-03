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
import androidx.compose.ui.Modifier
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.ChildTopBar
import com.receiptwarranty.app.ui.components.EmptyStateType
import com.receiptwarranty.app.ui.components.EmptyStateView
import com.receiptwarranty.app.ui.components.ItemCard
import com.receiptwarranty.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    items: List<ReceiptWarranty>,
    onItemClick: (ReceiptWarranty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            ChildTopBar(
                title = "Vault Archive",
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (items.isEmpty()) {
            EmptyStateView(
                type = EmptyStateType.EMPTY,
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
