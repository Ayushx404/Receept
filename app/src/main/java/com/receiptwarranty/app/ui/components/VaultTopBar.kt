package com.receiptwarranty.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape

/**
 * Standard app bar for top-level destinations.
 * [onSearchClick] shows a search icon; pass null to hide it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    title: String,
    modifier: Modifier = Modifier,
    titleIconRes: Int? = null,
    onSearchClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onSelectClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        modifier = modifier,
        title = {
            if (titleIconRes != null) {
                Image(
                    painter = painterResource(id = titleIconRes),
                    contentDescription = title,
                    modifier = Modifier.height(32.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            } else {
                Text(
                    text = title, 
                    fontWeight = FontWeight.ExtraBold 
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = WindowInsets(0.dp),
        scrollBehavior = scrollBehavior,
        actions = {
            if (onSelectClick != null) {
                IconButton(onClick = onSelectClick) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle, 
                        contentDescription = "Select items",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onSearchClick != null) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, 
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

/**
 * Top bar for detail screens — shows back navigation + optional edit/delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
    isArchived: Boolean = false,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        modifier = modifier,
        title = { 
            Text(
                text = title, 
                fontWeight = FontWeight.ExtraBold 
            ) 
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = WindowInsets(0.dp),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit, 
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onArchive != null) {
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = if (isArchived) "Restore" else "Archive",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onShare != null) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

/**
 * Top bar shown during bulk-selection mode.
 * Shows count of selected items, delete, and a close (exit) button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onShare: (() -> Unit)? = null,
    onExitSelection: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        windowInsets = WindowInsets(0.dp),
        actions = {
            if (onShare != null) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            IconButton(onClick = onDelete, enabled = selectedCount > 0) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected",
                    tint = if (selectedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
                )
            }
            IconButton(onClick = onExitSelection) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Cancel selection",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}

/**
 * Top bar for push-down child screens (Add/Edit, nested routes).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable () -> Unit = {}
) {
    LargeTopAppBar(
        modifier = modifier,
        title = { 
            Text(
                text = title, 
                fontWeight = FontWeight.ExtraBold 
            ) 
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = WindowInsets(0.dp),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = { actions() }
    )
}
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        placeholder = { Text("Search receipts, warranties, bills…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        shape = VaultShape.large,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
