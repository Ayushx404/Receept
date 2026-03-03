package com.receiptwarranty.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.util.share.ShareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePreviewBottomSheet(
    shareResult: Result<Uri>?,
    onGenerate: (ShareTheme) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(ShareTheme.CLEAN_WHITE) }
    
    // Trigger generation on first launch and when theme changes
    LaunchedEffect(selectedTheme) {
        onGenerate(selectedTheme)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xxl)
                .verticalScroll(rememberScrollState()) // Allow scrolling if screen is small
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Share Card Preview", 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            HorizontalDivider()

            // Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    shareResult == null -> {
                        CircularProgressIndicator()
                    }
                    shareResult!!.isFailure -> {
                        Text(
                            "Failed to generate card.", 
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    shareResult!!.isSuccess -> {
                        AsyncImage(
                            model = shareResult!!.getOrNull(),
                            contentDescription = "Share Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.lg)
                                .clip(VaultShape.large),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Theme Selection
            Text(
                "Select Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
            Spacer(Modifier.height(Spacing.md))
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(ShareTheme.entries.toTypedArray()) { theme ->
                    ThemeSelectorItem(
                        theme = theme,
                        isSelected = selectedTheme == theme,
                        onClick = { selectedTheme = theme }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xxl))

            // Share Button
            Button(
                onClick = {
                    shareResult?.getOrNull()?.let { uri ->
                        shareImageUri(context, uri)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .height(56.dp),
                shape = VaultShape.pill,
                enabled = shareResult?.isSuccess == true
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Share Image", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(Spacing.xxl)) // System bar padding
        }
    }
}

@Composable
private fun ThemeSelectorItem(
    theme: ShareTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeName = theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    val (bgColor, rimColor) = when(theme) {
        ShareTheme.CLEAN_WHITE -> Color(0xFFF8F9FA) to Color(0xFFE5E5EA)
        ShareTheme.DARK_ELEGANT -> Color(0xFF1C1C1E) to Color(0xFF38383A)
        ShareTheme.BRANDED_GRADIENT -> Color(0xFF1A2A6C) to Color(0xFF112255) // Approx rep
        ShareTheme.MINIMAL -> Color.White to Color.Black
        ShareTheme.ONEUI_LAVENDER -> Color(0xFFDCD4F5) to Color(0xFFE0D9ED)
        ShareTheme.ONEUI_MINT -> Color(0xFFC1F0E4) to Color(0xFFD4EBE4)
        ShareTheme.ONEUI_OCEAN -> Color(0xFFC1E3FC) to Color(0xFFD4E6F5)
        ShareTheme.ONEUI_CORAL -> Color(0xFFFFD9DB) to Color(0xFFF5D2D5)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else rimColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check, 
                    contentDescription = "Selected",
                    tint = if (theme == ShareTheme.CLEAN_WHITE || theme == ShareTheme.MINIMAL) Color.Black else Color.White
                )
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            themeName, 
            style = MaterialTheme.typography.bodySmall, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareImageUri(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Item"))
}
