package com.receiptwarranty.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import android.app.Activity
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate

import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.receiptwarranty.app.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptwarranty.app.ReceiptWarrantyApp
import com.receiptwarranty.app.data.ReminderDays
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.CategoryDefaults
import com.receiptwarranty.app.ui.components.CategorySelector
import com.receiptwarranty.app.ui.components.Material3DatePickerDialog
import com.receiptwarranty.app.ui.components.ReminderSelector
import com.receiptwarranty.app.ui.theme.CategoryIcons
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape
import com.receiptwarranty.app.util.ImageFileManager
import com.receiptwarranty.app.viewmodel.AddEditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    item: ReceiptWarranty?,
    type: ReceiptType?,
    autoLaunchScanner: Boolean = false,
    autoLaunchGallery: Boolean = false,
    tags: List<String>,
    categories: List<String>,
    onSave: (ReceiptWarranty) -> Unit,
    onCancel: () -> Unit,
    viewModel: AddEditViewModel? = null
) {
    val context = LocalContext.current
    val imageFileManager = remember { ImageFileManager(context) }

    var currentType by remember { mutableStateOf(type ?: ReceiptType.RECEIPT) }
    var title by remember { mutableStateOf(item?.title ?: "") }
    var category by remember { mutableStateOf(item?.category) }

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appearanceSettings by settingsViewModel.appearanceSettings.collectAsStateWithLifecycle()
    val iconStyle = appearanceSettings.iconStyle
    var imageUri by remember { mutableStateOf(item?.imageUri) }
    // Default to today to remove friction
    var purchaseDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var warrantyExpiryDate by remember { mutableLongStateOf(System.currentTimeMillis() + 31536000000L) }
    var reminderDays by remember { mutableStateOf<ReminderDays?>(item?.reminderDays ?: ReminderDays.ONE_WEEK) }
    var customReminderDays by remember { mutableStateOf(item?.customReminderDays) }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    
    var priceInput by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var tagsInput by remember { mutableStateOf(item?.tags ?: "") }
    var billingCycle by remember { mutableStateOf(item?.billingCycle ?: "Monthly") }

    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showWarrantyDatePicker by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val maxReminderDays = remember(purchaseDate, warrantyExpiryDate) {
        val purchase = purchaseDate
        val expiry = warrantyExpiryDate
        if (purchase != null && expiry != null) {
            val diffMs = expiry - purchase
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            diffDays.coerceIn(1, 15)
        } else {
            15
        }
    }

    LaunchedEffect(maxReminderDays) {
        customReminderDays?.let { days ->
            if (days > maxReminderDays) {
                customReminderDays = maxReminderDays
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageFileManager.saveImageFromUri(it)?.let { savedUri ->
                imageUri = savedUri
            }
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                imageFileManager.saveImageFromUri(uri)?.let { savedUri ->
                    imageUri = savedUri
                }
            }
        }
    }

    val launchScanner = {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()
        val scanner = GmsDocumentScanning.getClient(options)
        try {
            val activity = context as Activity
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(
                        context,
                        "Failed to launch scanner: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            android.util.Log.e("AddEditScreen", "Scanner error", e)
        }
    }

    // Auto launch intents
    var hasLaunchedIntent by remember { mutableStateOf(false) }
    LaunchedEffect(autoLaunchScanner, autoLaunchGallery) {
        if (!hasLaunchedIntent && item == null) {
            if (autoLaunchGallery) {
                galleryLauncher.launch("image/*")
                hasLaunchedIntent = true
            } else if (autoLaunchScanner) {
                launchScanner()
                hasLaunchedIntent = true
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val isValid = when (currentType) {
        ReceiptType.RECEIPT -> title.isNotBlank() && purchaseDate != null
        ReceiptType.WARRANTY -> title.isNotBlank() && purchaseDate != null && category != null && warrantyExpiryDate != null
        ReceiptType.BILL -> title.isNotBlank() && purchaseDate != null && warrantyExpiryDate != null
        ReceiptType.SUBSCRIPTION -> title.isNotBlank() && purchaseDate != null && warrantyExpiryDate != null
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = when {
                            item != null -> "Edit ${currentType.name.lowercase().replaceFirstChar { it.uppercase() }}"
                            currentType == ReceiptType.RECEIPT -> "Add Receipt"
                            currentType == ReceiptType.BILL -> "Add Bill"
                            currentType == ReceiptType.SUBSCRIPTION -> "Add Subscription"
                            else -> "Add Warranty"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isValid) {
                                onSave(
                                    ReceiptWarranty(
                                        id = item?.id ?: 0L,
                                        cloudId = item?.cloudId,
                                        driveFileId = if (item?.imageUri == imageUri) item?.driveFileId else null,
                                        type = currentType,
                                        title = title.trim(),
                                        category = category,
                                        imageUri = imageUri,
                                        purchaseDate = purchaseDate,
                                        warrantyExpiryDate = if (currentType != ReceiptType.RECEIPT) warrantyExpiryDate else null,
                                        reminderDays = if (currentType != ReceiptType.RECEIPT) reminderDays else null,
                                        customReminderDays = if (currentType != ReceiptType.RECEIPT && reminderDays == ReminderDays.CUSTOM) customReminderDays else null,
                                        notes = notes.ifBlank { null },
                                        price = priceInput.toDoubleOrNull(),
                                        tags = tagsInput.ifBlank { null },
                                        billingCycle = if (currentType == ReceiptType.SUBSCRIPTION) billingCycle else null,
                                        paymentHistory = item?.paymentHistory,
                                        createdAt = item?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = isValid
                    ) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = "Save",
                            tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg) // Increased spacing for cards
        ) {
            
            // 0. Type Toggle (Receipt / Warranty OR Bill / Subscription)
            if (item == null) {
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)
                    ) {
                        if (currentType == ReceiptType.RECEIPT || currentType == ReceiptType.WARRANTY) {
                            SegmentedButton(
                                selected = currentType == ReceiptType.RECEIPT,
                                onClick = { 
                                    currentType = ReceiptType.RECEIPT 
                                    category = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("Receipt")
                            }
                            SegmentedButton(
                                selected = currentType == ReceiptType.WARRANTY,
                                onClick = { 
                                    currentType = ReceiptType.WARRANTY
                                    category = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("Warranty")
                            }
                        } else {
                            SegmentedButton(
                                selected = currentType == ReceiptType.BILL,
                                onClick = { 
                                    currentType = ReceiptType.BILL 
                                    category = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("Bill")
                            }
                            SegmentedButton(
                                selected = currentType == ReceiptType.SUBSCRIPTION,
                                onClick = { 
                                    currentType = ReceiptType.SUBSCRIPTION
                                    category = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("Subscription")
                            }
                        }
                    }
                }
            }


            // 1. Photo Section
            item {
                Text(
                    text = "Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    if (imageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { imageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                        .padding(Spacing.sm)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                        val categoryColor = CategoryDefaults.getCategoryColor(category)

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category?.let { CategoryIcons.getIcon(it, iconStyle) } ?: Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (category != null) categoryColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                Button(onClick = { launchScanner() }) {
                                    Text("Scan Document")
                                }
                                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                                    Text("Gallery")
                                }
                            }
                        }
                    }
                }
            }
            // 2. Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = VaultShape.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            isError = title.isBlank() && title.isNotEmpty()
                        )

                        CategorySelector(
                            selectedCategory = category,
                            availableCategories = when (currentType) {
                                ReceiptType.RECEIPT -> CategoryDefaults.RECEIPT_CATEGORIES
                                ReceiptType.WARRANTY -> CategoryDefaults.WARRANTY_CATEGORIES
                                ReceiptType.BILL, ReceiptType.SUBSCRIPTION -> CategoryDefaults.BILL_SUBSCRIPTION_CATEGORIES
                            },
                            onCategorySelected = { category = it },
                            onAddCustomCategory = { },
                            isRequired = currentType != ReceiptType.BILL && currentType != ReceiptType.SUBSCRIPTION
                        )
                    }
                }
            }

            // 3. Dates & Financials Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = "Dates & Financials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            TextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                label = { Text("Price") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Text(text = appearanceSettings.currencySymbol, modifier = Modifier.padding(start = Spacing.sm)) },
                                shape = VaultShape.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                )
                            )
                            TextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("Tags") },
                                placeholder = { Text("tax, gift") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = VaultShape.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        DatePickerButton(
                            label = "Purchase Date *",
                            date = purchaseDate,
                            dateFormatter = dateFormatter,
                            onClick = { showPurchaseDatePicker = true },
                            isRequired = true
                        )

                        if (currentType != ReceiptType.RECEIPT) {
                            DatePickerButton(
                                label = if (currentType == ReceiptType.BILL) "Due Date *" else if (currentType == ReceiptType.SUBSCRIPTION) "Next Billing Date *" else "Warranty Expiry Date *",
                                date = warrantyExpiryDate,
                                dateFormatter = dateFormatter,
                                onClick = { showWarrantyDatePicker = true },
                                isRequired = true
                            )

                            ReminderSelector(
                                selectedReminder = reminderDays,
                                customDays = customReminderDays,
                                onReminderSelected = { sel, custom -> 
                                    reminderDays = sel
                                    customReminderDays = custom
                                },
                                maxDays = maxReminderDays,
                                enabled = warrantyExpiryDate != null
                            )

                            if (currentType == ReceiptType.SUBSCRIPTION) {
                                Spacer(Modifier.height(Spacing.md))
                                Text(
                                    text = "Billing Cycle",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                val billingCycles = listOf("Monthly", "Quarterly", "Yearly", "Every 6 months", "Weekly", "Custom")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    billingCycles.forEach { cycle ->
                                        val isSelected = billingCycle == cycle
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { billingCycle = cycle },
                                            label = { Text(cycle) }
                                        )
                                    }
                                }
                                if (billingCycle == "Custom") {
                                    Spacer(Modifier.height(Spacing.sm))
                                    TextField(
                                        value = billingCycle,
                                        onValueChange = { billingCycle = it },
                                        label = { Text("Custom billing cycle") },
                                        placeholder = { Text("e.g., Every 45 days") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = VaultShape.medium,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Notes Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = VaultShape.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }

    if (showPurchaseDatePicker) {
        Material3DatePickerDialog(
            initialDate = purchaseDate ?: System.currentTimeMillis(),
            onDateSelected = {
                purchaseDate = it
                showPurchaseDatePicker = false
            },
            onDismiss = { showPurchaseDatePicker = false }
        )
    }

    if (showWarrantyDatePicker) {
        Material3DatePickerDialog(
            initialDate = warrantyExpiryDate ?: System.currentTimeMillis(),
            onDateSelected = {
                warrantyExpiryDate = it
                showWarrantyDatePicker = false
            },
            onDismiss = { showWarrantyDatePicker = false }
        )
    }
}

@Composable
private fun DatePickerButton(
    label: String,
    date: Long?,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    isRequired: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = VaultShape.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (date != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday, 
            contentDescription = null, 
            modifier = Modifier.size(20.dp),
            tint = if (date != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = if (date != null) dateFormatter.format(Date(date)) else label,
            style = MaterialTheme.typography.bodyLarge
        )
        if (isRequired && date == null) {
            Text(
                text = " *",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
