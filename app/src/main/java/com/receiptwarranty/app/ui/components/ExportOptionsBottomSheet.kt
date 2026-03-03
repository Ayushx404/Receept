package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.util.ExportCategory
import com.receiptwarranty.app.util.ExportDateRange
import com.receiptwarranty.app.util.ExportDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionsBottomSheet(
    onExport: (ExportCategory, ExportDateRange, Long?, Long?, Boolean, ExportDestination) -> Unit,
    onDismiss: () -> Unit,
    isExporting: Boolean = false,
    exportProgress: Float = 0f
) {
    var selectedCategory by remember { mutableStateOf(ExportCategory.ALL) }
    var selectedDateRange by remember { mutableStateOf(ExportDateRange.ALL_TIME) }
    var includeImages by remember { mutableStateOf(true) }
    var selectedDestination by remember { mutableStateOf(ExportDestination.SHARE) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Export Data",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Create a backup of your Vault data in JSON format",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Category Selection
            SectionHeader(
                icon = Icons.Default.FilterList,
                title = "Category"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CategorySelector(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Date Range Selection
            SectionHeader(
                icon = Icons.Default.CalendarMonth,
                title = "Date Range"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DateRangeSelector(
                selected = selectedDateRange,
                onSelect = { selectedDateRange = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Include Images Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = VaultShape.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include Images",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Embed images as Base64 (larger file)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeImages,
                        onCheckedChange = { includeImages = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Destination Selection
            SectionHeader(
                icon = Icons.Default.Download,
                title = "Save To"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DestinationCard(
                    icon = Icons.Default.Share,
                    title = "Share",
                    description = "Send via app",
                    isSelected = selectedDestination == ExportDestination.SHARE,
                    onClick = { selectedDestination = ExportDestination.SHARE },
                    modifier = Modifier.weight(1f)
                )
                DestinationCard(
                    icon = Icons.Default.Download,
                    title = "Downloads",
                    description = "Save locally",
                    isSelected = selectedDestination == ExportDestination.DOWNLOADS,
                    onClick = { selectedDestination = ExportDestination.DOWNLOADS },
                    modifier = Modifier.weight(1f)
                )
            }

            // Progress indicator
            if (isExporting) {
                Spacer(modifier = Modifier.height(20.dp))
                Column {
                    Text(
                        text = "Exporting...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export Button
            Button(
                onClick = {
                    val startDate = if (selectedDateRange == ExportDateRange.CUSTOM) {
                        System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    } else null
                    val endDate = if (selectedDateRange == ExportDateRange.CUSTOM) {
                        System.currentTimeMillis()
                    } else null
                    onExport(
                        selectedCategory,
                        selectedDateRange,
                        startDate,
                        endDate,
                        includeImages,
                        selectedDestination
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isExporting) "Exporting..." else "Export Data")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategorySelector(
    selected: ExportCategory,
    onSelect: (ExportCategory) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        ExportCategory.entries.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected == category,
                        onClick = { onSelect(category) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == category,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                val icon = when (category) {
                    ExportCategory.ALL -> Icons.Default.Warning
                    ExportCategory.RECEIPTS -> Icons.Default.Receipt
                    ExportCategory.WARRANTIES -> Icons.Default.Warning
                    ExportCategory.BILLS -> Icons.Default.CreditCard
                    ExportCategory.SUBSCRIPTIONS -> Icons.Default.CreditCard
                }
                val label = when (category) {
                    ExportCategory.ALL -> "All Items"
                    ExportCategory.RECEIPTS -> "Receipts Only"
                    ExportCategory.WARRANTIES -> "Warranties Only"
                    ExportCategory.BILLS -> "Bills Only"
                    ExportCategory.SUBSCRIPTIONS -> "Subscriptions Only"
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DateRangeSelector(
    selected: ExportDateRange,
    onSelect: (ExportDateRange) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        ExportDateRange.entries.forEach { range ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected == range,
                        onClick = { onSelect(range) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == range,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                val label = when (range) {
                    ExportDateRange.ALL_TIME -> "All Time"
                    ExportDateRange.LAST_MONTH -> "Last Month"
                    ExportDateRange.LAST_3_MONTHS -> "Last 3 Months"
                    ExportDateRange.LAST_6_MONTHS -> "Last 6 Months"
                    ExportDateRange.LAST_YEAR -> "Last Year"
                    ExportDateRange.CUSTOM -> "Custom Range"
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DestinationCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = VaultShape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
