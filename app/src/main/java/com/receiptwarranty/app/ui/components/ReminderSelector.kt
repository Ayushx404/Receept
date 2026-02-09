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

@Composable
fun ReminderSelector(
    selectedReminder: ReminderDays?,
    onReminderSelected: (ReminderDays?) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Reminder",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReminderDays.entries.forEach { reminder ->
                FilterChip(
                    selected = selectedReminder == reminder,
                    onClick = {
                        if (enabled) {
                            onReminderSelected(
                                if (selectedReminder == reminder) null else reminder
                            )
                        }
                    },
                    label = { 
                        Text(
                            text = when (reminder) {
                                ReminderDays.ONE_DAY -> "1 day"
                                ReminderDays.THREE_DAYS -> "3 days"
                                ReminderDays.FIVE_DAYS -> "5 days"
                                ReminderDays.ONE_WEEK -> "1 week"
                            }
                        ) 
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedReminder != null && enabled) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "You'll be notified ${selectedReminder.displayName.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
