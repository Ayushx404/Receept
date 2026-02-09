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

    @Query("SELECT * FROM receipt_warranty ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReceiptWarranty>>

    @Query("SELECT * FROM receipt_warranty WHERE id = :id")
    fun getById(id: Long): Flow<ReceiptWarranty?>

    @Query("SELECT DISTINCT company FROM receipt_warranty ORDER BY company ASC")
    fun getAllCompanies(): Flow<List<String>>

    // Category queries
    @Query("SELECT DISTINCT category FROM receipt_warranty WHERE category IS NOT NULL ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM receipt_warranty WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<ReceiptWarranty>>

    @Query("SELECT category, COUNT(*) as count FROM receipt_warranty WHERE category IS NOT NULL GROUP BY category ORDER BY count DESC")
    fun getCategoryStats(): Flow<List<CategoryCount>>

    // Statistics queries
    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'RECEIPT'")
    fun getReceiptCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY'")
    fun getWarrantyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate > :now")
    fun getActiveWarrantyCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate - :now <= :thresholdMs")
    fun getExpiringSoonCount(
        now: Long = System.currentTimeMillis(),
        thresholdMs: Long = 7 * 24 * 60 * 60 * 1000L
    ): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate < :now")
    fun getExpiredWarrantyCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("""
        SELECT * FROM receipt_warranty 
        WHERE type = 'WARRANTY' 
        AND warrantyExpiryDate IS NOT NULL 
        AND warrantyExpiryDate > :now 
        AND warrantyExpiryDate - :now <= :thirtyDaysMs 
        ORDER BY warrantyExpiryDate ASC
    """)
    fun getExpiringSoonItems(
        now: Long = System.currentTimeMillis(),
        thirtyDaysMs: Long = 30L * 24 * 60 * 60 * 1000
    ): Flow<List<ReceiptWarranty>>

    // Reminder queries
    @Query("""
        SELECT * FROM receipt_warranty 
        WHERE type = 'WARRANTY' 
        AND reminderDays IS NOT NULL 
        AND warrantyExpiryDate IS NOT NULL
    """)
    fun getItemsWithReminders(): Flow<List<ReceiptWarranty>>

    // Export query (synchronous for CSV generation)
    @Query("SELECT * FROM receipt_warranty ORDER BY createdAt DESC")
    suspend fun getAllItemsForExport(): List<ReceiptWarranty>

    // Insert and Update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReceiptWarranty): Long

    @Update
    suspend fun update(item: ReceiptWarranty)

    @Delete
    suspend fun delete(item: ReceiptWarranty)

    @Query("DELETE FROM receipt_warranty WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategoryCount(
    val category: String,
    val count: Int
)
