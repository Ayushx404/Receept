package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.WarrantyStatus
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors
import com.receiptwarranty.app.ui.theme.VaultShape

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val categoryColor = CategoryDefaults.getCategoryColor(category)
    Surface(
        modifier = modifier,
        shape = VaultShape.small,
        color = categoryColor.copy(alpha = 0.12f)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            color = categoryColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusChip(
    status: WarrantyStatus,
    modifier: Modifier = Modifier,
    itemType: ReceiptType? = null,
    labelOverride: String? = null,
    colorOverride: Color? = null,
    bgColorOverride: Color? = null
) {
    val (defaultText, defaultColor, defaultBgColor) = when (status) {
        WarrantyStatus.VALID -> {
            val label = when(itemType) {
                ReceiptType.BILL, ReceiptType.SUBSCRIPTION -> "Active"
                else -> "Valid"
            }
            Triple(label, VaultColors.statusValid, VaultColors.statusValidBg)
        }
        WarrantyStatus.EXPIRING_SOON -> {
            val label = when(itemType) {
                ReceiptType.BILL, ReceiptType.SUBSCRIPTION -> "Renewing Soon"
                else -> "Expiring Soon"
            }
            Triple(label, VaultColors.statusExpiringSoon, VaultColors.statusExpiringSoonBg)
        }
        WarrantyStatus.EXPIRED -> Triple("Expired", VaultColors.statusExpired, VaultColors.statusExpiredBg)
        WarrantyStatus.NO_WARRANTY -> {
            val label = when(itemType) {
                ReceiptType.BILL, ReceiptType.SUBSCRIPTION -> "Active"
                else -> "Receipt"
            }
            Triple(label, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        }
    }

    val text = labelOverride ?: defaultText
    val color = colorOverride ?: defaultColor
    val bgColor = bgColorOverride ?: (colorOverride?.copy(alpha = 0.12f) ?: defaultBgColor)

    Surface(
        modifier = modifier,
        shape = VaultShape.small,
        color = bgColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
