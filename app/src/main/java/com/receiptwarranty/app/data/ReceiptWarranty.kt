package com.receiptwarranty.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReceiptType {
    RECEIPT,
    WARRANTY
}

enum class ReminderDays(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 day before"),
    THREE_DAYS(3, "3 days before"),
    FIVE_DAYS(5, "5 days before"),
    ONE_WEEK(7, "1 week before")
}

@Entity(tableName = "receipt_warranty")
data class ReceiptWarranty(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: ReceiptType,
    val title: String,
    val company: String,
    val category: String? = null,
    val imageUri: String? = null,
    val driveFileId: String? = null,
    val cloudId: String? = null,
    val purchaseDate: Long? = null,
    val warrantyExpiryDate: Long? = null,
    val reminderDays: ReminderDays? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun warrantyStatus(): WarrantyStatus {
        val expiry = warrantyExpiryDate ?: return WarrantyStatus.NO_WARRANTY
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        return when {
            expiry < now -> WarrantyStatus.EXPIRED
            expiry - now <= sevenDaysMs -> WarrantyStatus.EXPIRING_SOON
            else -> WarrantyStatus.VALID
        }
    }
}

enum class WarrantyStatus {
    VALID,
    EXPIRING_SOON,
    EXPIRED,
    NO_WARRANTY
}

enum class WarrantyFilter {
    ALL,
    ALL_RECEIPTS,
    ALL_WARRANTIES,
    EXPIRING_SOON,
    EXPIRED
}
