package com.receiptwarranty.app.ui.screens

import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.receiptwarranty.app.data.IconPackStyle
import com.receiptwarranty.app.data.AppearanceSettings
import com.receiptwarranty.app.data.sync.SyncStatus
import com.receiptwarranty.app.ui.theme.ThemeMode
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.ui.theme.availableColors
import com.receiptwarranty.app.viewmodel.SettingsViewModel
import com.receiptwarranty.app.ui.theme.Spacing
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.ColumnScope
import com.receiptwarranty.app.ui.components.PaletteSwatch
import com.receiptwarranty.app.ui.components.QuickStatCard
import com.receiptwarranty.app.ui.components.ThemePreviewCard
import com.receiptwarranty.app.ui.components.VaultCurrencyPickerSheet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.DataObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userEmail: String = "",
    userName: String? = null,
    userProfilePicture: String? = null,
    appVersion: String = "1.0.0",
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onExportCSV: () -> Unit,
    onExportJSON: () -> Unit,
    onImport: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAboutMe: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToAdvanced: () -> Unit,
    onBack: () -> Unit
) {
    var versionClicks by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showSignOutDialog by remember { mutableStateOf(false) }
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val appearanceSettings by viewModel.appearanceSettings.collectAsStateWithLifecycle()
    var showCurrencyPicker by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showCurrencyPicker) {
        VaultCurrencyPickerSheet(
            onDismiss = { showCurrencyPicker = false },
            onCurrencySelected = { viewModel.setCurrencySymbol(it) },
            currentSelection = appearanceSettings.currencySymbol
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // 1. Account Section
            item {
                SettingsGroup(title = "Account") {
                    if (userEmail.isNotEmpty()) {
                        AccountItem(
                            email = userEmail,
                            name = userName,
                            profilePicture = userProfilePicture
                        )
                        SettingsRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Sign Out",
                            subtitle = "Linked to Google Drive",
                            onClick = { showSignOutDialog = true },
                            showChevron = false,
                            titleColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        SettingsRow(
                            icon = Icons.Default.AccountCircle,
                            title = "Not Signed In",
                            subtitle = "Tap to sign in with Google",
                            onClick = onSignOut,
                            showChevron = true,
                            titleColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Appearance Section
            item {
                SettingsGroup(title = "Appearance") {
                    AppearanceItem(
                        settings = appearanceSettings,
                        onModeChange = { viewModel.setThemeMode(it) },
                        onDynamicChange = { viewModel.setUseDynamicColor(it) },
                        onColorChange = { viewModel.setPrimaryColor(it) },
                        onAmoledChange = { viewModel.setUseAmoledBlack(it) },
                        onIconStyleChange = { viewModel.setIconStyle(it) },
                        onCurrencyClick = { showCurrencyPicker = true }
                    )
                }
            }

            // 3. Data & Backup
            item {
                SettingsGroup(title = "Data & Backup") {
                    if (userEmail.isNotEmpty()) {
                        SyncItem(
                            status = syncStatus,
                            lastSync = lastSyncTime,
                            onSync = { viewModel.syncNow() }
                        )
                    } else {
                        SettingsRow(
                            icon = Icons.Default.CloudSync,
                            title = "Cloud Backup Offline",
                            subtitle = "Sign in to backup to Google Drive",
                            onClick = onSignOut,
                            showChevron = false
                        )
                    }
                    SettingsRow(
                        icon = Icons.Default.DataObject,
                        title = "Export Data (JSON)",
                        subtitle = "Full backup with images",
                        onClick = onExportJSON
                    )
                    SettingsRow(
                        icon = Icons.Default.FileDownload,
                        title = "Export to CSV",
                        subtitle = "Spreadsheet format (no images)",
                        onClick = onExportCSV
                    )
                    SettingsRow(
                        icon = Icons.Default.Upload,
                        title = "Import Data",
                        subtitle = "Restore from JSON backup",
                        onClick = onImport
                    )
                    SettingsRow(
                        icon = Icons.Default.Archive,
                        title = "Vault Archive",
                        subtitle = "View and restore archived items",
                        onClick = onNavigateToArchive
                    )
                }
            }

            // 4. About
            item {
                SettingsGroup(title = "About") {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "$appVersion",
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < 500) {
                                versionClicks++
                            } else {
                                versionClicks = 1
                            }
                            lastClickTime = currentTime
                            
                            if (versionClicks == 7 && !appearanceSettings.isDeveloperModeEnabled) {
                                viewModel.setDeveloperModeEnabled(true)
                                android.widget.Toast.makeText(context, "Developer mode enabled", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (versionClicks > 0 && versionClicks < 7 && !appearanceSettings.isDeveloperModeEnabled) {
                                val remaining = 7 - versionClicks
                                if (remaining <= 3) {
                                    android.widget.Toast.makeText(context, "You are now $remaining steps away from being a developer", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        showChevron = false
                    )
                    if (appearanceSettings.isDeveloperModeEnabled) {
                        SettingsRow(
                            icon = Icons.Filled.BugReport,
                            title = "Advanced Settings",
                            subtitle = "Developer tools & destructive actions",
                            onClick = onNavigateToAdvanced,
                            showChevron = true
                        )
                    }
                    SettingsRow(
                        icon = Icons.Filled.Shield,
                        title = "Privacy & Security",
                        subtitle = "How we handle your data",
                        onClick = onNavigateToPrivacy
                    )
                    SettingsRow(
                        icon = Icons.Default.AccountCircle,
                        title = "About Me",
                        subtitle = "Developer & Project Info",
                        onClick = onNavigateToAboutMe
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
                Text(
                    text = "Receipt Warranty Tracker",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.xs)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VaultShape.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            } else if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AccountItem(
    email: String,
    name: String? = null,
    profilePicture: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profilePicture != null) {
            coil.compose.AsyncImage(
                model = profilePicture,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (name ?: email).take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column {
            Text(
                text = name ?: email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (name != null) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AppearanceItem(
    settings: AppearanceSettings,
    onModeChange: (ThemeMode) -> Unit,
    onDynamicChange: (Boolean) -> Unit,
    onColorChange: (String) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onIconStyleChange: (IconPackStyle) -> Unit,
    onCurrencyClick: () -> Unit
) {
    val isDarkMode = settings.themeMode == ThemeMode.DARK ||
        (settings.themeMode == ThemeMode.SYSTEM && androidx.compose.foundation.isSystemInDarkTheme())

    Column(modifier = Modifier.padding(Spacing.lg)) {


        Text("Theme Mode", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.sm))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val modes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
            val labels = listOf("Light", "Dark", "Auto")
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = settings.themeMode == mode,
                    onClick = { onModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                    label = { Text(labels[index]) }
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Text("Accent Color & Theme", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(Spacing.md))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Dynamic theme card
            item {
                ThemePreviewCard(
                    primaryColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    surfaceColor = MaterialTheme.colorScheme.surface,
                    onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                    label = "Dynamic",
                    isSelected = settings.useDynamicColor,
                    onClick = { onDynamicChange(true) }
                )
            }
            
            // Available color cards — One UI 8 Palette Sync
            val colorNames = mapOf(
                "#0381FE" to "Blue",
                "#FF6B35" to "Coral",
                "#2ECD71" to "Green",
                "#FF3B30" to "Red",
                "#AF52DE" to "Purple",
                "#5856D6" to "Indigo",
                "#FF2D55" to "Pink",
                "#00C7BE" to "Teal",
                "#64D2FF" to "Sky Blue",
                "#FFD60A" to "Yellow"
            )

            items(availableColors, key = { it }) { hex ->
                val cardPrimary = try {
                    Color(AndroidColor.parseColor(hex))
                } catch (e: Exception) { Color(0xFF0A84FF) }
                
                ThemePreviewCard(
                    primaryColor = cardPrimary,
                    backgroundColor = if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF5F7FA),
                    surfaceColor = if (isDarkMode) Color(0xFF222426) else Color.White,
                    onSurfaceColor = if (isDarkMode) Color(0xFFE2E2E6) else Color(0xFF1A1C1E),
                    label = colorNames[hex] ?: hex,
                    isSelected = !settings.useDynamicColor && settings.primaryColorHex.lowercase() == hex.lowercase(),
                    onClick = { 
                        onDynamicChange(false)
                        onColorChange(hex) 
                    }
                )
            }
        }
        
        // AMOLED toggle — only visible in dark mode
        if (isDarkMode) {
            Spacer(Modifier.height(Spacing.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pure Black (AMOLED)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("True black background for OLED screens", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.useAmoledBlack, onCheckedChange = onAmoledChange)
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Text("Icon Style", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.sm))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val styles = IconPackStyle.entries
            val labels = listOf("Lucide", "Phosphor", "TwoTone")
            styles.forEachIndexed { index, style ->
                SegmentedButton(
                    selected = settings.iconStyle == style,
                    onClick = { onIconStyleChange(style) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = styles.size),
                    label = { Text(labels[index], maxLines = 1) }
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Text("Currency", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.sm))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCurrencyClick() },
            shape = VaultShape.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, VaultShape.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            settings.currencySymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(Spacing.md))
                    Column {
                        Text(
                            "Preferred Currency",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Used for all financial tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SyncItem(
    status: SyncStatus,
    lastSync: Long,
    onSync: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = VaultShape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(Spacing.md))
                    Column {
                        Text(
                            "Cloud Archive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lastSync > 0) "Synced ${formatLastSyncTime(lastSync)}" else "Backup your items safely",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (status is SyncStatus.Syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = onSync,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (status is SyncStatus.Error) {
                Spacer(Modifier.height(Spacing.md))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = VaultShape.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign Out", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Sign Out") },
        text = { Text("Are you sure you want to sign out? Your local data will be preserved but sync will stop.") }
    )
}

private fun formatLastSyncTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
