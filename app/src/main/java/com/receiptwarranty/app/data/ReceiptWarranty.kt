package com.receiptwarranty.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReceiptType {
    RECEIPT,
    WARRANTY,
    BILL,
    SUBSCRIPTION
}

enum class ReminderDays(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 day before"),
    THREE_DAYS(3, "3 days before"),
    FIVE_DAYS(5, "5 days before"),
    ONE_WEEK(7, "1 week before"),
    TWO_WEEKS(14, "2 weeks before"),
    ONE_MONTH(30, "1 month before"),
    THREE_MONTHS(90, "3 months before"),
    CUSTOM(-1, "Custom")
}

@Entity(tableName = "receipt_warranty")
data class ReceiptWarranty(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: ReceiptType,
    val title: String,
    val category: String? = null,
    val imageUri: String? = null,
    val driveFileId: String? = null,
    val cloudId: String? = null,
    val purchaseDate: Long? = null,
    val warrantyExpiryDate: Long? = null,
    val reminderDays: ReminderDays? = null,
    val customReminderDays: Int? = null,
    val notes: String? = null,
    val price: Double? = null,
    val tags: String? = null, // Comma-separated tags
    val additionalImageUris: String? = null, // Comma-separated URIs
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isPaid: Boolean = false,
    val lastPaidDate: Long? = null,
    val billingCycle: String? = null,
    val paymentHistory: String? = null, // Comma-separated timestamps
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun warrantyStatus(): WarrantyStatus {
        if (type == ReceiptType.RECEIPT) return WarrantyStatus.NO_WARRANTY
        val expiry = warrantyExpiryDate ?: return WarrantyStatus.NO_WARRANTY
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        return when {
            type == ReceiptType.BILL -> when {
                expiry - now <= sevenDaysMs -> WarrantyStatus.EXPIRING_SOON  // billing soon
                else -> WarrantyStatus.VALID
            }
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
    ALL_BILLS,
    EXPIRING_SOON,
    EXPIRED,
    SUBSCRIPTIONS
}
