package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReceiptType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSelectionBottomSheet(
    onTypeSelected: (ReceiptType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add New Item",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TypeSelectionCard(
                type = ReceiptType.RECEIPT,
                icon = Icons.Default.Receipt,
                title = "Receipt",
                description = "Store a purchase receipt for your records",
                onClick = { onTypeSelected(ReceiptType.RECEIPT) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TypeSelectionCard(
                type = ReceiptType.WARRANTY,
                icon = Icons.Default.Verified,
                title = "Warranty",
                description = "Track a warranty with expiry reminders",
                onClick = { onTypeSelected(ReceiptType.WARRANTY) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelectionCard(
    type: ReceiptType,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                ReceiptType.RECEIPT -> MaterialTheme.colorScheme.primaryContainer
                ReceiptType.WARRANTY -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (type) {
                    ReceiptType.RECEIPT -> MaterialTheme.colorScheme.primary
                    ReceiptType.WARRANTY -> MaterialTheme.colorScheme.secondary
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
