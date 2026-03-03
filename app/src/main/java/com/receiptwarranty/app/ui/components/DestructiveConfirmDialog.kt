package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Destructive confirmation dialog that explicitly tells the user about cloud + Drive effects.
 * Use everywhere a delete action is initiated.
 */
@Composable
fun DestructiveConfirmDialog(
    title: String = "Delete item?",
    /** When true, the body copy mentions cloud + Drive deletion. */
    includesCloudWarning: Boolean = true,
    itemDescription: String = "This item",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyText = buildString {
        append(itemDescription)
        append(" will be permanently deleted and cannot be recovered.")
        if (includesCloudWarning) {
            append(
                "\n\nThis will also remove it from Firestore cloud storage " +
                "and any attached images from Google Drive."
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(title, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(bodyText, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    modifier = androidx.compose.ui.Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = null
    )
}
