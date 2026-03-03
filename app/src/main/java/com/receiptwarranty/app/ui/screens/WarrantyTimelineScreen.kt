package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyStatus
import com.receiptwarranty.app.ui.components.ChildTopBar
import com.receiptwarranty.app.ui.components.EmptyStateType
import com.receiptwarranty.app.ui.components.EmptyStateView
import com.receiptwarranty.app.ui.components.ItemCard
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class TimelineRow(
    val section: String,
    val item: ReceiptWarranty,
    val daysRemaining: Int
)

private enum class TimelineTab(val title: String, val icon: ImageVector, val types: List<ReceiptType>) {
    WARRANTY("Warranty", Icons.Default.Verified, listOf(ReceiptType.WARRANTY)),
    BILL("Bill", Icons.Default.Receipt, listOf(ReceiptType.BILL)),
    SUBSCRIPTION("Subscription", Icons.Default.Payment, listOf(ReceiptType.SUBSCRIPTION))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyTimelineScreen(
    items: List<ReceiptWarranty>,
    onItemClick: (ReceiptWarranty) -> Unit,
    onMarkAsPaid: ((ReceiptWarranty) -> Unit)? = null,
    onRenew: ((ReceiptWarranty) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = TimelineTab.entries.toTypedArray()

    val filteredItems = items.filter { item ->
        tabs[selectedTabIndex].types.contains(item.type) &&
            item.warrantyExpiryDate != null
    }.sortedBy { it.warrantyExpiryDate }

    val timelineRows = filteredItems.map { item ->
        val daysRemaining = ((item.warrantyExpiryDate!! - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
        val section = Instant.ofEpochMilli(item.warrantyExpiryDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
        TimelineRow(section = section, item = item, daysRemaining = daysRemaining)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChildTopBar(
                title = "Timeline",
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }

            if (timelineRows.isEmpty()) {
                EmptyStateView(
                    type = EmptyStateType.EMPTY,
                    customTitle = "No items in timeline",
                    customSubtitle = "Add items with expiry dates to see them in your timeline.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    var previousSection: String? = null
                    timelineRows.forEach { row ->
                        if (row.section != previousSection) {
                            item(key = "header_${row.section}") {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.lg,
                                        vertical = Spacing.md
                                    )
                                ) {
                                    if (previousSection != null) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(bottom = Spacing.md),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    Text(
                                        text = row.section,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            previousSection = row.section
                        }

                        item(key = "item_${row.item.id}") {
                            TimelineItemRow(
                                item = row.item,
                                daysRemaining = row.daysRemaining,
                                onClick = { onItemClick(row.item) },
                                onMarkAsPaid = {
                                    onMarkAsPaid?.invoke(row.item)
                                },
                                onRenew = {
                                    onRenew?.invoke(row.item)
                                },
                                modifier = Modifier.padding(horizontal = Spacing.lg)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: ReceiptWarranty,
    daysRemaining: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMarkAsPaid: (() -> Unit)?,
    onRenew: (() -> Unit)?
) {
    val statusColor = when {
        daysRemaining < 0 -> VaultColors.statusExpired
        daysRemaining <= 7 -> VaultColors.statusExpiringSoon
        else -> VaultColors.statusValid
    }

    val statusText = when {
        daysRemaining < 0 -> "Expired"
        daysRemaining == 0 -> "Due today"
        daysRemaining == 1 -> "1 day"
        else -> "$daysRemaining days"
    }

    Box(modifier = modifier) {
        ItemCard(
            item = item,
            onClick = onClick,
            statusOverride = statusText,
            statusColorOverride = statusColor
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {

            if (item.type == ReceiptType.BILL || item.type == ReceiptType.SUBSCRIPTION) {
                if (!item.isPaid && onMarkAsPaid != null) {
                    IconButton(
                        onClick = onMarkAsPaid,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Mark as Paid",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (item.type == ReceiptType.WARRANTY && onRenew != null) {
                IconButton(
                    onClick = onRenew,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Renew",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
