package com.receiptwarranty.app.data.remote

import com.google.firebase.firestore.DocumentId
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReminderDays

data class FirestoreReceiptWarranty(
    @DocumentId
    val id: String = "",
    val type: String = "WARRANTY",
    val title: String = "",
    val category: String? = null,
    val imageUri: String? = null,
    val driveFileId: String? = null,
    val purchaseDate: Long? = null,
    val warrantyExpiryDate: Long? = null,
    val reminderDays: String? = null,
    val notes: String? = null,
    val price: Double? = null,
    val tags: String? = null,
    val additionalImageUris: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isPaid: Boolean = false,
    val lastPaidDate: Long? = null,
    val billingCycle: String? = null,
    val paymentHistory: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val localId: Long = 0
) {
    fun toLocalModel(): com.receiptwarranty.app.data.ReceiptWarranty {
        return com.receiptwarranty.app.data.ReceiptWarranty(
            id = localId,
            cloudId = id.ifEmpty { null },
            type = try { ReceiptType.valueOf(type) } catch (e: Exception) { ReceiptType.WARRANTY },
            title = title,
            category = category,
            imageUri = imageUri,
            driveFileId = driveFileId,
            purchaseDate = purchaseDate,
            warrantyExpiryDate = warrantyExpiryDate,
            reminderDays = reminderDays?.let {
                try { ReminderDays.valueOf(it) } catch (e: Exception) { null }
            },
            notes = notes,
            price = price,
            tags = tags,
            additionalImageUris = additionalImageUris,
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            isPaid = isPaid,
            lastPaidDate = lastPaidDate,
            billingCycle = billingCycle,
            paymentHistory = paymentHistory,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromLocalModel(
            localModel: com.receiptwarranty.app.data.ReceiptWarranty,
            cloudId: String = ""
        ): FirestoreReceiptWarranty {
            return FirestoreReceiptWarranty(
                id = cloudId,
                type = localModel.type.name,
                title = localModel.title,
                category = localModel.category,
                imageUri = localModel.imageUri,
                driveFileId = localModel.driveFileId,
                purchaseDate = localModel.purchaseDate,
                warrantyExpiryDate = localModel.warrantyExpiryDate,
                reminderDays = localModel.reminderDays?.name,
                notes = localModel.notes,
                price = localModel.price,
                tags = localModel.tags,
                additionalImageUris = localModel.additionalImageUris,
                isDeleted = localModel.isDeleted,
                deletedAt = localModel.deletedAt,
                isPaid = localModel.isPaid,
                lastPaidDate = localModel.lastPaidDate,
                billingCycle = localModel.billingCycle,
                paymentHistory = localModel.paymentHistory,
                createdAt = localModel.createdAt,
                updatedAt = System.currentTimeMillis(),
                localId = localModel.id
            )
        }
    }
}

data class FirestoreUserProfile(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val displayName: String? = null,
    val lastSyncTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestoreSyncMetadata(
    @DocumentId
    val userId: String = "",
    val lastSyncTime: Long = 0,
    val pendingChanges: Int = 0
)
