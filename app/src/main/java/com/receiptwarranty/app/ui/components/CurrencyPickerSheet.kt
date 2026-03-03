package com.receiptwarranty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.VaultShape

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String
)

val availableCurrencies = listOf(
    CurrencyInfo("USD", "US Dollar", "$"),
    CurrencyInfo("EUR", "Euro", "€"),
    CurrencyInfo("GBP", "British Pound", "£"),
    CurrencyInfo("INR", "Indian Rupee", "₹"),
    CurrencyInfo("JPY", "Japanese Yen", "¥"),
    CurrencyInfo("CAD", "Canadian Dollar", "C$"),
    CurrencyInfo("AUD", "Australian Dollar", "A$"),
    CurrencyInfo("CNY", "Chinese Yuan", "¥"),
    CurrencyInfo("AED", "UAE Dirham", "د.إ"),
    CurrencyInfo("KRW", "South Korean Won", "₩"),
    CurrencyInfo("SGD", "Singapore Dollar", "S$"),
    CurrencyInfo("NZD", "New Zealand Dollar", "NZ$"),
    CurrencyInfo("CHF", "Swiss Franc", "CHF"),
    CurrencyInfo("SEK", "Swedish Krona", "kr"),
    CurrencyInfo("NOK", "Norwegian Krone", "kr"),
    CurrencyInfo("DKK", "Danish Krone", "kr"),
    CurrencyInfo("BRL", "Brazilian Real", "R$"),
    CurrencyInfo("ZAR", "South African Rand", "R"),
    CurrencyInfo("MXN", "Mexican Peso", "$"),
    CurrencyInfo("HKD", "Hong Kong Dollar", "HK$"),
    CurrencyInfo("TRY", "Turkish Lira", "₺"),
    CurrencyInfo("RUB", "Russian Ruble", "₽"),
    CurrencyInfo("IDR", "Indonesian Rupiah", "Rp"),
    CurrencyInfo("MYR", "Malaysian Ringgit", "RM"),
    CurrencyInfo("PHP", "Philippine Peso", "₱"),
    CurrencyInfo("THB", "Thai Baht", "฿"),
    CurrencyInfo("VND", "Vietnamese Dong", "₫")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultCurrencyPickerSheet(
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    currentSelection: String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            availableCurrencies
        } else {
            availableCurrencies.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(top = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                placeholder = { Text("Search currency...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                shape = VaultShape.medium,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )

            // List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredCurrencies, key = { it.code }) { currency ->
                    CurrencyRow(
                        currency = currency,
                        isSelected = currentSelection == currency.symbol,
                        onClick = {
                            onCurrencySelected(currency.symbol)
                            onDismiss()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    currency: CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol Circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currency.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Code
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Selected Icon
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
