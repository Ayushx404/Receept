package com.receiptwarranty.app.data.remote

import com.google.firebase.firestore.DocumentId
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReminderDays

data class FirestoreReceiptWarranty(
    @DocumentId
    val id: String = "",
    val type: String = "WARRANTY",
    val title: String = "",
    val company: String = "",
    val category: String? = null,
    val imageUri: String? = null,
    val driveFileId: String? = null,
    val purchaseDate: Long? = null,
    val warrantyExpiryDate: Long? = null,
    val reminderDays: String? = null,
    val notes: String? = null,
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
            company = company,
            category = category,
            imageUri = imageUri,
            driveFileId = driveFileId,
            purchaseDate = purchaseDate,
            warrantyExpiryDate = warrantyExpiryDate,
            reminderDays = reminderDays?.let {
                try { ReminderDays.valueOf(it) } catch (e: Exception) { null }
            },
            notes = notes,
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
                company = localModel.company,
                category = localModel.category,
                imageUri = localModel.imageUri,
                driveFileId = localModel.driveFileId,
                purchaseDate = localModel.purchaseDate,
                warrantyExpiryDate = localModel.warrantyExpiryDate,
                reminderDays = localModel.reminderDays?.name,
                notes = localModel.notes,
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
