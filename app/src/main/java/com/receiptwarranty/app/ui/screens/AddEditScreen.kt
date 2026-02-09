package com.receiptwarranty.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.receiptwarranty.app.data.ReminderDays
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.ui.components.CategorySelector
import com.receiptwarranty.app.ui.components.Material3DatePickerDialog
import com.receiptwarranty.app.ui.components.ReminderSelector
import com.receiptwarranty.app.util.ImageFileManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    item: ReceiptWarranty?,
    type: ReceiptType,
    companies: List<String>,
    categories: List<String>,
    onSave: (ReceiptWarranty) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val imageFileManager = remember { ImageFileManager(context) }

    var title by remember { mutableStateOf(item?.title ?: "") }
    var company by remember { mutableStateOf(item?.company ?: "") }
    var category by remember { mutableStateOf(item?.category) }
    var imageUri by remember { mutableStateOf(item?.imageUri) }
    var purchaseDate by remember { mutableStateOf(item?.purchaseDate) }
    var warrantyExpiryDate by remember { mutableStateOf(item?.warrantyExpiryDate) }
    var reminderDays by remember { mutableStateOf(item?.reminderDays) }
    var notes by remember { mutableStateOf(item?.notes ?: "") }

    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showWarrantyDatePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageFileManager.saveImageFromUri(it)?.let { savedUri ->
                imageUri = savedUri
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val isValid = when (type) {
        ReceiptType.RECEIPT -> {
            title.isNotBlank() && company.isNotBlank() && purchaseDate != null
        }
        ReceiptType.WARRANTY -> {
            title.isNotBlank() && company.isNotBlank() &&
            purchaseDate != null && category != null &&
            warrantyExpiryDate != null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            item != null -> "Edit ${type.name.lowercase().replaceFirstChar { it.uppercase() }}"
                            type == ReceiptType.RECEIPT -> "Add Receipt"
                            else -> "Add Warranty"
                        }
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
                                        type = type,
                                        title = title.trim(),
                                        company = company.trim(),
                                        category = category,
                                        imageUri = imageUri,
                                        purchaseDate = purchaseDate,
                                        warrantyExpiryDate = if (type == ReceiptType.WARRANTY) warrantyExpiryDate else null,
                                        reminderDays = if (type == ReceiptType.WARRANTY) reminderDays else null,
                                        notes = notes.ifBlank { null },
                                        createdAt = item?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = isValid
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = title.isBlank() && title.isNotEmpty()
                )
            }

            item {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company/Store *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = company.isBlank() && company.isNotEmpty()
                )
            }

            if (type == ReceiptType.WARRANTY) {
                item {
                    CategorySelector(
                        selectedCategory = category,
                        availableCategories = categories,
                        onCategorySelected = { category = it },
                        onAddCustomCategory = { },
                        isRequired = true
                    )
                }
            }

            item {
                Text(
                    text = "Photo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onClick = { galleryLauncher.launch("image/*") }
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
                                    .padding(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove photo"
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap to add photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                DatePickerButton(
                    label = "Purchase Date *",
                    date = purchaseDate,
                    dateFormatter = dateFormatter,
                    onClick = { showPurchaseDatePicker = true },
                    isRequired = true
                )
            }

            if (type == ReceiptType.WARRANTY) {
                item {
                    DatePickerButton(
                        label = "Warranty Expiry Date *",
                        date = warrantyExpiryDate,
                        dateFormatter = dateFormatter,
                        onClick = { showWarrantyDatePicker = true },
                        isRequired = true
                    )
                }

                item {
                    ReminderSelector(
                        selectedReminder = reminderDays,
                        onReminderSelected = { reminderDays = it },
                        enabled = warrantyExpiryDate != null
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            item {
                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (date != null) dateFormatter.format(Date(date)) else label,
            style = MaterialTheme.typography.labelMedium
        )
        if (isRequired && date == null) {
            Text(
                text = " *",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
