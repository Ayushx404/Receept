package com.receiptwarranty.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptWarrantyRepository(
    private val dao: ReceiptWarrantyDao
) {

    fun getAll(): Flow<List<ReceiptWarranty>> = dao.getAll()

    fun search(query: String): Flow<List<ReceiptWarranty>> {
        return dao.getAll().map { items ->
            if (query.isBlank()) items
            else {
                val lower = query.lowercase().trim()
                items.filter {
                    it.title.lowercase().contains(lower) ||
                            it.company.lowercase().contains(lower) ||
                            it.notes?.lowercase()?.contains(lower) == true ||
                            it.category?.lowercase()?.contains(lower) == true
                }
            }
        }
    }

    fun filterByWarrantyStatus(
        items: List<ReceiptWarranty>,
        filter: WarrantyFilter
    ): List<ReceiptWarranty> {
        return when (filter) {
            WarrantyFilter.ALL -> items
            WarrantyFilter.ALL_RECEIPTS -> items.filter { it.type == ReceiptType.RECEIPT }
            WarrantyFilter.ALL_WARRANTIES -> items.filter { it.type == ReceiptType.WARRANTY }
            WarrantyFilter.EXPIRING_SOON -> items.filter { 
                it.type == ReceiptType.WARRANTY && it.warrantyStatus() == WarrantyStatus.EXPIRING_SOON 
            }
            WarrantyFilter.EXPIRED -> items.filter { 
                it.type == ReceiptType.WARRANTY && it.warrantyStatus() == WarrantyStatus.EXPIRED 
            }
        }
    }

    fun filterByCategory(
        items: List<ReceiptWarranty>,
        category: String?
    ): List<ReceiptWarranty> {
        return if (category == null) items
        else items.filter { it.category == category }
    }

    fun getById(id: Long): Flow<ReceiptWarranty?> = dao.getById(id)

    fun getAllCompanies(): Flow<List<String>> = dao.getAllCompanies()

    // Category methods
    fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()

    fun getByCategory(category: String): Flow<List<ReceiptWarranty>> = dao.getByCategory(category)

    fun getCategoryStats(): Flow<List<CategoryCount>> = dao.getCategoryStats()

    // Statistics methods
    fun getReceiptCount(): Flow<Int> = dao.getReceiptCount()

    fun getWarrantyCount(): Flow<Int> = dao.getWarrantyCount()

    fun getActiveWarrantyCount(): Flow<Int> = dao.getActiveWarrantyCount()

    fun getExpiredWarrantyCount(): Flow<Int> = dao.getExpiredWarrantyCount()

    fun getExpiringSoonCount(): Flow<Int> = dao.getExpiringSoonCount()

    fun getExpiringSoonItems(): Flow<List<ReceiptWarranty>> = dao.getExpiringSoonItems()

    // Reminder methods
    fun getItemsWithReminders(): Flow<List<ReceiptWarranty>> = dao.getItemsWithReminders()

    // Export method
    suspend fun exportToCSV(): String {
        val items = dao.getAllItemsForExport()
        return buildCSV(items)
    }

    private fun buildCSV(items: List<ReceiptWarranty>): String {
        val sb = StringBuilder()
        sb.appendLine("id,type,title,company,category,purchase_date,expiry_date,reminder,notes,created_at")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        items.forEach { item ->
            sb.appendLine(
                "${item.id}," +
                "${item.type}," +
                "\"${item.title.replace("\"", "\"\"")}\"," +
                "\"${item.company.replace("\"", "\"\"")}\"," +
                "${item.category ?: ""}," +
                "${item.purchaseDate?.let { dateFormat.format(Date(it)) } ?: ""}," +
                "${item.warrantyExpiryDate?.let { dateFormat.format(Date(it)) } ?: ""}," +
                "${item.reminderDays?.displayName ?: ""}," +
                "\"${item.notes?.replace("\"", "\"\"") ?: ""}\"," +
                "${item.createdAt}"
            )
        }

        return sb.toString()
    }

    suspend fun insert(item: ReceiptWarranty): Long = dao.insert(item)

    suspend fun update(item: ReceiptWarranty) = dao.update(item)

    suspend fun delete(item: ReceiptWarranty) = dao.delete(item)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
