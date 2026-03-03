package com.receiptwarranty.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.model.VaultExportData
import com.receiptwarranty.app.data.model.toExportItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ExportCategory {
    ALL,
    RECEIPTS,
    WARRANTIES,
    BILLS,
    SUBSCRIPTIONS
}

enum class ExportDateRange {
    ALL_TIME,
    LAST_MONTH,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    LAST_YEAR,
    CUSTOM
}

enum class ExportDestination {
    SHARE,
    DOWNLOADS
}

object JsonExporter {

    fun exportToJson(
        context: Context,
        items: List<com.receiptwarranty.app.data.ReceiptWarranty>,
        category: ExportCategory = ExportCategory.ALL,
        dateRange: ExportDateRange = ExportDateRange.ALL_TIME,
        customStartDate: Long? = null,
        customEndDate: Long? = null,
        includeImages: Boolean = true
    ): Result<Uri> {
        val filteredItems = filterItems(items, category, dateRange, customStartDate, customEndDate)
        
        val exportItems = filteredItems.map { it.toExportItem(includeImages) }
        
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        val exportData = VaultExportData(
            version = 1,
            exportDate = isoFormat.format(Date()),
            items = exportItems
        )
        
        val jsonString = buildJson(exportData)
        
        return saveToDownloads(context, jsonString)
    }

    fun shareJson(
        context: Context,
        items: List<com.receiptwarranty.app.data.ReceiptWarranty>,
        category: ExportCategory = ExportCategory.ALL,
        dateRange: ExportDateRange = ExportDateRange.ALL_TIME,
        customStartDate: Long? = null,
        customEndDate: Long? = null,
        includeImages: Boolean = true
    ): Result<Uri> {
        val filteredItems = filterItems(items, category, dateRange, customStartDate, customEndDate)
        
        val exportItems = filteredItems.map { it.toExportItem(includeImages) }
        
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        val exportData = VaultExportData(
            version = 1,
            exportDate = isoFormat.format(Date()),
            items = exportItems
        )
        
        val jsonString = buildJson(exportData)
        
        return shareViaShareSheet(context, jsonString)
    }

    private fun filterItems(
        items: List<com.receiptwarranty.app.data.ReceiptWarranty>,
        category: ExportCategory,
        dateRange: ExportDateRange,
        customStartDate: Long?,
        customEndDate: Long?
    ): List<com.receiptwarranty.app.data.ReceiptWarranty> {
        var result = items.filter { !it.isDeleted }
        
        result = when (category) {
            ExportCategory.ALL -> result
            ExportCategory.RECEIPTS -> result.filter { it.type == ReceiptType.RECEIPT }
            ExportCategory.WARRANTIES -> result.filter { it.type == ReceiptType.WARRANTY }
            ExportCategory.BILLS -> result.filter { it.type == ReceiptType.BILL }
            ExportCategory.SUBSCRIPTIONS -> result.filter { it.type == ReceiptType.SUBSCRIPTION }
        }
        
        val now = System.currentTimeMillis()
        val startDate = when (dateRange) {
            ExportDateRange.ALL_TIME -> null
            ExportDateRange.LAST_MONTH -> now - (30L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_3_MONTHS -> now - (90L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_6_MONTHS -> now - (180L * 24 * 60 * 60 * 1000)
            ExportDateRange.LAST_YEAR -> now - (365L * 24 * 60 * 60 * 1000)
            ExportDateRange.CUSTOM -> customStartDate
        }
        
        val endDate = if (dateRange == ExportDateRange.CUSTOM) customEndDate else now
        
        if (startDate != null) {
            result = result.filter { item ->
                val itemDate = item.purchaseDate ?: item.createdAt
                itemDate >= startDate
            }
        }
        
        if (endDate != null) {
            result = result.filter { item ->
                val itemDate = item.purchaseDate ?: item.createdAt
                itemDate <= endDate
            }
        }
        
        return result
    }

    private fun buildJson(data: VaultExportData): String {
        val json = JSONObject()
        json.put("version", data.version)
        json.put("exportDate", data.exportDate)
        
        val itemsArray = JSONArray()
        data.items.forEach { item ->
            val itemJson = JSONObject()
            itemJson.put("type", item.type)
            itemJson.put("title", item.title)
            itemJson.put("category", item.category)
            itemJson.put("imageBase64", item.imageBase64)
            itemJson.put("purchaseDate", item.purchaseDate)
            itemJson.put("warrantyExpiryDate", item.warrantyExpiryDate)
            itemJson.put("reminderDays", item.reminderDays)
            itemJson.put("customReminderDays", item.customReminderDays)
            itemJson.put("notes", item.notes)
            itemJson.put("price", item.price)
            itemJson.put("tags", item.tags)
            
            if (item.additionalImageUris != null) {
                itemJson.put("additionalImageUris", JSONArray(item.additionalImageUris))
            }
            
            itemJson.put("isPaid", item.isPaid)
            itemJson.put("lastPaidDate", item.lastPaidDate)
            itemJson.put("billingCycle", item.billingCycle)
            itemJson.put("createdAt", item.createdAt)
            itemJson.put("updatedAt", item.updatedAt)
            
            itemsArray.put(itemJson)
        }
        
        json.put("items", itemsArray)
        
        return json.toString(2)
    }

    private fun saveToDownloads(context: Context, jsonContent: String): Result<Uri> {
        val resolver = context.contentResolver
        val timestamp = SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.US).format(Date())
        val fileName = "vault_backup_$timestamp.json"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values)
            ?: return Result.failure(IllegalStateException("Could not create JSON file"))

        return try {
            resolver.openOutputStream(uri)?.use { output ->
                output.write(jsonContent.toByteArray())
                output.flush()
            } ?: return Result.failure(IllegalStateException("Could not write JSON file"))

            Result.success(uri)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Result.failure(e)
        }
    }

    private fun shareViaShareSheet(context: Context, jsonContent: String): Result<Uri> {
        return try {
            val timestamp = SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.US).format(Date())
            val fileName = "vault_backup_$timestamp.json"
            
            val cacheDir = File(context.cacheDir, "shared_exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            
            val file = File(cacheDir, fileName)
            file.writeText(jsonContent)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
