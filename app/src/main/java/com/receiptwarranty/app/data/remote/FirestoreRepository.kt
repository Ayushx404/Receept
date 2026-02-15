package com.receiptwarranty.app.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.receiptwarranty.app.data.ReceiptWarranty
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.data.ReminderDays
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val userId: String) {

    private val TAG = "FirestoreRepository"
    
    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("users").document(userId)
    private val receiptsRef = userRef.collection("receipts")
    private val profileRef = userRef.collection("profile")
    private val syncRef = userRef.collection("metadata").document("sync")

    suspend fun uploadItem(item: ReceiptWarranty): Result<String> {
        return try {
            Log.d(TAG, "Uploading item to Firestore: ${item.title}, imageUri: ${item.imageUri}")
            
            val firestoreItem = FirestoreReceiptWarranty.fromLocalModel(item)
            val docRef = if (!item.cloudId.isNullOrEmpty()) {
                // Update existing document
                receiptsRef.document(item.cloudId)
            } else {
                // Create new document
                receiptsRef.document()
            }
            docRef.set(firestoreItem).await()
            updateSyncMetadata()
            Log.d(TAG, "Item uploaded successfully: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload item: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateItem(item: ReceiptWarranty): Result<Unit> {
        return try {
            val cloudId = item.cloudId ?: return Result.failure(Exception("No cloud ID to update"))
            val firestoreItem = FirestoreReceiptWarranty.fromLocalModel(item)
            receiptsRef.document(cloudId)
                .set(firestoreItem)
                .await()
            updateSyncMetadata()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            receiptsRef.document(itemId).delete().await()
            updateSyncMetadata()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAllItems(): Result<List<ReceiptWarranty>> {
        return try {
            val snapshot = receiptsRef.orderBy("updatedAt", Query.Direction.DESCENDING).get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    val firestoreItem = doc.toObject(FirestoreReceiptWarranty::class.java)
                    firestoreItem?.toLocalModel()
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeItems(): Flow<List<FirestoreReceiptWarranty>> = callbackFlow {
        val listener = receiptsRef
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreReceiptWarranty::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(email: String, displayName: String?): Result<Unit> {
        return try {
            val profile = FirestoreUserProfile(
                userId = userId,
                email = email,
                displayName = displayName,
                lastSyncTime = System.currentTimeMillis()
            )
            profileRef.document("info").set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSyncMetadata(): FirestoreSyncMetadata {
        return try {
            val doc = syncRef.get().await()
            doc.toObject(FirestoreSyncMetadata::class.java) ?: FirestoreSyncMetadata(userId = userId)
        } catch (e: Exception) {
            FirestoreSyncMetadata(userId = userId)
        }
    }

    private suspend fun updateSyncMetadata() {
        try {
            syncRef.set(
                mapOf(
                    "userId" to userId,
                    "lastSyncTime" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            // Silent fail for metadata update
        }
    }

    suspend fun clearAllData(): Result<Unit> {
        return try {
            val snapshot = receiptsRef.get().await()
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
