package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.ReminderDays
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSelector(
    selectedReminder: ReminderDays?,
    customDays: Int?,
    onReminderSelected: (ReminderDays?, Int?) -> Unit,
    maxDays: Int = 15,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.Switch(
                checked = selectedReminder != null,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        onReminderSelected(ReminderDays.CUSTOM, customDays ?: 1)
                    } else {
                        onReminderSelected(null, null)
                    }
                },
                enabled = enabled
            )
        }

        if (selectedReminder != null && enabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notify Me",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${customDays ?: 1} days before",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                androidx.compose.material3.Slider(
                    value = (customDays ?: 1).coerceIn(1, maxDays).toFloat(),
                    onValueChange = { onReminderSelected(ReminderDays.CUSTOM, it.toInt()) },
                    valueRange = 1f..maxDays.toFloat(),
                    steps = if (maxDays > 1) maxDays - 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Notifying ${customDays ?: 1} days before expiry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
