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
import com.receiptwarranty.app.data.auth.GoogleAuthManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val authManager: GoogleAuthManager
) {

    private val TAG = "FirestoreRepository"
    
    private val userId: String
        get() = authManager.getCurrentUser()?.uid ?: "local"

    private val db = FirebaseFirestore.getInstance()
    private val userRef get() = db.collection("users").document(userId)
    private val receiptsRef get() = userRef.collection("receipts")
    private val profileRef get() = userRef.collection("profile")
    private val syncRef get() = userRef.collection("metadata").document("sync")

    suspend fun uploadItem(item: ReceiptWarranty): Result<String> {
        return try {
            Log.d(TAG, "Uploading item to Firestore: ${item.title}, imageUri: ${item.imageUri}")
            
            val firestoreItem = FirestoreReceiptWarranty.fromLocalModel(item)
            // Use local Room ID as deterministic Firestore document ID to prevent duplicates
            val docId = item.id.toString()
            val docRef = receiptsRef.document(docId)
            docRef.set(firestoreItem).await()
            updateSyncMetadata()
            Log.d(TAG, "Item uploaded successfully with ID: $docId")
            Result.success(docId)
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

    suspend fun downloadItemsSince(lastSyncTime: Long): Result<List<ReceiptWarranty>> {
        return try {
            val snapshot = receiptsRef
                .whereGreaterThan("updatedAt", lastSyncTime)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
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
            android.util.Log.w(TAG, "Failed to update sync metadata", e)
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

    suspend fun cleanupDuplicateDocuments(): Result<Int> {
        return try {
            Log.d(TAG, "Starting duplicate cleanup...")
            val snapshot = receiptsRef.get().await()
            
            val documentsById = snapshot.documents.groupBy { it.id }
            var deletedCount = 0
            
            for ((docId, docs) in documentsById) {
                if (docs.size > 1) {
                    Log.d(TAG, "Found ${docs.size} duplicates for ID: $docId")
                    val sortedByUpdated = docs.sortedByDescending {
                        (it.get("updatedAt") as? Long) ?: 0L
                    }
                    val toKeep = sortedByUpdated.first()
                    val toDelete = sortedByUpdated.drop(1)
                    
                    toDelete.forEach { doc ->
                        receiptsRef.document(doc.id).delete().await()
                        deletedCount++
                        Log.d(TAG, "Deleted duplicate document: ${doc.id}")
                    }
                }
            }
            
            Log.d(TAG, "Cleanup complete. Deleted $deletedCount duplicate documents")
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
