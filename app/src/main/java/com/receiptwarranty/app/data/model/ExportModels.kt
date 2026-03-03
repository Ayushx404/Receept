package com.receiptwarranty.app.data.model

import android.net.Uri
import androidx.core.net.toUri
import android.util.Base64
import com.receiptwarranty.app.ReceiptWarrantyApp
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReminderDays
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class VaultExportData(
    val version: Int = 1,
    val exportDate: String,
    val items: List<ExportItem>
)

data class ExportItem(
    val type: String,
    val title: String,
    val category: String?,
    val imageBase64: String?,
    val purchaseDate: String?,
    val warrantyExpiryDate: String?,
    val reminderDays: String?,
    val customReminderDays: Int?,
    val notes: String?,
    val price: Double?,
    val tags: String?,
    val additionalImageUris: List<String>?,
    val isPaid: Boolean?,
    val lastPaidDate: String?,
    val billingCycle: String?,
    val createdAt: String,
    val updatedAt: String
)

fun ReceiptWarranty.toExportItem(includeImages: Boolean = true): ExportItem {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    return ExportItem(
        type = type.name,
        title = title,
        category = category,
        imageBase64 = if (includeImages) imageUri?.let { loadBase64FromUri(it) } else null,
        purchaseDate = purchaseDate?.let { isoFormat.format(Date(it)) },
        warrantyExpiryDate = warrantyExpiryDate?.let { isoFormat.format(Date(it)) },
        reminderDays = reminderDays?.name,
        customReminderDays = customReminderDays,
        notes = notes,
        price = price,
        tags = tags,
        additionalImageUris = additionalImageUris?.split(",")?.filter { it.isNotBlank() },
        isPaid = if (type == ReceiptType.BILL || type == ReceiptType.SUBSCRIPTION) isPaid else null,
        lastPaidDate = lastPaidDate?.let { isoFormat.format(Date(it)) },
        billingCycle = billingCycle,
        createdAt = isoFormat.format(Date(createdAt)),
        updatedAt = isoFormat.format(Date(updatedAt))
    )
}

private fun loadBase64FromUri(uriString: String): String? {
    return try {
        val uri: Uri = uriString.toUri()
        val context = ReceiptWarrantyApp.INSTANCE
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bytes = input.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        null
    }
}

fun ExportItem.toReceiptWarranty(): ReceiptWarranty {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val parsedType = try {
        ReceiptType.valueOf(type)
    } catch (e: Exception) {
        ReceiptType.RECEIPT
    }

    return ReceiptWarranty(
        id = 0,
        type = parsedType,
        title = title,
        category = category,
        imageUri = null,
        driveFileId = null,
        cloudId = null,
        purchaseDate = purchaseDate?.let { runCatching { isoFormat.parse(it) }.getOrNull()?.time },
        warrantyExpiryDate = warrantyExpiryDate?.let { runCatching { isoFormat.parse(it) }.getOrNull()?.time },
        reminderDays = reminderDays?.let { runCatching { ReminderDays.valueOf(it) }.getOrNull() },
        customReminderDays = customReminderDays,
        notes = notes,
        price = price,
        tags = tags,
        additionalImageUris = additionalImageUris?.joinToString(","),
        isDeleted = false,
        deletedAt = null,
        isPaid = isPaid ?: false,
        lastPaidDate = lastPaidDate?.let { runCatching { isoFormat.parse(it) }.getOrNull()?.time },
        billingCycle = billingCycle,
        paymentHistory = null,
        isArchived = false,
        createdAt = runCatching { isoFormat.parse(createdAt) }.getOrNull()?.time ?: System.currentTimeMillis(),
        updatedAt = runCatching { isoFormat.parse(updatedAt) }.getOrNull()?.time ?: System.currentTimeMillis()
    )
}

data class ImportPreview(
    val totalItems: Int,
    val receipts: Int,
    val warranties: Int,
    val bills: Int,
    val subscriptions: Int,
    val items: List<ExportItem>
)

enum class ImportConflictStrategy {
    SKIP,
    REPLACE,
    CREATE_NEW
}
