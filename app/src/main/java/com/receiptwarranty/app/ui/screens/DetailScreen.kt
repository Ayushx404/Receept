package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyStatus
import com.receiptwarranty.app.ui.components.CategoryDefaults
import com.receiptwarranty.app.ui.components.DestructiveConfirmDialog
import com.receiptwarranty.app.ui.components.DetailTopBar
import com.receiptwarranty.app.ui.components.EmptyStateType
import com.receiptwarranty.app.ui.components.EmptyStateView
import com.receiptwarranty.app.ui.components.FullscreenImageViewer
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.util.ShareHelper
import com.receiptwarranty.app.viewmodel.SettingsViewModel
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptwarranty.app.viewmodel.DetailViewModel
import com.receiptwarranty.app.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Long,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onEdit: (ReceiptWarranty) -> Unit
) {
    val item by viewModel.getItem(itemId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }
    var showShareOptions by remember { mutableStateOf(false) }
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appearanceSettings by settingsViewModel.appearanceSettings.collectAsStateWithLifecycle()
    val iconStyle = appearanceSettings.iconStyle

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    when {
        item == null -> {
            // Loading / not found
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val entry = item!!
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    DetailTopBar(
                        title = entry.title,
                        onBack = onBack,
                        onEdit = { onEdit(entry) },
                        onArchive = { 
                            if (entry.isArchived) viewModel.unarchiveItem(itemId)
                            else viewModel.archiveItem(itemId)
                        },
                        isArchived = entry.isArchived,
                        onShare = { showShareOptions = true },
                        onDelete = { showDeleteDialog = true },
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Hero image — tap to open fullscreen viewer
                    if (entry.imageUri != null) {
                        Spacer(Modifier.height(Spacing.md))
                        Box(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                            SubcomposeAsyncImage(
                                model = entry.imageUri,
                                contentDescription = "Receipt/Warranty image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(VaultShape.large)
                                    .clickable { showFullscreen = true },
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                                },
                                error = {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .background(color = CategoryDefaults.getCategoryColor(entry.category).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            com.receiptwarranty.app.ui.theme.CategoryIcons.getIcon(entry.category, iconStyle),
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = CategoryDefaults.getCategoryColor(entry.category).copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            )
                        }
                        if (showFullscreen) {
                            FullscreenImageViewer(
                                imageUri = entry.imageUri!!,
                                onDismiss = { showFullscreen = false }
                            )
                        }
                    } else {
                        Spacer(Modifier.height(Spacing.md))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg)
                                .height(200.dp)
                                .clip(VaultShape.large)
                                .background(color = CategoryDefaults.getCategoryColor(entry.category).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                com.receiptwarranty.app.ui.theme.CategoryIcons.getIcon(entry.category, iconStyle),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = CategoryDefaults.getCategoryColor(entry.category)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        // Title + status badge
                        Text(entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(Spacing.xs))
                        if (entry.type == ReceiptType.WARRANTY && entry.warrantyExpiryDate != null) {
                            val status = entry.warrantyStatus()
                            val (statusColor, statusLabel) = when (status) {
                                WarrantyStatus.VALID -> VaultColors.statusValid to "Active"
                                WarrantyStatus.EXPIRING_SOON -> VaultColors.statusExpiringSoon to "Expiring Soon"
                                WarrantyStatus.EXPIRED -> VaultColors.statusExpired to "Expired"
                                WarrantyStatus.NO_WARRANTY -> MaterialTheme.colorScheme.outline to ""
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.12f), VaultShape.pill)
                                    .padding(horizontal = Spacing.md, vertical = 6.dp)
                            ) {
                                Text(
                                    statusLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(Spacing.xl))

                        // Info card — Curved box for details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = VaultShape.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                            ) {
                                if (entry.price != null) {
                                    InfoRow(
                                        Icons.Default.Store, // Ideally attach_money
                                        "Amount",
                                        CurrencyUtils.formatRupee(entry.price)
                                    )
                                }
                                InfoRow(
                                    Icons.Default.CalendarToday,
                                    "Purchase Date",
                                    entry.purchaseDate?.let { dateFormatter.format(Date(it)) } ?: "—"
                                )
                                if (entry.type == ReceiptType.WARRANTY) {
                                    InfoRow(
                                        Icons.Default.CalendarToday,
                                        "Warranty Expires",
                                        entry.warrantyExpiryDate?.let { dateFormatter.format(Date(it)) } ?: "—"
                                    )
                                    if (entry.category != null) {
                                        InfoRow(Icons.Default.Category, "Category", entry.category!!)
                                    }
                                    if (entry.reminderDays != null) {
                                        InfoRow(
                                            Icons.Default.NotificationsActive,
                                            "Reminder",
                                            entry.reminderDays!!.displayName
                                        )
                                    }
                                }
                                if (entry.type == ReceiptType.BILL || entry.type == ReceiptType.SUBSCRIPTION) {
                                    InfoRow(
                                        Icons.Default.CheckCircle,
                                        "Payment Status",
                                        if (entry.isPaid) "Paid" else "Pending"
                                    )
                                    if (entry.isPaid && entry.lastPaidDate != null) {
                                        InfoRow(
                                            Icons.Default.CalendarToday,
                                            "Last Payment Date",
                                            dateFormatter.format(Date(entry.lastPaidDate))
                                        )
                                    }
                                    if (entry.billingCycle != null) {
                                        InfoRow(
                                            Icons.Default.Payment,
                                            "Billing Cycle",
                                            entry.billingCycle
                                        )
                                    }
                                    if (entry.category != null) {
                                        InfoRow(Icons.Default.Category, "Category", entry.category!!)
                                    }
                                }
                            }
                        }

                        if (entry.type == ReceiptType.BILL || entry.type == ReceiptType.SUBSCRIPTION) {
                            if (!entry.paymentHistory.isNullOrBlank()) {
                                Spacer(Modifier.height(Spacing.lg))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = VaultShape.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(Spacing.lg)) {
                                        Text(
                                            "Payment History",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(Spacing.md))
                                        val paymentDates = entry.paymentHistory!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }.sortedDescending()
                                        for (timestamp in paymentDates) {
                                            val date = timestamp.toLongOrNull()
                                            if (date != null) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = VaultColors.statusValid
                                                    )
                                                    Spacer(Modifier.width(Spacing.sm))
                                                    Text(
                                                        text = dateFormatter.format(Date(date)),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (!entry.isPaid) {
                                Spacer(Modifier.height(Spacing.lg))
                                Button(
                                    onClick = { viewModel.markAsPaid(itemId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(Spacing.sm))
                                    Text("Mark as Paid Today")
                                }
                            }
                        }

                        if (!entry.tags.isNullOrBlank()) {
                            Spacer(Modifier.height(Spacing.lg))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = VaultShape.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text(
                                        "Tags", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(Spacing.md))
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        val tagsList = entry.tags!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        for (tag in tagsList) {
                                            Surface(
                                                shape = VaultShape.pill,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                border = null
                                            ) {
                                                Text(
                                                    text = tag,
                                                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = 4.dp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!entry.notes.isNullOrBlank()) {
                            Spacer(Modifier.height(Spacing.lg))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = VaultShape.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text(
                                        "Notes", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(
                                        entry.notes!!, 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                DestructiveConfirmDialog(
                    title = "Delete \"${entry.title}\"?",
                    itemDescription = "This item",
                    includesCloudWarning = true,
                    onConfirm = {
                        viewModel.deleteById(itemId) { onBack() }
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            if (showShareOptions) {
                val shareResult by viewModel.shareCardResult.collectAsStateWithLifecycle()
                com.receiptwarranty.app.ui.components.SharePreviewBottomSheet(
                    shareResult = shareResult,
                    onGenerate = { theme -> viewModel.generateShareCard(context, entry, theme) },
                    onDismiss = { showShareOptions = false },
                    sheetState = shareSheetState
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.padding(start = Spacing.sm)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
