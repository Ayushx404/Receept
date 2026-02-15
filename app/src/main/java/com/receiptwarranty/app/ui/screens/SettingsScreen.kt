package com.receiptwarranty.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.data.sync.SyncStateManager
import com.receiptwarranty.app.data.sync.SyncStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userEmail: String = "",
    syncStateManager: SyncStateManager? = null,
    onSignOut: () -> Unit = {},
    onExportCSV: () -> Unit,
    onBack: () -> Unit
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    val syncStatus by syncStateManager?.syncStatus?.collectAsState() ?: remember { mutableStateOf(SyncStatus.Idle) }
    val lastSyncTime by syncStateManager?.lastSyncTime?.collectAsState() ?: remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (userEmail.isNotEmpty()) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sync Section
                if (syncStateManager != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cloud Sync",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (lastSyncTime > 0) {
                                            "Last synced: ${formatLastSyncTime(lastSyncTime)}"
                                        } else {
                                            "Never synced"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (syncStatus) {
                                is SyncStatus.Idle -> {
                                    OutlinedButton(
                                        onClick = { scope.launch { syncStateManager.syncAll() } },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync Now")
                                    }
                                }
                                is SyncStatus.Syncing -> {
                                    Button(
                                        onClick = { },
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Syncing...")
                                    }
                                }
                                is SyncStatus.Success -> {
                                    OutlinedButton(
                                        onClick = { scope.launch { syncStateManager.syncAll() } },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync Again")
                                    }
                                    LaunchedEffect(syncStatus) {
                                        kotlinx.coroutines.delay(2000)
                                        syncStateManager.resetStatus()
                                    }
                                }
                                is SyncStatus.Error -> {
                                    OutlinedButton(
                                        onClick = { scope.launch { syncStateManager.syncAll() } },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry Sync")
                                    }
                                    val errorMsg = (syncStatus as SyncStatus.Error).message
                                    if (errorMsg.isNotEmpty()) {
                                        Text(
                                            text = errorMsg,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Sign Out",
                    subtitle = "Sign out of your Google account",
                    onClick = { showSignOutDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Share,
                title = "Export Data",
                subtitle = "Export all receipts and warranties to CSV",
                onClick = onExportCSV
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "1.0.0",
                onClick = { }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Receipt Warranty Tracker",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun formatLastSyncTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
