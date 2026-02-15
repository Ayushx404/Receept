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
import androidx.compose.material3.OutlinedTextField
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
    val ALL = listOf(
        "Electronics", "Appliances", "Furniture", "Vehicles",
        "Clothing", "Sports Equipment", "Tools", "Books",
        "Gaming", "Health & Beauty", "Home & Garden", "Other"
    )
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
        OutlinedTextField(
            value = selectedCategory ?: if (isRequired) "Select Category *" else "Select Category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            isError = isRequired && selectedCategory == null,
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
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true,
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
