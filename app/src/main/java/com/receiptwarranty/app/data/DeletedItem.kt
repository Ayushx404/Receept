package com.receiptwarranty.app.data

import androidx.room.Entity
import androidx.room.Index

/**
 * Entity to track deleted items for proper sync conflict resolution
 * This helps prevent deleted items from being re-added during sync
 */
@Entity(
    tableName = "deleted_items",
    primaryKeys = ["cloudId", "userId"],
    indices = [Index(value = ["userId"], name = "idx_deleted_user")]
)
data class DeletedItem(
    val cloudId: String, // Firestore document ID
    val deletedAt: Long = System.currentTimeMillis(),
    val userId: String
)
