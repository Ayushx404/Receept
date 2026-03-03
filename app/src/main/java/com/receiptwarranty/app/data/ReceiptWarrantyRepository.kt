package com.receiptwarranty.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.receiptwarranty.app.data.model.ImportConflictStrategy
import com.receiptwarranty.app.util.JsonImporter

class ReceiptWarrantyRepository(
    private val dao: ReceiptWarrantyDao
) {

    fun getAll(): Flow<List<ReceiptWarranty>> = dao.getAll()

    fun getArchivedItems(): Flow<List<ReceiptWarranty>> = dao.getArchivedItems()

    fun search(query: String): Flow<List<ReceiptWarranty>> {
        return dao.getAll().map { items ->
            searchAndFilter(items, query)
        }
    }

    /**
     * Single shared search + filter + category pipeline.
     * Eliminates triplicated logic in ViewModels.
     */
    fun searchAndFilter(
        items: List<ReceiptWarranty>,
        query: String = "",
        filter: WarrantyFilter = WarrantyFilter.ALL,
        category: String? = null,
        tags: List<String> = emptyList()
    ): List<ReceiptWarranty> {
        var result = items
        if (query.isNotBlank()) {
            val lower = query.lowercase().trim()
            result = result.filter {
                it.title.lowercase().contains(lower) ||
                    it.notes?.lowercase()?.contains(lower) == true ||
                    it.category?.lowercase()?.contains(lower) == true ||
                    it.tags?.lowercase()?.contains(lower) == true
            }
        }
        result = when (filter) {
            WarrantyFilter.ALL -> result
            WarrantyFilter.ALL_RECEIPTS -> result.filter { it.type == ReceiptType.RECEIPT }
            WarrantyFilter.ALL_WARRANTIES -> result.filter { it.type == ReceiptType.WARRANTY }
            WarrantyFilter.ALL_BILLS -> result.filter { it.type == ReceiptType.BILL }
            WarrantyFilter.EXPIRING_SOON -> result.filter {
                it.type != ReceiptType.RECEIPT && it.warrantyStatus() == WarrantyStatus.EXPIRING_SOON
            }
            WarrantyFilter.EXPIRED -> result.filter {
                it.type == ReceiptType.WARRANTY && it.warrantyStatus() == WarrantyStatus.EXPIRED
            }
            WarrantyFilter.SUBSCRIPTIONS -> result.filter { it.type == ReceiptType.SUBSCRIPTION }
        }
        if (category != null) result = result.filter { it.category == category }
        if (tags.isNotEmpty()) {
            result = result.filter { item ->
                val itemTags = item.tags?.split(",")?.map { it.trim().lowercase() } ?: emptyList()
                tags.all { tag -> itemTags.contains(tag.lowercase()) }
            }
        }
        return result
    }

    fun getById(id: Long): Flow<ReceiptWarranty?> = dao.getById(id)

    fun getAllTags(): Flow<List<String>> = dao.getAllTags()

    // Category methods
    fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()

    fun getByCategory(category: String): Flow<List<ReceiptWarranty>> = dao.getByCategory(category)

    fun getCategoryStats(): Flow<List<CategoryCount>> = dao.getCategoryStats()

    // Statistics methods
    fun getReceiptCount(): Flow<Int> = dao.getReceiptCount()

    fun getWarrantyCount(): Flow<Int> = dao.getWarrantyCount()

    fun getBillCount(): Flow<Int> = dao.getBillCount()

    fun getActiveWarrantyCount(): Flow<Int> = dao.getActiveWarrantyCount()

    fun getExpiredWarrantyCount(): Flow<Int> = dao.getExpiredWarrantyCount()

    fun getExpiringSoonCount(): Flow<Int> = dao.getExpiringSoonCount()

    fun getExpiringSoonItems(): Flow<List<ReceiptWarranty>> = dao.getExpiringSoonItems()

    fun getUpcomingBillsTotal(): Flow<Double?> = dao.getUpcomingBillsTotal()

    fun getSubscriptionCount(): Flow<Int> = dao.getSubscriptionCount()

    fun getTotalValueProtected(): Flow<Double?> = dao.getTotalValueProtected()

    fun getTotalValueAll(): Flow<Double?> = dao.getTotalValueAll()

    fun getValueByType(type: ReceiptType): Flow<Double?> = dao.getValueByType(type.name)

    fun getUpcomingRenewals(limit: Int = 10): Flow<List<ReceiptWarranty>> {
        val now = System.currentTimeMillis()
        val futureDate = now + (90 * 24 * 60 * 60 * 1000L)
        return dao.getUpcomingRenewals(now, futureDate, limit)
    }

    fun getUpcomingSubscriptions(limit: Int = 10): Flow<List<ReceiptWarranty>> {
        val now = System.currentTimeMillis()
        return dao.getUpcomingSubscriptions(now, limit)
    }

    fun getUpcomingBills(limit: Int = 10): Flow<List<ReceiptWarranty>> {
        val now = System.currentTimeMillis()
        return dao.getUpcomingBills(now, limit)
    }

    fun getMonthlySubscriptionCost(): Flow<Double?> = dao.getMonthlySubscriptionCost()

    // Reminder methods
    fun getItemsWithReminders(): Flow<List<ReceiptWarranty>> = dao.getItemsWithReminders()

    // Feature methods (Trash & Spending)
    fun getTrashItems(): Flow<List<ReceiptWarranty>> = dao.getTrashItems()
    
    suspend fun softDelete(id: Long) = dao.softDelete(id)
    
    suspend fun restoreItem(id: Long) = dao.restoreItem(id)
    
    suspend fun permanentlyDeleteOldTrash(cutoffMs: Long) = dao.permanentlyDeleteOldTrash(cutoffMs)
    
    suspend fun emptyTrash() = dao.emptyTrash()

    suspend fun archiveItem(id: Long) = dao.archiveItem(id)

    suspend fun unarchiveItem(id: Long) = dao.unarchiveItem(id)
    
    fun getTotalSpending(): Flow<Double?> = dao.getTotalSpending()

    fun getSpendingForMonth(monthOffset: Int = 0): Flow<Double?> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        return dao.getSpendingForPeriod(start, cal.timeInMillis)
    }

    // Export method
    suspend fun exportToCSV(): String {
        val items = dao.getAllItemsForExport()
        return buildCSV(items)
    }

    suspend fun getAllItemsForExport(): List<ReceiptWarranty> {
        return dao.getAllItemsForExport()
    }

    suspend fun getItemsByTypeForExport(type: ReceiptType): List<ReceiptWarranty> {
        return dao.getItemsByTypeForExport(type.name)
    }

    suspend fun getItemsByDateRangeForExport(startDate: Long, endDate: Long): List<ReceiptWarranty> {
        return dao.getItemsByDateRangeForExport(startDate, endDate)
    }

    suspend fun getItemsByTypeAndDateRangeForExport(type: ReceiptType, startDate: Long, endDate: Long): List<ReceiptWarranty> {
        return dao.getItemsByTypeAndDateRangeForExport(type.name, startDate, endDate)
    }

    suspend fun importItems(
        items: List<ReceiptWarranty>,
        conflictStrategy: ImportConflictStrategy = ImportConflictStrategy.SKIP
    ): Int {
        var imported = 0
        for (item in items) {
            val existingItem = findExistingItem(item)
            when {
                existingItem != null && conflictStrategy == ImportConflictStrategy.SKIP -> {
                    // Skip
                }
                existingItem != null && conflictStrategy == ImportConflictStrategy.REPLACE -> {
                    val updatedItem = item.copy(
                        id = existingItem.id,
                        createdAt = existingItem.createdAt
                    )
                    dao.update(updatedItem)
                    imported++
                }
                else -> {
                    dao.insert(item)
                    imported++
                }
            }
        }
        return imported
    }

    private suspend fun findExistingItem(newItem: ReceiptWarranty): ReceiptWarranty? {
        return dao.findByUniqueFields(
            newItem.title,
            newItem.category,
            newItem.purchaseDate
        )
    }

    private fun buildCSV(items: List<ReceiptWarranty>): String {
        val sb = StringBuilder()
        sb.appendLine("id,type,title,category,price,tags,purchase_date,expiry_date,reminder,billing_cycle,payment_history,notes,created_at")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        items.forEach { item ->
            sb.appendLine(
                "${item.id}," +
                "${item.type}," +
                "\"${item.title.replace("\"", "\"\"")}\"," +
                "${item.category ?: ""}," +
                "${item.price ?: ""}," +
                "\"${item.tags ?: ""}\"," +
                "${item.purchaseDate?.let { dateFormat.format(Date(it)) } ?: ""}," +
                "${item.warrantyExpiryDate?.let { dateFormat.format(Date(it)) } ?: ""}," +
                "${item.reminderDays?.displayName ?: ""}," +
                "\"${item.billingCycle ?: ""}\"," +
                "\"${item.paymentHistory ?: ""}\"," +
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
    
    // Deleted items tracking methods
    suspend fun trackDeletion(cloudId: String, userId: String) {
        dao.insertDeletedItem(DeletedItem(cloudId, System.currentTimeMillis(), userId))
    }
    
    suspend fun isItemDeleted(cloudId: String, userId: String): Boolean {
        return dao.findDeletedItem(cloudId, userId) != null
    }
    
    suspend fun cleanOldDeletedItems(userId: String, olderThan: Long) {
        dao.deleteOldDeletedItems(userId, olderThan)
    }
    
    suspend fun getAllDeletedCloudIds(userId: String): List<String> {
        return dao.getAllDeletedCloudIds(userId)
    }

    fun getValueBreakdown(): Flow<Map<String, Double>> = combine(
        getValueByType(ReceiptType.WARRANTY),
        getValueByType(ReceiptType.RECEIPT),
        getValueByType(ReceiptType.BILL),
        getValueByType(ReceiptType.SUBSCRIPTION)
    ) { warranty, receipt, bill, subscription ->
        mapOf(
            "Warranty" to (warranty ?: 0.0),
            "Receipt" to (receipt ?: 0.0),
            "Bill" to (bill ?: 0.0),
            "Subscription" to (subscription ?: 0.0)
        )
    }

    fun getRecentPayments(limit: Int = 5): Flow<List<ReceiptWarranty>> = dao.getRecentPayments(limit)

    suspend fun deleteAllItems() {
        dao.deleteAll()
    }

    suspend fun resetSyncMetadata() {
        dao.getAll().first().forEach { item ->
            dao.update(item.copy(cloudId = null, driveFileId = null, updatedAt = System.currentTimeMillis()))
        }
    }
}
