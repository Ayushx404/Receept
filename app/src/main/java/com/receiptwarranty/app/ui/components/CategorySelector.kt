package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import com.receiptwarranty.app.ui.theme.VaultShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object CategoryDefaults {
    val WARRANTY_CATEGORIES = listOf(
        "Electronics", "Appliances", "Furniture", "Vehicles",
        "Sports Equipment", "Tools", "Gaming", "Services", "Other"
    ).sorted()

    val RECEIPT_CATEGORIES = listOf(
        "Groceries", "Dining", "Travel", "Stationery",
        "Personal Care", "Health & Beauty", "Clothing",
        "Books", "Services", "Education",
        "Business", "Home Decor", "Other"
    ).sorted()

    val BILL_SUBSCRIPTION_CATEGORIES = listOf(
        "Electricity", "Water", "Internet", "Mobile",
        "Insurance", "Rent", "Streaming", "EMI",
        "Subscription", "Utilities", "Other"
    ).sorted()

    val ALL = (WARRANTY_CATEGORIES + RECEIPT_CATEGORIES).distinct().sorted()

    fun getCategoryColor(category: String?): androidx.compose.ui.graphics.Color {
        return when (category) {
            "Electronics" -> androidx.compose.ui.graphics.Color(0xFF673AB7)
            "Appliances" -> androidx.compose.ui.graphics.Color(0xFFE91E63)
            "Furniture" -> androidx.compose.ui.graphics.Color(0xFF795548)
            "Vehicles" -> androidx.compose.ui.graphics.Color(0xFF607D8B)
            "Clothing" -> androidx.compose.ui.graphics.Color(0xFFFF5722)
            "Sports Equipment" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            "Tools" -> androidx.compose.ui.graphics.Color(0xFFFFC107)
            "Books" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            "Gaming" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
            "Health & Beauty" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
            "Groceries" -> androidx.compose.ui.graphics.Color(0xFFCDDC39)
            "Dining" -> androidx.compose.ui.graphics.Color(0xFFFFEB3B)
            "Travel" -> androidx.compose.ui.graphics.Color(0xFF00BCD4)
            "Subscription" -> androidx.compose.ui.graphics.Color(0xFF3F51B5)
            "Services" -> androidx.compose.ui.graphics.Color(0xFF009688)
            "Personal Care" -> androidx.compose.ui.graphics.Color(0xFFF44336)
            "Education" -> androidx.compose.ui.graphics.Color(0xFF03A9F4)
            "Business" -> androidx.compose.ui.graphics.Color(0xFF424242)
            "Home Decor" -> androidx.compose.ui.graphics.Color(0xFFF06292)
            "Stationery" -> androidx.compose.ui.graphics.Color(0xFF90A4AE)
            "Electricity" -> androidx.compose.ui.graphics.Color(0xFFFFD700)
            "Water" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
            "Internet" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
            "Mobile" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            "Insurance" -> androidx.compose.ui.graphics.Color(0xFF607D8B)
            "Rent" -> androidx.compose.ui.graphics.Color(0xFF795548)
            "Streaming" -> androidx.compose.ui.graphics.Color(0xFFE91E63)
            "EMI" -> androidx.compose.ui.graphics.Color(0xFFFF5722)
            "Utilities" -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: String?,
    availableCategories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onAddCustomCategory: () -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val allCategories = remember(availableCategories) {
        (CategoryDefaults.ALL + availableCategories).distinct().sorted()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedCategory ?: if (isRequired) "Select Category *" else "Select Category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            isError = isRequired && selectedCategory == null,
            shape = VaultShape.medium,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            label = { Text("Category") }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("No Category") },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )

            HorizontalDivider()

            allCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Category")
                    }
                },
                onClick = {
                    expanded = false
                    showCustomDialog = true
                }
            )
        }
    }

    if (showCustomDialog) {
        CustomCategoryDialog(
            onConfirm = { customCategory ->
                onCategorySelected(customCategory)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

@Composable
private fun CustomCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Category") },
        text = {
            TextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true,
                shape = VaultShape.medium,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
