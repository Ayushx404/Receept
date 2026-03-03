package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.Spacing

/** The type of empty state to render. */
enum class EmptyStateType { EMPTY, NO_SEARCH_RESULTS, ERROR }

/**
 * Reusable empty / error placeholder used by all list screens.
 * Provides an icon, title, subtitle, and optional action button.
 */
@Composable
fun EmptyStateView(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    customTitle: String? = null,
    customSubtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val (iconVec, title, subtitle) = when (type) {
        EmptyStateType.EMPTY -> Triple(
            Icons.Default.Receipt,
            "Nothing here yet",
            "Tap + to add your first receipt or warranty"
        )
        EmptyStateType.NO_SEARCH_RESULTS -> Triple(
            Icons.Default.SearchOff,
            "No results found",
            "Try a different search term or clear the filters"
        )
        EmptyStateType.ERROR -> Triple(
            Icons.Default.Error,
            "Something went wrong",
            "Pull to refresh or try again"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = iconVec,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = customTitle ?: title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = customSubtitle ?: subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(Spacing.md))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
