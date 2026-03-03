package com.receiptwarranty.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.WarrantyStatus
import com.receiptwarranty.app.ui.theme.CategoryIcons
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.util.CurrencyUtils
import com.receiptwarranty.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    item: ReceiptWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onSelect: () -> Unit = {},
    statusOverride: String? = null,
    statusColorOverride: Color? = null
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appearanceSettings by settingsViewModel.appearanceSettings.collectAsStateWithLifecycle()
    val iconStyle = appearanceSettings.iconStyle

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isSelected) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Using custom scale animation
                onClick = {
                    if (isSelectionMode) {
                        onSelect()
                    } else {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = VaultShape.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // Category Icon (Replaces Image & Type Badge)
            Card(
                modifier = Modifier.size(64.dp),
                shape = VaultShape.medium,
                colors = CardDefaults.cardColors(
                    containerColor = CategoryDefaults.getCategoryColor(item.category).copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(item.category, iconStyle),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = CategoryDefaults.getCategoryColor(item.category)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (item.category != null) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Category chip - remove duplicate if already shown
                    if (item.category == null) {
                        item.category?.let { category ->
                            CategoryChip(category = category)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dates and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dates
                    Column {
                        Text(
                            text = "Purchased: ${formatDate(item.purchaseDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.warrantyExpiryDate != null) {
                            val label = when (item.type) {
                                ReceiptType.BILL -> "Renews: ${formatDate(item.warrantyExpiryDate)}"
                                ReceiptType.WARRANTY -> "Expires: ${formatDate(item.warrantyExpiryDate)}"
                                else -> null
                            }
                            label?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Status
                    StatusChip(
                        status = item.warrantyStatus(),
                        labelOverride = statusOverride,
                        colorOverride = statusColorOverride
                    )
                }

                // Reminder indicator — shown for both WARRANTIES and BILLS
                if (item.type != ReceiptType.RECEIPT && item.reminderDays != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.reminderDays.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long?): String = CurrencyUtils.formatDate(timestamp)
