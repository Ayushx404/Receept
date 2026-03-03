package com.receiptwarranty.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultColors

enum class BannerType { OFFLINE, SYNCING, SYNC_ERROR }

/**
 * Slim animated banner shown beneath the top bar for connectivity and sync state.
 * Uses icon + text (never color-only) for accessibility.
 */
@Composable
fun SyncStatusBanner(
    visible: Boolean,
    type: BannerType,
    modifier: Modifier = Modifier,
    message: String = "",
    pendingCount: Int = 0,
    onRetry: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        val (bgColor, contentColor, icon, defaultMsg) = when (type) {
            BannerType.OFFLINE -> BannerStyle(
                bg = VaultColors.statusOfflineBg,
                content = VaultColors.statusOffline,
                icon = Icons.Default.CloudOff,
                label = if (pendingCount > 0)
                    "You're offline · $pendingCount pending change${if (pendingCount > 1) "s" else ""}"
                else
                    "You're offline — changes will sync when connected"
            )
            BannerType.SYNCING -> BannerStyle(
                bg = VaultColors.statusSyncingBg,
                content = VaultColors.statusSyncing,
                icon = Icons.Default.Sync,
                label = "Syncing…"
            )
            BannerType.SYNC_ERROR -> BannerStyle(
                bg = VaultColors.statusSyncErrorBg,
                content = VaultColors.statusSyncError,
                icon = Icons.Default.Error,
                label = "Sync failed"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = message.ifBlank { defaultMsg },
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
            }
            if (onRetry != null && type == BannerType.SYNC_ERROR) {
                TextButton(onClick = onRetry) {
                    Text("Retry", style = MaterialTheme.typography.labelMedium, color = contentColor)
                }
            }
        }
    }
}

private data class BannerStyle(
    val bg: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
