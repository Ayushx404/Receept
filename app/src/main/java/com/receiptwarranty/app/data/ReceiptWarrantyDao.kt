package com.receiptwarranty.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptWarrantyDao {

    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReceiptWarranty>>

    @Query("SELECT * FROM receipt_warranty WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getArchivedItems(): Flow<List<ReceiptWarranty>>

    @Query("SELECT * FROM receipt_warranty WHERE id = :id AND isDeleted = 0")
    fun getById(id: Long): Flow<ReceiptWarranty?>

    // Tags queries
    @Query("SELECT DISTINCT tags FROM receipt_warranty WHERE tags IS NOT NULL AND isDeleted = 0 AND isArchived = 0")
    fun getAllTags(): Flow<List<String>>

    // Category queries
    @Query("SELECT DISTINCT category FROM receipt_warranty WHERE category IS NOT NULL AND isDeleted = 0 AND isArchived = 0 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM receipt_warranty WHERE category = :category AND isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<ReceiptWarranty>>

    @Query("SELECT category, COUNT(*) as count FROM receipt_warranty WHERE category IS NOT NULL AND isDeleted = 0 AND isArchived = 0 GROUP BY category ORDER BY count DESC")
    fun getCategoryStats(): Flow<List<CategoryCount>>

    // Statistics queries
    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'RECEIPT' AND isDeleted = 0 AND isArchived = 0")
    fun getReceiptCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND isDeleted = 0 AND isArchived = 0")
    fun getWarrantyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'BILL' AND isDeleted = 0 AND isArchived = 0")
    fun getBillCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'SUBSCRIPTION' AND isDeleted = 0 AND isArchived = 0")
    fun getSubscriptionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate > :now AND isDeleted = 0 AND isArchived = 0")
    fun getActiveWarrantyCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type IN ('WARRANTY', 'BILL') AND warrantyExpiryDate IS NOT NULL AND warrantyExpiryDate - :now <= :thresholdMs AND warrantyExpiryDate > :now AND isDeleted = 0 AND isArchived = 0")
    fun getExpiringSoonCount(
        now: Long = System.currentTimeMillis(),
        thresholdMs: Long = 7 * 24 * 60 * 60 * 1000L
    ): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate < :now AND isDeleted = 0 AND isArchived = 0")
    fun getExpiredWarrantyCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("""
        SELECT * FROM receipt_warranty 
        WHERE type IN ('WARRANTY', 'BILL')
        AND warrantyExpiryDate IS NOT NULL 
        AND warrantyExpiryDate > :now 
        AND warrantyExpiryDate - :now <= :thirtyDaysMs 
        AND isDeleted = 0
        AND isArchived = 0
        AND (type = 'WARRANTY' OR isPaid = 0)
        ORDER BY warrantyExpiryDate ASC
    """)
    fun getExpiringSoonItems(
        now: Long = System.currentTimeMillis(),
        thirtyDaysMs: Long = 30L * 24 * 60 * 60 * 1000
    ): Flow<List<ReceiptWarranty>>

    // Upcoming liabilities
    @Query("""
        SELECT SUM(price) FROM receipt_warranty 
        WHERE type = 'BILL'
        AND warrantyExpiryDate IS NOT NULL 
        AND warrantyExpiryDate > :now 
        AND warrantyExpiryDate - :now <= :thirtyDaysMs 
        AND isDeleted = 0
        AND isPaid = 0
        AND isArchived = 0
    """)
    fun getUpcomingBillsTotal(
        now: Long = System.currentTimeMillis(),
        thirtyDaysMs: Long = 30L * 24 * 60 * 60 * 1000
    ): Flow<Double?>

    @Query("SELECT COALESCE(SUM(price), 0) FROM receipt_warranty WHERE isDeleted = 0 AND isArchived = 0 AND type IN ('WARRANTY', 'SUBSCRIPTION') AND (warrantyExpiryDate IS NULL OR warrantyExpiryDate > :now)")
    fun getTotalValueProtected(now: Long = System.currentTimeMillis()): Flow<Double?>

    @Query("SELECT COALESCE(SUM(price), 0) FROM receipt_warranty WHERE isDeleted = 0 AND isArchived = 0")
    fun getTotalValueAll(): Flow<Double?>

    @Query("SELECT COALESCE(SUM(price), 0) FROM receipt_warranty WHERE type = :type AND isDeleted = 0 AND isArchived = 0 AND (warrantyExpiryDate IS NULL OR warrantyExpiryDate > :now)")
    fun getValueByType(type: String, now: Long = System.currentTimeMillis()): Flow<Double?>

    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 AND isArchived = 0 AND type IN ('WARRANTY', 'BILL', 'SUBSCRIPTION') AND warrantyExpiryDate IS NOT NULL AND warrantyExpiryDate BETWEEN :now AND :futureDate AND (type = 'WARRANTY' OR isPaid = 0) ORDER BY warrantyExpiryDate ASC LIMIT :limit")
    fun getUpcomingRenewals(now: Long, futureDate: Long, limit: Int = 10): Flow<List<ReceiptWarranty>>

    @Query("SELECT * FROM receipt_warranty WHERE type = 'SUBSCRIPTION' AND isDeleted = 0 AND isArchived = 0 AND isPaid = 0 AND warrantyExpiryDate IS NOT NULL AND warrantyExpiryDate > :now ORDER BY warrantyExpiryDate ASC LIMIT :limit")
    fun getUpcomingSubscriptions(now: Long, limit: Int = 10): Flow<List<ReceiptWarranty>>

    @Query("SELECT * FROM receipt_warranty WHERE type = 'BILL' AND isDeleted = 0 AND isArchived = 0 AND isPaid = 0 AND warrantyExpiryDate IS NOT NULL AND warrantyExpiryDate > :now ORDER BY warrantyExpiryDate ASC LIMIT :limit")
    fun getUpcomingBills(now: Long, limit: Int = 10): Flow<List<ReceiptWarranty>>

    @Query("SELECT COALESCE(SUM(price), 0) FROM receipt_warranty WHERE type = 'SUBSCRIPTION' AND isDeleted = 0 AND isArchived = 0")
    fun getMonthlySubscriptionCost(): Flow<Double?>

    // Reminder queries
    @Query("""
        SELECT * FROM receipt_warranty 
        WHERE type = 'WARRANTY' 
        AND reminderDays IS NOT NULL 
        AND warrantyExpiryDate IS NOT NULL
        AND isDeleted = 0
        AND isArchived = 0
    """)
    fun getItemsWithReminders(): Flow<List<ReceiptWarranty>>

    // Export query (synchronous for CSV generation)
    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllItemsForExport(): List<ReceiptWarranty>

    // Filtered export queries for JSON
    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 AND type = :type ORDER BY createdAt DESC")
    suspend fun getItemsByTypeForExport(type: String): List<ReceiptWarranty>

    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 AND purchaseDate >= :startDate AND purchaseDate <= :endDate ORDER BY createdAt DESC")
    suspend fun getItemsByDateRangeForExport(startDate: Long, endDate: Long): List<ReceiptWarranty>

    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 0 AND type = :type AND purchaseDate >= :startDate AND purchaseDate <= :endDate ORDER BY createdAt DESC")
    suspend fun getItemsByTypeAndDateRangeForExport(type: String, startDate: Long, endDate: Long): List<ReceiptWarranty>

    // Insert, Update and Delete
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReceiptWarranty): Long

    @Update
    suspend fun update(item: ReceiptWarranty)

    @Delete
    suspend fun delete(item: ReceiptWarranty)

    @Query("DELETE FROM receipt_warranty WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM receipt_warranty")
    suspend fun deleteAll()
    
    // Find by unique fields for sync
    @Query("""
        SELECT * FROM receipt_warranty 
        WHERE title = :title 
        AND category = :category
        AND (purchaseDate = :purchaseDate OR (purchaseDate IS NULL AND :purchaseDate IS NULL))
        AND isDeleted = 0
        LIMIT 1
    """)
    suspend fun findByUniqueFields(title: String, category: String?, purchaseDate: Long?): ReceiptWarranty?
    
    // Feature queries (Trash & Spending)
    @Query("SELECT * FROM receipt_warranty WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashItems(): Flow<List<ReceiptWarranty>>

    @Query("UPDATE receipt_warranty SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE receipt_warranty SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreItem(id: Long)

    @Query("DELETE FROM receipt_warranty WHERE isDeleted = 1 AND deletedAt < :cutoffMs")
    suspend fun permanentlyDeleteOldTrash(cutoffMs: Long)
    
    @Query("DELETE FROM receipt_warranty WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT SUM(price) FROM receipt_warranty WHERE isDeleted = 0 AND isArchived = 0 AND price IS NOT NULL")
    fun getTotalSpending(): Flow<Double?>

    @Query("""
        SELECT SUM(price) FROM receipt_warranty 
        WHERE isDeleted = 0 
        AND isArchived = 0
        AND price IS NOT NULL 
        AND purchaseDate >= :startOfMonth 
        AND purchaseDate <= :endOfMonth
    """)
    fun getSpendingForPeriod(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
    
    // Deleted items tracking
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedItem(deletedItem: DeletedItem)
    
    @Query("SELECT * FROM deleted_items WHERE cloudId = :cloudId AND userId = :userId")
    suspend fun findDeletedItem(cloudId: String, userId: String): DeletedItem?
    
    @Query("SELECT cloudId FROM deleted_items WHERE userId = :userId")
    suspend fun getAllDeletedCloudIds(userId: String): List<String>
    
    @Query("DELETE FROM deleted_items WHERE cloudId = :cloudId AND userId = :userId")
    suspend fun deleteDeletedItem(cloudId: String, userId: String)
    
    @Query("DELETE FROM deleted_items WHERE userId = :userId")
    suspend fun clearDeletedItems(userId: String)
    
    @Query("SELECT COUNT(*) FROM deleted_items WHERE userId = :userId")
    suspend fun getDeletedItemsCount(userId: String): Int
    
    @Query("DELETE FROM deleted_items WHERE userId = :userId AND deletedAt < :timestamp")
    suspend fun deleteOldDeletedItems(userId: String, timestamp: Long)
    @Query("UPDATE receipt_warranty SET isArchived = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun archiveItem(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE receipt_warranty SET isArchived = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun unarchiveItem(id: Long, timestamp: Long = System.currentTimeMillis())
    @Query("SELECT * FROM receipt_warranty WHERE isPaid = 1 AND isDeleted = 0 AND type IN ('BILL', 'SUBSCRIPTION') ORDER BY lastPaidDate DESC LIMIT :limit")
    fun getRecentPayments(limit: Int = 5): Flow<List<ReceiptWarranty>>
}

data class CategoryCount(
    val category: String,
    val count: Int
)
