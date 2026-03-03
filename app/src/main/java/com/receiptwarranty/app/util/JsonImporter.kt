package com.receiptwarranty.app.util

import android.content.Context
import android.net.Uri
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.model.ExportItem
import com.receiptwarranty.app.data.model.ImportPreview
import com.receiptwarranty.app.data.model.VaultExportData
import com.receiptwarranty.app.data.model.toReceiptWarranty
import org.json.JSONArray
import org.json.JSONObject

object JsonImporter {

    fun parseJson(context: Context, uri: Uri): Result<VaultExportData> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalStateException("Cannot open file"))
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            
            val version = json.optInt("version", 1)
            val exportDate = json.optString("exportDate", "")
            
            val itemsArray = json.getJSONArray("items")
            val items = mutableListOf<ExportItem>()
            
            for (i in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(i)
                val item = parseExportItem(itemJson)
                items.add(item)
            }
            
            Result.success(VaultExportData(
                version = version,
                exportDate = exportDate,
                items = items
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getImportPreview(context: Context, uri: Uri): Result<ImportPreview> {
        return parseJson(context, uri).map { exportData ->
            val receipts = exportData.items.count { it.type == "RECEIPT" }
            val warranties = exportData.items.count { it.type == "WARRANTY" }
            val bills = exportData.items.count { it.type == "BILL" }
            val subscriptions = exportData.items.count { it.type == "SUBSCRIPTION" }
            
            ImportPreview(
                totalItems = exportData.items.size,
                receipts = receipts,
                warranties = warranties,
                bills = bills,
                subscriptions = subscriptions,
                items = exportData.items
            )
        }
    }

    fun importDataWithAutoConflict(
        context: Context,
        uri: Uri,
        existingItems: List<ReceiptWarranty>
    ): Result<ImportResult> {
        return try {
            val parseResult = parseJson(context, uri)
            if (parseResult.isFailure) {
                return Result.failure(parseResult.exceptionOrNull() ?: Exception("Parse failed"))
            }
            
            val exportData = parseResult.getOrNull()!!
            var imported = 0
            var skipped = 0
            
            val newItems = mutableListOf<ReceiptWarranty>()
            
            for (exportItem in exportData.items) {
                val newItem = exportItem.toReceiptWarranty()
                
                val existingItem = findExistingItem(existingItems, newItem)
                
                if (existingItem != null) {
                    skipped++
                } else {
                    newItems.add(newItem)
                    imported++
                }
            }
            
            Result.success(ImportResult(
                totalImported = imported,
                totalReplaced = 0,
                totalSkipped = skipped,
                items = newItems
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseExportItem(json: JSONObject): ExportItem {
        val additionalImages = json.optJSONArray("additionalImageUris")
        val additionalImagesList = if (additionalImages != null) {
            (0 until additionalImages.length()).map { additionalImages.getString(it) }
        } else null

        return ExportItem(
            type = json.optString("type", "RECEIPT"),
            title = json.optString("title", ""),
            category = json.optString("category").takeIf { it.isNotEmpty() },
            imageBase64 = json.optString("imageBase64").takeIf { it.isNotEmpty() },
            purchaseDate = json.optString("purchaseDate").takeIf { it.isNotEmpty() },
            warrantyExpiryDate = json.optString("warrantyExpiryDate").takeIf { it.isNotEmpty() },
            reminderDays = json.optString("reminderDays").takeIf { it.isNotEmpty() },
            customReminderDays = if (json.has("customReminderDays") && !json.isNull("customReminderDays")) json.getInt("customReminderDays") else null,
            notes = json.optString("notes").takeIf { it.isNotEmpty() },
            price = if (json.has("price") && !json.isNull("price")) json.getDouble("price") else null,
            tags = json.optString("tags").takeIf { it.isNotEmpty() },
            additionalImageUris = additionalImagesList,
            isPaid = if (json.has("isPaid") && !json.isNull("isPaid")) json.getBoolean("isPaid") else null,
            lastPaidDate = json.optString("lastPaidDate").takeIf { it.isNotEmpty() },
            billingCycle = json.optString("billingCycle").takeIf { it.isNotEmpty() },
            createdAt = json.optString("createdAt"),
            updatedAt = json.optString("updatedAt")
        )
    }

    private fun findExistingItem(
        existingItems: List<ReceiptWarranty>,
        newItem: ReceiptWarranty
    ): ReceiptWarranty? {
        return existingItems.find { existing ->
            existing.title == newItem.title &&
            existing.category == newItem.category &&
            existing.purchaseDate == newItem.purchaseDate &&
            existing.type == newItem.type &&
            !existing.isDeleted
        }
    }

    data class ImportResult(
        val totalImported: Int,
        val totalReplaced: Int,
        val totalSkipped: Int,
        val items: List<ReceiptWarranty>
    )
}
